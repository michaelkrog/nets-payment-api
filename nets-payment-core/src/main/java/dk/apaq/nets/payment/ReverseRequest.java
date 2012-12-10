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
 * A Request for Reversing and Auhorization.
 */
public final class ReverseRequest extends AbstractNetsRequest<ReverseRequest> {
    private final String approvalCode;
    private final String processingCode;
    private boolean fraudSuspected;
    private boolean malfunctionSuspected;
    
    /**
     * Constructs a new ReverseRequest.
     * @param merchant The Merchant.
     * @param card The card used for Authorization.
     * @param money The amount equal to the amount originally authorized.
     * @param orderId The orderid for which the amount was authorized.
     * @param ode The ODE returned in Authorization response.
     * @param processingCode The processing code.
     * @param approvalCode The approval code.
     * @param channelFactory  The channel factory.
     */
    public ReverseRequest(Merchant merchant, Card card, Money money, String orderId, String ode, String processingCode, String approvalCode, 
            ChannelFactory channelFactory) {
        super(merchant, card, money, orderId, ode, channelFactory);
        this.approvalCode = approvalCode;
        this.processingCode = processingCode;
    }

    /**
     * Whether fraud is suspected.
     * @return True/False
     */
    public boolean isFraudSuspected() {
        return fraudSuspected;
    }

    /**
     * Whether malfunction is suspected.
     * @return True/False
     */
    public boolean isMalfunctionSuspected() {
        return malfunctionSuspected;
    }

    /**
     * Whether malfuncation is suspected.
     * @param value True/False
     * @return This request instance.
     */
    public ReverseRequest setMalfunctionSuspected(boolean value) {
        this.malfunctionSuspected = value;
        return this;
    }

    /**
     * Whether fraud is suspected.
     * @param value True/False
     * @return This request instance.
     */
    public ReverseRequest setFraudSuspected(boolean value) {
        this.fraudSuspected = value;
        return this;
    }

    @Override
    protected IsoMessage buildMessage() {
        DateFormat df = new SimpleDateFormat(DATE_FORMAT);
        IsoMessage message = getChannelFactory().getMessageFactory().newMessage(MessageTypes.REVERSAL_ADVICE_REQUEST);
        message.setIsoHeader(PsipHeader.OK.toString());
        String function = FunctionCode.Reverse_FullReversal.getCode();
        String reason = MessageReason.CustomerCancellation.getCode();
        if (fraudSuspected) {
            reason = MessageReason.SuspectedFraud.getCode();
        }
        if (malfunctionSuspected) {
            reason = MessageReason.SuspectedMalfunction.getCode();
        }
        String address = buildAddressField();
        message.setField(PRIMARY_ACCOUNT_NUMBER, new IsoValue<String>(IsoType.LLVAR, getCard().getCardNumber()));
        message.setField(PROCESSING_CODE, new IsoValue<String>(IsoType.NUMERIC, processingCode, PROCESSING_CODE_LENGTH));
        message.setField(AMOUNT, new IsoValue<Integer>(IsoType.NUMERIC, getMoney().getAmountMinorInt(), AMOUNT_LENGTH));
        message.setField(LOCAL_TIME, new IsoValue<String>(IsoType.NUMERIC, df.format(new Date()), LOCAL_TIME_LENGTH));
        message.setField(FUNCTION_CODE, new IsoValue<String>(IsoType.NUMERIC, function, FUNCTION_CODE_LENGTH));
        message.setField(MESSAGE_REASON_CODE, new IsoValue<String>(IsoType.NUMERIC, reason, MESSAGE_REASON_CODE_LENGTH));
        message.setField(CARD_ACCEPTOR_BUSINESS_CODE,
                new IsoValue<String>(IsoType.NUMERIC, BUSINESS_CODE_FORMAT.format(getMerchant().getBusinessCode()),
                CARD_ACCEPTOR_BUSINESS_CODE_LENGTH));
        message.setField(ACQUIRER_REFERENCE, new IsoValue<String>(IsoType.LLVAR, getOrderId()));
        message.setField(APPROVAL_CODE, new IsoValue<String>(IsoType.ALPHA, approvalCode, APPROVAL_CODE_LENGTH));
        message.setField(CARD_ACCEPTOR_TERMINAL_ID,
                new IsoValue<String>(IsoType.ALPHA, fillString(getTerminalId(), ' ', CARD_ACCEPTOR_TERMINAL_ID_LENGTH),
                CARD_ACCEPTOR_TERMINAL_ID_LENGTH));
        message.setField(CARD_ACCEPTOR_IDENTIFICATION_CODE,
                new IsoValue<String>(IsoType.ALPHA, getMerchant().getMerchantId(), CARD_ACCEPTOR_IDENTIFICATION_CODE_LENGTH));
        message.setField(CARD_ACCEPTOR_NAME_LOCATION, new IsoValue<String>(IsoType.LLVAR, address, CARD_ACCEPTOR_NAME_LOCATION_LENGTH));
        message.setField(CURRENCY_CODE, new IsoValue<String>(IsoType.ALPHA, getMoney().getCurrencyUnit().getCurrencyCode(), CURRENCY_CODE_LENGTH));
        message.setField(AUTH_ODE, new IsoValue<String>(IsoType.LLLVAR, getOde(), AUTH_ODE_LENGTH));
        return message;
    }

}
