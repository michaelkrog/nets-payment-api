package dk.apaq.nets.payment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.IsoValue;
import dk.apaq.framework.common.beans.finance.Card;
import dk.apaq.nets.payment.io.ChannelFactory;
import org.joda.money.Money;

import static dk.apaq.nets.payment.MessageFields.*;

/**
 *
 * Request used for authorization.
 *
 */
public final class AuthorizeRequest extends AbstractNetsRequest<AuthorizeRequest> {

    private boolean recurring;
    private boolean estimatedAmount;
    private boolean gambling;
    private boolean fraudSuspect;

    AuthorizeRequest(Merchant merchant, Card card, Money money, String orderId, ChannelFactory channelFactory) {
        super(merchant, card, money, orderId, channelFactory);

    }

    /**
     * Set whether the amount is estimated amount.
     * @param value The estimated amount this request should try to authenticate.
     * @return The request instance.
     */
    public AuthorizeRequest setEstimatedAmount(boolean value) {
        this.estimatedAmount = value;
        return this;
    }

    /**
     * Sets whether fraud is suspected.
     * @param value True/False.
     * @return The request instance.
     */
    public AuthorizeRequest setFraudSuspect(boolean value) {
        this.fraudSuspect = value;
        return this;
    }

    /**
     * Sets whether this is a request related to gambling.
     * @param value True/False
     * @return This request instance.
     */
    public AuthorizeRequest setGambling(boolean value) {
        this.gambling = value;
        return this;
    }

    /**
     * Sets whether this requesrt is for a recurring payment.
     * @param value True/False
     * @return This request instance.
     */
    public AuthorizeRequest setRecurring(boolean value) {
        this.recurring = value;
        return this;
    }

    /**
     * Whether the amount is estimated.
     * @return True/False
     */
    public boolean isEstimatedAmount() {
        return estimatedAmount;
    }

    /**
     * Whether fraud is suspected.
     * @return True/False
     */
    public boolean isFraudSuspect() {
        return fraudSuspect;
    }

    /**
     * Whether the request i related to gambling.
     * @return True/False
     */
    public boolean isGambling() {
        return gambling;
    }

    /**
     * Whether the request is for a recurring payment.
     * @return True/False
     */
    public boolean isRecurring() {
        return recurring;
    }

    @Override
    protected IsoMessage buildMessage() {
        DateFormat df = new SimpleDateFormat(DATE_FORMAT);
        IsoMessage message = getChannelFactory().getMessageFactory().newMessage(MessageTypes.AUTHORIZATION_REQUEST);
        message.setIsoHeader(PsipHeader.OK.toString());
        String expire = EXPIRE_FORMAT.format(getCard().getExpireYear()) + EXPIRE_FORMAT.format(getCard().getExpireMonth());
        String processingCode = gambling ? ProcessingCode.QuasiCash.getCode() : ProcessingCode.GoodsAndServices.getCode();
        String pointOfService = recurring ? PointOfService.InternetMerchantRecurring.getCode() : PointOfService.InternetMerchant.getCode();
        String function = estimatedAmount ? FunctionCode.Authorize_Original_Estimated_Amount.getCode()
                : FunctionCode.Authorize_Original_Accurate_Amount.getCode();
        String reason = fraudSuspect ? MessageReason.SuspeciousOfFraud.getCode() : MessageReason.NormalTransaction.getCode();
        String address = buildAddressField();

        message.setField(PRIMARY_ACCOUNT_NUMBER, new IsoValue<String>(IsoType.LLVAR, getCard().getCardNumber()));
        message.setField(PROCESSING_CODE, new IsoValue<String>(IsoType.NUMERIC, processingCode, PROCESSING_CODE_LENGTH));
        message.setField(AMOUNT, new IsoValue<Integer>(IsoType.NUMERIC, getMoney().getAmountMinorInt(), AMOUNT_LENGTH));
        message.setField(LOCAL_TIME, new IsoValue<String>(IsoType.NUMERIC, df.format(new Date()), LOCAL_TIME_LENGTH));
        message.setField(EXPIRATION, new IsoValue<String>(IsoType.NUMERIC, expire, EXPIRATION_LENGTH));
        message.setField(POINT_OF_SERVICE, new IsoValue<String>(IsoType.ALPHA, pointOfService, POINT_OF_SERVICE_LENGTH));
        message.setField(FUNCTION_CODE, new IsoValue<String>(IsoType.NUMERIC, function, FUNCTION_CODE_LENGTH));
        message.setField(MESSAGE_REASON_CODE, new IsoValue<String>(IsoType.NUMERIC, reason, MESSAGE_REASON_CODE_LENGTH));
        message.setField(CARD_ACCEPTOR_BUSINESS_CODE,
                new IsoValue<String>(IsoType.NUMERIC, BUSINESS_CODE_FORMAT.format(getMerchant().getBusinessCode()), 
                CARD_ACCEPTOR_BUSINESS_CODE_LENGTH));
        message.setField(ACQUIRER_REFERENCE, new IsoValue<String>(IsoType.LLVAR, getOrderId()));
        message.setField(CARD_ACCEPTOR_TERMINAL_ID,
                new IsoValue<String>(IsoType.ALPHA, fillString(getTerminalId(), ' ', CARD_ACCEPTOR_TERMINAL_ID_LENGTH), 
                CARD_ACCEPTOR_TERMINAL_ID_LENGTH));
        message.setField(CARD_ACCEPTOR_IDENTIFICATION_CODE,
                new IsoValue<String>(IsoType.ALPHA, getMerchant().getMerchantId(), CARD_ACCEPTOR_IDENTIFICATION_CODE_LENGTH));
        message.setField(CARD_ACCEPTOR_NAME_LOCATION, new IsoValue<String>(IsoType.LLVAR, address, CARD_ACCEPTOR_NAME_LOCATION_LENGTH));
        message.setField(ADDITIONAL_DATA_NATIONAL, new IsoValue<String>(IsoType.LLLVAR, "V503" + getCard().getCvd()));
        message.setField(CURRENCY_CODE, new IsoValue<String>(IsoType.ALPHA, getMoney().getCurrencyUnit().getCurrencyCode(), CURRENCY_CODE_LENGTH));
        return message;
    }
}
