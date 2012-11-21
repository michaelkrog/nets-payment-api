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
public class CaptureRequest extends AbstractNetsRequest<CaptureRequest> {
    private boolean recurring;
    private boolean amountDiffers;
    private boolean gambling;
    private boolean refund;
    private ActionCode actionCode;
    private String approvalCode;

    public CaptureRequest(Merchant merchant, Card card, Money money, String orderId, String ode, String approvalCode, ActionCode actionCode, ChannelFactory channelFactory) {
        super(merchant, card, money, orderId, ode, channelFactory);
        this.actionCode = actionCode;
        this.approvalCode = approvalCode;
    }

    public CaptureRequest setRecurring(boolean recurring) {
        this.recurring = recurring;
        return this;
    }

    public boolean isRecurring() {
        return recurring;
    }

    public boolean isRefund() {
        return refund;
    }

    public CaptureRequest setRefund(boolean refund) {
        this.refund = refund;
        return this;
    }

    public CaptureRequest setAmountDiffers(boolean amountDiffers) {
        this.amountDiffers = amountDiffers;
        return this;
    }

    public boolean isAmountDiffers() {
        return amountDiffers;
    }

    public ActionCode getActionCode() {
        return actionCode;
    }

    public String getApprovalCode() {
        return approvalCode;
    }

    public CaptureRequest setGambling(boolean gambling) {
        this.gambling = gambling;
        return this;
    }

    public boolean isGambling() {
        return gambling;
    }

    @Override
    protected IsoMessage buildMessage() {
        IsoMessage message = channelFactory.getMessageFactory().newMessage(MessageTypes.CAPTURE_REQUEST);
        message.setIsoHeader(PsipHeader.OK.toString());
        String expire = expireFormat.format(card.getExpireYear()) + expireFormat.format(card.getExpireMonth());
        String pointOfService = recurring ? PointOfService.InternetMerchantRecurring.getCode() : PointOfService.InternetMerchant.getCode();
        String processingCode = null;
        String function = null;
        if (refund) {
            processingCode = ProcessingCode.GoodsAndServiceCredit.getCode();
            function = FunctionCode.Capture_Original_Transaction.getCode();
        } else {
            processingCode = gambling ? ProcessingCode.QuasiCash.getCode() : ProcessingCode.GoodsAndServices.getCode();
            function = amountDiffers ? FunctionCode.Capture_Amount_Differs.getCode() : FunctionCode.Capture_Amount_Accurate.getCode();
        }
        String address = buildAddressField();
        message.setField(2, new IsoValue<String>(IsoType.LLVAR, card.getCardNumber()));
        message.setField(3, new IsoValue<String>(IsoType.NUMERIC, processingCode, 6));
        message.setField(4, new IsoValue<Integer>(IsoType.NUMERIC, money.getAmountMinorInt(), 12));
        message.setField(12, new IsoValue<String>(IsoType.NUMERIC, df.format(new Date()), 12));
        message.setField(14, new IsoValue<String>(IsoType.NUMERIC, expire, 4));
        message.setField(22, new IsoValue<String>(IsoType.ALPHA, pointOfService, 12));
        message.setField(24, new IsoValue<String>(IsoType.NUMERIC, function, 3));
        message.setField(26, new IsoValue<String>(IsoType.NUMERIC, "0000", 4));
        message.setField(31, new IsoValue<String>(IsoType.LLVAR, orderId));
        message.setField(38, new IsoValue<String>(IsoType.ALPHA, approvalCode, 6));
        message.setField(39, new IsoValue<String>(IsoType.NUMERIC, actionCode.getCode(), 3));
        message.setField(41, new IsoValue<String>(IsoType.ALPHA, fillString(terminalId, ' ', 8), 8));
        message.setField(42, new IsoValue<String>(IsoType.ALPHA, merchant.getMerchantId(), 15));
        message.setField(43, new IsoValue<String>(IsoType.LLVAR, address.toString(), 99));
        message.setField(47, new IsoValue<String>(IsoType.LLLVAR, "V503" + card.getCvd()));
        message.setField(49, new IsoValue<String>(IsoType.ALPHA, money.getCurrencyUnit().getCurrencyCode(), 3));
        message.setField(56, new IsoValue<String>(IsoType.LLLVAR, ode, 255));
        return message;
    }

}
