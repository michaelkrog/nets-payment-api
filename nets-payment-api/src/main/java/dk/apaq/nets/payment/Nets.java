package dk.apaq.nets.payment;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.IsoValue;
import com.solab.iso8583.parse.AlphaParseInfo;
import com.solab.iso8583.parse.FieldParseInfo;
import com.solab.iso8583.parse.LllbinParseInfo;
import com.solab.iso8583.parse.LllvarParseInfo;
import com.solab.iso8583.parse.LlvarParseInfo;
import com.solab.iso8583.parse.NumericParseInfo;
import dk.apaq.nets.payment.io.Channel;
import dk.apaq.nets.payment.io.ChannelFactory;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.joda.money.Money;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author michael
 */
public class Nets {

    private static final Logger LOG = LoggerFactory.getLogger(Nets.class);
    private final ChannelFactory channelFactory;
    private final DateFormat df = new SimpleDateFormat("yyMMddHHmmss");
    private final NumberFormat expireFormat = NumberFormat.getIntegerInstance();
    private final NumberFormat cvdFormat = NumberFormat.getIntegerInstance();

    private abstract class Request<T> {

        protected Merchant merchant;
        protected Card card;
        protected Money money;
        protected String orderId;
        protected String ode, terminalId = "";

        public Request(Merchant merchant, Card card, Money money, String orderId) {
            Validate.notNull(merchant, "merchant must be specified");
            Validate.notNull(card, "card must be specified");
            Validate.notNull(money, "money must be specified");
            Validate.notNull(orderId, "orderId must be specified");

            this.merchant = merchant;
            this.card = card;
            this.money = money;
            this.orderId = orderId;
        }

        public T setOde(String ode) {
            this.ode = ode;
            return (T) this;
        }

        public T setTerminalId(String terminalId) {
            this.terminalId = terminalId;
            return (T) this;
        }

        public Card getCard() {
            return card;
        }

        public Merchant getMerchant() {
            return merchant;
        }

        public Money getMoney() {
            return money;
        }

        public String getOrderId() {
            return orderId;
        }

        public String getTerminalId() {
            return terminalId;
        }

        public String getOde() {
            return ode;
        }

        protected String fillString(String value, char character, int length) {
            StringBuilder sb = new StringBuilder(value);
            fillString(sb, character, length);
            return sb.toString();
        }

        protected void fillString(StringBuilder builder, char character, int length) {
            if (builder.length() > length) {
                builder.delete(length, builder.length());
            } else {
                for (int i = builder.length(); i < length; i++) {
                    builder.append(character);
                }
            }
        }

        protected String buildAddressField() {
            StringBuilder address = new StringBuilder();
            address.append(merchant.getName());
            address.append("\\");
            address.append(merchant.getAddress().getStreet());
            address.append("\\");
            address.append(merchant.getAddress().getCity());
            address.append("\\");

            address.append(merchant.getAddress().getPostalCode());
            fillString(address, ' ', 96);

            address.append(merchant.getAddress().getCountryCode());
            return address.toString();
        }

        protected abstract IsoMessage buildMessage();

        public NetsResponse send() throws IOException {
            IsoMessage message = buildMessage();
            Channel channel = channelFactory.createChannel();
            message = channel.sendMessage(message);

            if (message == null) {
                throw new IOException("Message could not be parsed. Unknown message type?");
            }

            String actionCodeString = message.getField(MessageFields.FIELD_INDEX_ACTION_CODE).toString();
            String ode = message.getField(MessageFields.FIELD_INDEX_AUTH_ODE).toString();

            //TODO Do something with the message and return meaningfull stuff
            return new NetsResponse(ActionCode.fromCode(actionCodeString), ode);
        }
    }

    public class AuthorizeRequest extends Request<AuthorizeRequest> {

        private boolean recurring, estimatedAmount, gambling, fraudSuspect;

        private AuthorizeRequest(Merchant merchant, Card card, Money money, String orderId) {
            super(merchant, card, money, orderId);
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
            String reason = fraudSuspect ? "1511" : "0000";
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

    public class ReverseRequest extends Request<ReverseRequest> {

        private String approvalCode;

        private ReverseRequest(Merchant merchant, Card card, Money money, String orderId) {
            super(merchant, card, money, orderId);
        }

        public ReverseRequest setApprovalCode(String approvalCode) {
            this.approvalCode = approvalCode;
            return this;
        }

        @Override
        protected IsoMessage buildMessage() {
            IsoMessage message = channelFactory.getMessageFactory().newMessage(MessageTypes.REVERSAL_ADVICE_REQUEST);
            message.setIsoHeader(PsipHeader.OK.toString());

            String function = FunctionCode.Reverse_FullReversal.getCode();
            String reason = MessageReason.CustomerCancellation.getCode();

            String address = buildAddressField();

            message.setField(MessageFields.FIELD_INDEX_PRIMARY_ACCOUNT_NUMBER, new IsoValue<String>(IsoType.LLVAR, card.getCardNumber()));
            message.setField(MessageFields.FIELD_INDEX_PROCESSING_CODE, new IsoValue<Integer>(IsoType.NUMERIC, 000000, 6));
            message.setField(MessageFields.FIELD_INDEX_AMOUNT, new IsoValue<Integer>(IsoType.NUMERIC, money.getAmountMinorInt(), 12));
            message.setField(MessageFields.FIELD_INDEX_LOCAL_TIME, new IsoValue<String>(IsoType.NUMERIC, df.format(new Date()), 12));
            message.setField(MessageFields.FIELD_INDEX_FUNCTION_CODE, new IsoValue<String>(IsoType.NUMERIC, function, 3));
            message.setField(MessageFields.FIELD_INDEX_MESSAGE_REASON_CODE, new IsoValue<String>(IsoType.NUMERIC, reason, 4));
            message.setField(MessageFields.FIELD_INDEX_CARD_ACCEPTOR_BUSINESS_CODE, new IsoValue<String>(IsoType.NUMERIC, "0000", 4));
            message.setField(MessageFields.FIELD_INDEX_ACQUIRER_REFERENCE, new IsoValue<String>(IsoType.LLVAR, orderId));
            message.setField(MessageFields.FIELD_INDEX_APPROVAL_CODE, new IsoValue<String>(IsoType.ALPHA, approvalCode, 6));
            message.setField(MessageFields.FIELD_INDEX_CARD_ACCEPTOR_TERMINAL_ID, new IsoValue<String>(IsoType.ALPHA, fillString(terminalId, ' ', 8), 8));
            message.setField(MessageFields.FIELD_INDEX_CARD_ACCEPTOR_IDENTIFICATION_CODE, new IsoValue<String>(IsoType.ALPHA, merchant.getMerchantId(), 15));
            message.setField(MessageFields.FIELD_INDEX_CARD_ACCEPTOR_NAME_LOCATION, new IsoValue<String>(IsoType.LLVAR, address, 99));
            message.setField(MessageFields.FIELD_INDEX_CURRENCY_CODE, new IsoValue<String>(IsoType.ALPHA, money.getCurrencyUnit().getCurrencyCode(), 3));
            message.setField(MessageFields.FIELD_INDEX_AUTH_ODE, new IsoValue<String>(IsoType.LLLVAR, ode, 255));
            return message;
        }
    }

    public class CaptureRequest extends Request<CaptureRequest> {

        private boolean recurring, amountDiffers, gambling;
        private ActionCode actionCode;
        private String approvalCode;

        private CaptureRequest(Merchant merchant, Card card, Money money, String orderId) {
            super(merchant, card, money, orderId);
        }

        public CaptureRequest setRecurring(boolean recurring) {
            this.recurring = recurring;
            return this;
        }

        public boolean isRecurring() {
            return recurring;
        }

        public CaptureRequest setAmountDiffers(boolean amountDiffers) {
            this.amountDiffers = amountDiffers;
            return this;
        }

        public boolean isAmountDiffers() {
            return amountDiffers;
        }

        public CaptureRequest setActionCode(ActionCode actionCode) {
            this.actionCode = actionCode;
            return this;
        }

        public ActionCode getActionCode() {
            return actionCode;
        }
        
        public CaptureRequest setApprovalCode(String approvalCode) {
            this.approvalCode = approvalCode;
            return this;
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
            IsoMessage message = channelFactory.getMessageFactory().newMessage(MessageTypes.AUTHORIZATION_REQUEST);
            message.setIsoHeader(PsipHeader.OK.toString());

            String expire = expireFormat.format(card.getExpireYear()) + expireFormat.format(card.getExpireMonth());
            String processingCode = gambling ? ProcessingCode.QuasiCash.getCode() : ProcessingCode.GoodsAndServices.getCode();
            String pointOfService = recurring ? PointOfService.InternetMerchantRecurring.getCode() : PointOfService.InternetMerchant.getCode();
            String function = amountDiffers ? FunctionCode.Capture_Amount_Differs.getCode() : FunctionCode.Capture_Amount_Accurate.getCode();

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

    private void init() {
        LOG.info("Initializing Nets instance.");

        Map<Integer, FieldParseInfo> authRespFields = new HashMap<Integer, FieldParseInfo>();
        authRespFields.put(MessageFields.FIELD_INDEX_PRIMARY_ACCOUNT_NUMBER, new LlvarParseInfo());
        authRespFields.put(MessageFields.FIELD_INDEX_PROCESSING_CODE, new NumericParseInfo(6));
        authRespFields.put(MessageFields.FIELD_INDEX_AMOUNT, new NumericParseInfo(12));
        authRespFields.put(MessageFields.FIELD_INDEX_LOCAL_TIME, new NumericParseInfo(12));
        authRespFields.put(MessageFields.FIELD_INDEX_ACQUIRER_REFERENCE, new LlvarParseInfo());
        authRespFields.put(MessageFields.FIELD_INDEX_APPROVAL_CODE, new AlphaParseInfo(6));
        authRespFields.put(MessageFields.FIELD_INDEX_ACTION_CODE, new NumericParseInfo(3));
        authRespFields.put(MessageFields.FIELD_INDEX_CARD_ACCEPTOR_TERMINAL_ID, new AlphaParseInfo(8));
        authRespFields.put(MessageFields.FIELD_INDEX_CARD_ACCEPTOR_IDENTIFICATION_CODE, new AlphaParseInfo(15));
        authRespFields.put(MessageFields.FIELD_INDEX_ADDITIONAL_RESPONSE_DATA, new LlvarParseInfo());
        authRespFields.put(MessageFields.FIELD_INDEX_ADDITIONAL_DATA_NATIONAL, new LllvarParseInfo());
        authRespFields.put(MessageFields.FIELD_INDEX_CURRENCY_CODE, new AlphaParseInfo(3));
        authRespFields.put(MessageFields.FIELD_INDEX_AUTH_ODE, new LllvarParseInfo());

        channelFactory.getMessageFactory().setParseMap(MessageTypes.AUTHORIZATION_RESPONSE, authRespFields);

        Map<Integer, FieldParseInfo> reverseRespFields = new HashMap<Integer, FieldParseInfo>();
        reverseRespFields.put(MessageFields.FIELD_INDEX_PRIMARY_ACCOUNT_NUMBER, new LlvarParseInfo());
        reverseRespFields.put(MessageFields.FIELD_INDEX_PROCESSING_CODE, new NumericParseInfo(6));
        reverseRespFields.put(MessageFields.FIELD_INDEX_AMOUNT, new NumericParseInfo(12));
        reverseRespFields.put(MessageFields.FIELD_INDEX_LOCAL_TIME, new NumericParseInfo(12));
        reverseRespFields.put(MessageFields.FIELD_INDEX_ACQUIRER_REFERENCE, new LlvarParseInfo());
        reverseRespFields.put(MessageFields.FIELD_INDEX_ACTION_CODE, new NumericParseInfo(3));
        reverseRespFields.put(MessageFields.FIELD_INDEX_CARD_ACCEPTOR_TERMINAL_ID, new AlphaParseInfo(8));
        reverseRespFields.put(MessageFields.FIELD_INDEX_CARD_ACCEPTOR_IDENTIFICATION_CODE, new AlphaParseInfo(15));
        reverseRespFields.put(MessageFields.FIELD_INDEX_CURRENCY_CODE, new AlphaParseInfo(3));
        reverseRespFields.put(MessageFields.FIELD_INDEX_AUTH_ODE, new LllvarParseInfo());

        channelFactory.getMessageFactory().setParseMap(MessageTypes.REVERSAL_ADVICE_RESPONSE, reverseRespFields);

        LOG.info("Nets instance initialized.");

    }

    public Nets(ChannelFactory channelFactory) throws MalformedURLException {
        this.channelFactory = channelFactory;

        expireFormat.setMinimumIntegerDigits(2);
        expireFormat.setMaximumIntegerDigits(2);
        cvdFormat.setMinimumIntegerDigits(3);
        cvdFormat.setMaximumIntegerDigits(3);

        init();
    }

    public AuthorizeRequest authorize(Merchant merchant, Card card, Money money, String orderId) throws IOException {
        return new AuthorizeRequest(merchant, card, money, orderId);
    }

    public CaptureRequest capture(Merchant merchant, Card card, Money money, String orderId, boolean recurring, String terminalId, String approvalCode, ActionCode actionCode, boolean amountDiffers) throws IOException {
        return new CaptureRequest(merchant, card, money, orderId);
    }

    public ReverseRequest reverse(Merchant merchant, Card card, Money money, String orderId) throws IOException {
        return new ReverseRequest(merchant, card, money, orderId);
    }
}
