/*
 * Copyright by Apaq 2011-2013
 */

package dk.apaq.nets.payment;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.IsoValue;
import dk.apaq.nets.payment.io.ChannelFactory;
import java.util.Date;
import org.joda.money.Money;

/**
 * Javadoc
 */
public class ReverseRequest extends AbstractNetsRequest<ReverseRequest> {
    private String approvalCode;
    private boolean fraudSuspected;
    private boolean malfunctionSuspected;

    public ReverseRequest(Merchant merchant, Card card, Money money, String orderId, String ode, String approvalCode, ChannelFactory channelFactory) {
        super(merchant, card, money, orderId, ode, channelFactory);
        this.approvalCode = approvalCode;
    }

    public boolean isFraudSuspected() {
        return fraudSuspected;
    }

    public boolean isMalfunctionSuspected() {
        return malfunctionSuspected;
    }

    public ReverseRequest setMalfunctionSuspected(boolean malfunctionSuspected) {
        this.malfunctionSuspected = malfunctionSuspected;
        return this;
    }

    public ReverseRequest setFraudSuspected(boolean fraudSuspected) {
        this.fraudSuspected = fraudSuspected;
        return this;
    }

    @Override
    protected IsoMessage buildMessage() {
        IsoMessage message = channelFactory.getMessageFactory().newMessage(MessageTypes.REVERSAL_ADVICE_REQUEST);
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
        message.setField(MessageFields.PRIMARY_ACCOUNT_NUMBER, new IsoValue<String>(IsoType.LLVAR, card.getCardNumber()));
        message.setField(MessageFields.PROCESSING_CODE, new IsoValue<Integer>(IsoType.NUMERIC, 000000, 6));
        message.setField(MessageFields.AMOUNT, new IsoValue<Integer>(IsoType.NUMERIC, money.getAmountMinorInt(), 12));
        message.setField(MessageFields.LOCAL_TIME, new IsoValue<String>(IsoType.NUMERIC, df.format(new Date()), 12));
        message.setField(MessageFields.FUNCTION_CODE, new IsoValue<String>(IsoType.NUMERIC, function, 3));
        message.setField(MessageFields.MESSAGE_REASON_CODE, new IsoValue<String>(IsoType.NUMERIC, reason, 4));
        message.setField(MessageFields.CARD_ACCEPTOR_BUSINESS_CODE, new IsoValue<String>(IsoType.NUMERIC, "0000", 4));
        message.setField(MessageFields.ACQUIRER_REFERENCE, new IsoValue<String>(IsoType.LLVAR, orderId));
        message.setField(MessageFields.APPROVAL_CODE, new IsoValue<String>(IsoType.ALPHA, approvalCode, 6));
        message.setField(MessageFields.CARD_ACCEPTOR_TERMINAL_ID, new IsoValue<String>(IsoType.ALPHA, fillString(terminalId, ' ', 8), 8));
        message.setField(MessageFields.CARD_ACCEPTOR_IDENTIFICATION_CODE, new IsoValue<String>(IsoType.ALPHA, merchant.getMerchantId(), 15));
        message.setField(MessageFields.CARD_ACCEPTOR_NAME_LOCATION, new IsoValue<String>(IsoType.LLVAR, address, 99));
        message.setField(MessageFields.CURRENCY_CODE, new IsoValue<String>(IsoType.ALPHA, money.getCurrencyUnit().getCurrencyCode(), 3));
        message.setField(MessageFields.AUTH_ODE, new IsoValue<String>(IsoType.LLLVAR, ode, 255));
        return message;
    }

}
