package dk.apaq.nets.payment;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.IsoValue;
import dk.apaq.nets.payment.io.ChannelFactory;
import java.util.Date;
import org.joda.money.Money;

/**
 *
 * @author michael
 */
public class AuthorizeRequest extends Request<AuthorizeRequest> {
    private boolean recurring;
    private boolean estimatedAmount;
    private boolean gambling;
    private boolean fraudSuspect;
    
    AuthorizeRequest(Merchant merchant, Card card, Money money, String orderId, ChannelFactory channelFactory) {
        super(merchant, card, money, orderId, channelFactory);
        
    }

    public AuthorizeRequest setEstimatedAmount(boolean estimatedAmount) {
        this.estimatedAmount = estimatedAmount;
        return this;
    }

    public AuthorizeRequest setFraudSuspect(boolean fraudSuspect) {
        this.fraudSuspect = fraudSuspect;
        return this;
    }

    public AuthorizeRequest setGambling(boolean gambling) {
        this.gambling = gambling;
        return this;
    }

    public AuthorizeRequest setRecurring(boolean recurring) {
        this.recurring = recurring;
        return this;
    }

    public boolean isEstimatedAmount() {
        return estimatedAmount;
    }

    public boolean isFraudSuspect() {
        return fraudSuspect;
    }

    public boolean isGambling() {
        return gambling;
    }

    public boolean isRecurring() {
        return recurring;
    }

    @Override
    protected IsoMessage buildMessage() {
        IsoMessage message = channelFactory.getMessageFactory().newMessage(MessageTypes.AUTHORIZATION_REQUEST);
        message.setIsoHeader(PsipHeader.OK.toString());
        String expire = expireFormat.format(card.getExpireYear()) + expireFormat.format(card.getExpireMonth());
        String processingCode = gambling ? ProcessingCode.QuasiCash.getCode() : ProcessingCode.GoodsAndServices.getCode();
        String pointOfService = recurring ? PointOfService.InternetMerchantRecurring.getCode() : PointOfService.InternetMerchant.getCode();
        String function = estimatedAmount ? FunctionCode.Authorize_Original_Estimated_Amount.getCode() : FunctionCode.Authorize_Original_Accurate_Amount.getCode();
        String reason = fraudSuspect ? MessageReason.SuspeciousOfFraud.getCode() : MessageReason.NormalTransaction.getCode();
        String address = buildAddressField();
        message.setField(2, new IsoValue<String>(IsoType.LLVAR, card.getCardNumber()));
        message.setField(3, new IsoValue<String>(IsoType.NUMERIC, processingCode, 6));
        message.setField(4, new IsoValue<Integer>(IsoType.NUMERIC, money.getAmountMinorInt(), 12));
        message.setField(12, new IsoValue<String>(IsoType.NUMERIC, df.format(new Date()), 12));
        message.setField(14, new IsoValue<String>(IsoType.NUMERIC, expire, 4));
        message.setField(22, new IsoValue<String>(IsoType.ALPHA, pointOfService, 12));
        message.setField(24, new IsoValue<String>(IsoType.NUMERIC, function, 3));
        message.setField(25, new IsoValue<String>(IsoType.NUMERIC, reason, 4));
        message.setField(26, new IsoValue<String>(IsoType.NUMERIC, "0000", 4));
        message.setField(31, new IsoValue<String>(IsoType.LLVAR, orderId));
        message.setField(41, new IsoValue<String>(IsoType.ALPHA, fillString(terminalId, ' ', 8), 8));
        message.setField(42, new IsoValue<String>(IsoType.ALPHA, merchant.getMerchantId(), 15));
        message.setField(43, new IsoValue<String>(IsoType.LLVAR, address, 99));
        message.setField(47, new IsoValue<String>(IsoType.LLLVAR, "V503" + card.getCvd()));
        message.setField(49, new IsoValue<String>(IsoType.ALPHA, money.getCurrencyUnit().getCurrencyCode(), 3));
        return message;
    }
    
}
