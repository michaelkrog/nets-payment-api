/*
 * Copyright by Apaq 2011-2013
 */

package dk.apaq.nets.payment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.IsoValue;
import static dk.apaq.nets.payment.MessageFields.*;
import dk.apaq.nets.payment.io.ChannelFactory;
import org.joda.money.Money;

/**
 * Request class for Capture requests.
 */
public final class CaptureRequest extends AbstractNetsRequest<CaptureRequest> {
    private boolean recurring;
    private boolean amountDiffers;
    private boolean gambling;
    private boolean refund;
    private ActionCode actionCode;
    private String approvalCode;

    /**
     * Constructs a new CaptureRequest.
     * @param merchant The merchant.
     * @param card The card
     * @param money The amount and currency.
     * @param orderId The order id.
     * @param ode The ODE (Auth)
     * @param approvalCode The approval code.
     * @param actionCode The action code.
     * @param channelFactory The channelfactory.
     */
    public CaptureRequest(Merchant merchant, Card card, Money money, String orderId, String ode, String approvalCode, ActionCode actionCode, 
            ChannelFactory channelFactory) {
        super(merchant, card, money, orderId, ode, channelFactory);
        this.actionCode = actionCode;
        this.approvalCode = approvalCode;
    }

    /**
     * Whether this is a capture for a recurring payment.
     * @param value True/False
     * @return This request instance.
     */
    public CaptureRequest setRecurring(boolean value) {
        this.recurring = value;
        return this;
    }

    /**
     * Whether this request is marked as used for a recurring payment.
     * @return True/False
     */
    public boolean isRecurring() {
        return recurring;
    }

    /**
     * Whether this Capture Request is used for a Refund.
     * @return True/False
     */
    public boolean isRefund() {
        return refund;
    }

    /**
     * Whether this is a refund for a capture.
     * @param value True/False
     * @return This request instance.
     */
    public CaptureRequest setRefund(boolean value) {
        this.refund = value;
        return this;
    }

    /**
     * Whether the amount differs. Used if amount of transaction is less than echoed in 'PSIP authorization Response'. 
     * Used as well for split shipment (if allowed for the card type) and subsequently for all partial captures.
     * @param value True/False
     * @return This request instance.
     */
    public CaptureRequest setAmountDiffers(boolean value) {
        this.amountDiffers = value;
        return this;
    }

    /**
     * Whether the request is marked as if the amount differs. See <code>setAmountDiffers</code>.
     * @return True/False
     */
    public boolean isAmountDiffers() {
        return amountDiffers;
    }

    /**
     * Retrieves the action code.
     * @return  The ActionCode.
     */
    public ActionCode getActionCode() {
        return actionCode;
    }

    /**
     * Retrieves the ApprovaleCode.
     * @return The approval code.
     */
    public String getApprovalCode() {
        return approvalCode;
    }

    /**
     * Sets whether this is related to gambling.
     * @param value True/False
     * @return This request instance.
     */
    public CaptureRequest setGambling(boolean value) {
        this.gambling = value;
        return this;
    }

    /**
     * Whether the request is marked as related to gambling.
     * @return True/FAlse
     */
    public boolean isGambling() {
        return gambling;
    }

    @Override
    protected IsoMessage buildMessage() {
        DateFormat df = new SimpleDateFormat(DATE_FORMAT);
        IsoMessage message = getChannelFactory().getMessageFactory().newMessage(MessageTypes.CAPTURE_REQUEST);
        message.setIsoHeader(PsipHeader.OK.toString());
        String expire = EXPIRE_FORMAT.format(getCard().getExpireYear()) + EXPIRE_FORMAT.format(getCard().getExpireMonth());
        String pointOfService = recurring ? PointOfService.InternetMerchantRecurring.getCode() : PointOfService.InternetMerchant.getCode();
        String processingCode;
        String function;
        if (refund) {
            processingCode = ProcessingCode.GoodsAndServiceCredit.getCode();
            function = FunctionCode.Capture_Original_Transaction.getCode();
        } else {
            processingCode = gambling ? ProcessingCode.QuasiCash.getCode() : ProcessingCode.GoodsAndServices.getCode();
            function = amountDiffers ? FunctionCode.Capture_Amount_Differs.getCode() : FunctionCode.Capture_Amount_Accurate.getCode();
        }
        String address = buildAddressField();
        message.setField(PRIMARY_ACCOUNT_NUMBER, new IsoValue<String>(IsoType.LLVAR, getCard().getCardNumber()));
        message.setField(PROCESSING_CODE, new IsoValue<String>(IsoType.NUMERIC, processingCode, PROCESSING_CODE_LENGTH));
        message.setField(AMOUNT, new IsoValue<Integer>(IsoType.NUMERIC, getMoney().getAmountMinorInt(), AMOUNT_LENGTH));
        message.setField(LOCAL_TIME, new IsoValue<String>(IsoType.NUMERIC, df.format(new Date()), LOCAL_TIME_LENGTH));
        message.setField(EXPIRATION, new IsoValue<String>(IsoType.NUMERIC, expire, EXPIRATION_LENGTH));
        message.setField(POINT_OF_SERVICE, new IsoValue<String>(IsoType.ALPHA, pointOfService, POINT_OF_SERVICE_LENGTH));
        message.setField(FUNCTION_CODE, new IsoValue<String>(IsoType.NUMERIC, function, FUNCTION_CODE_LENGTH));
        message.setField(CARD_ACCEPTOR_BUSINESS_CODE,
                new IsoValue<String>(IsoType.NUMERIC, BUSINESS_CODE_FORMAT.format(getMerchant().getBusinessCode()),
                CARD_ACCEPTOR_BUSINESS_CODE_LENGTH));
        message.setField(ACQUIRER_REFERENCE, new IsoValue<String>(IsoType.LLVAR, getOrderId()));
        message.setField(APPROVAL_CODE, new IsoValue<String>(IsoType.ALPHA, approvalCode, APPROVAL_CODE_LENGTH));
        message.setField(ACTION_CODE, new IsoValue<String>(IsoType.NUMERIC, actionCode.getCode(), ACTION_CODE_LENGTH));
        message.setField(CARD_ACCEPTOR_TERMINAL_ID,
                new IsoValue<String>(IsoType.ALPHA, fillString(getTerminalId(), ' ', CARD_ACCEPTOR_TERMINAL_ID_LENGTH),
                CARD_ACCEPTOR_TERMINAL_ID_LENGTH));
        message.setField(CARD_ACCEPTOR_IDENTIFICATION_CODE,
                new IsoValue<String>(IsoType.ALPHA, getMerchant().getMerchantId(), CARD_ACCEPTOR_IDENTIFICATION_CODE_LENGTH));
        message.setField(CARD_ACCEPTOR_NAME_LOCATION, new IsoValue<String>(IsoType.LLVAR, address.toString(), CARD_ACCEPTOR_NAME_LOCATION_LENGTH));
        message.setField(ADDITIONAL_DATA_NATIONAL, new IsoValue<String>(IsoType.LLLVAR, "V503" + getCard().getCvd()));
        message.setField(CURRENCY_CODE, new IsoValue<String>(IsoType.ALPHA, getMoney().getCurrencyUnit().getCurrencyCode(), CURRENCY_CODE_LENGTH));
        message.setField(AUTH_ODE, new IsoValue<String>(IsoType.LLLVAR, getOde(), AUTH_ODE_LENGTH));
        return message;
    }

}
