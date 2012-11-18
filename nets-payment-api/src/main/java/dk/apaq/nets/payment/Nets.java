package dk.apaq.nets.payment;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.IsoValue;
import com.solab.iso8583.parse.AlphaParseInfo;
import com.solab.iso8583.parse.FieldParseInfo;
import com.solab.iso8583.parse.LllvarParseInfo;
import com.solab.iso8583.parse.LlvarParseInfo;
import com.solab.iso8583.parse.NumericParseInfo;
import dk.apaq.framework.repository.Repository;
import dk.apaq.nets.payment.io.Channel;
import dk.apaq.nets.payment.io.ChannelFactory;
import java.io.IOException;
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
    private static final int DEFAULT_MAX_ATTEMPTS_PER_REQUEST = 5;
    private static final int DEFAULT_MIN_WAIT_BETWEEN_ATTEMPTS = 60000;
    private final ChannelFactory channelFactory;
    private final DateFormat df = new SimpleDateFormat("yyMMddHHmmss");
    private final NumberFormat expireFormat = NumberFormat.getIntegerInstance();
    private final NumberFormat cvdFormat = NumberFormat.getIntegerInstance();
    private final Repository<TransactionData, String> repository;
    
    private int maxRequestAttempts = DEFAULT_MAX_ATTEMPTS_PER_REQUEST;
    private int minWaitBetweenAttempts = DEFAULT_MIN_WAIT_BETWEEN_ATTEMPTS;

    private abstract class Request<T> {

        protected Merchant merchant;
        protected Card card;
        protected Money money;
        protected String orderId;
        protected String ode, terminalId = "";

        public Request(Merchant merchant, Card card, Money money, String orderId) {
            this(merchant, card, money, orderId, null);
        }

        public Request(Merchant merchant, Card card, Money money, String orderId, String ode) {
            Validate.notNull(merchant, "merchant must be specified");
            Validate.notNull(card, "card must be specified");
            Validate.notNull(money, "money must be specified");
            Validate.notNull(orderId, "orderId must be specified");

            this.merchant = merchant;
            this.card = card;
            this.money = money;
            this.orderId = orderId;
            this.ode = ode;
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
            IsoMessage request = buildMessage();
            IsoMessage response = null;
            
            int attempts = 0;
            boolean doAttempt = true;
            long start = System.currentTimeMillis();
            
            while (doAttempt) {
                doAttempt = false;
                Channel channel = channelFactory.createChannel();
                attempts++;
                try {
                    response = channel.sendMessage(request);
                    if (response == null) {
                        return new NetsResponse(ActionCode.Function_Not_Supported, null);
                    }
                } catch (IOException ex) { 
                    doAttempt = response == null && attempts < maxRequestAttempts;
                    
                    //Error occurred - if we are gonna try again then sleep a little first according to Nets requirements.
                    if(doAttempt) {
                        long timeTillNextRequest = Math.max(0, minWaitBetweenAttempts - (System.currentTimeMillis() - start));
                        try { Thread.sleep(timeTillNextRequest); } catch (InterruptedException ex2) { /* EMPTY */ }
                    }
                }
            }

            if (response == null) {
                throw new IOException("Unable to communicate with Nets.");
            }
            
            String actionCodeString = response.getField(MessageFields.ACTION_CODE).toString();
            String newOde = response.getField(MessageFields.AUTH_ODE).toString();
            String approvalCode = null;

            if (response.hasField(MessageFields.APPROVAL_CODE)) {
                approvalCode = response.getField(MessageFields.APPROVAL_CODE).toString();
            }

            return new NetsResponse(ActionCode.fromCode(actionCodeString), newOde, approvalCode);
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

    public class ReverseRequest extends Request<ReverseRequest> {

        private String approvalCode;
        private boolean fraudSuspected;
        private boolean malfunctionSuspected;

        private ReverseRequest(Merchant merchant, Card card, Money money, String orderId, String ode, String approvalCode) {
            super(merchant, card, money, orderId, ode);
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

    public class CaptureRequest extends Request<CaptureRequest> {

        private boolean recurring, amountDiffers, gambling, refund;
        private ActionCode actionCode;
        private String approvalCode;

        private CaptureRequest(Merchant merchant, Card card, Money money, String orderId, String ode, String approvalCode, ActionCode actionCode) {
            super(merchant, card, money, orderId, ode);
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

            if(refund) {
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

    private class NetsResponse {

        private ActionCode actionCode;
        private String ode;
        private String approvalCode;

        public NetsResponse(ActionCode actionCode, String ode) {
            this.actionCode = actionCode;
            this.ode = ode;
        }

        public NetsResponse(ActionCode actionCode, String ode, String approvalCode) {
            this.actionCode = actionCode;
            this.ode = ode;
            this.approvalCode = approvalCode;
        }

        public ActionCode getActionCode() {
            return actionCode;
        }

        public String getOde() {
            return ode;
        }

        public String getApprovalCode() {
            return approvalCode;
        }
    }

    private void init() {
        LOG.info("Initializing Nets instance.");

        Map<Integer, FieldParseInfo> authRespFields = new HashMap<Integer, FieldParseInfo>();
        authRespFields.put(MessageFields.PRIMARY_ACCOUNT_NUMBER, new LlvarParseInfo());
        authRespFields.put(MessageFields.PROCESSING_CODE, new NumericParseInfo(6));
        authRespFields.put(MessageFields.AMOUNT, new NumericParseInfo(12));
        authRespFields.put(MessageFields.LOCAL_TIME, new NumericParseInfo(12));
        authRespFields.put(MessageFields.ACQUIRER_REFERENCE, new LlvarParseInfo());
        authRespFields.put(MessageFields.APPROVAL_CODE, new AlphaParseInfo(6));
        authRespFields.put(MessageFields.ACTION_CODE, new NumericParseInfo(3));
        authRespFields.put(MessageFields.CARD_ACCEPTOR_TERMINAL_ID, new AlphaParseInfo(8));
        authRespFields.put(MessageFields.CARD_ACCEPTOR_IDENTIFICATION_CODE, new AlphaParseInfo(15));
        authRespFields.put(MessageFields.ADDITIONAL_RESPONSE_DATA, new LlvarParseInfo());
        authRespFields.put(MessageFields.ADDITIONAL_DATA_NATIONAL, new LllvarParseInfo());
        authRespFields.put(MessageFields.CURRENCY_CODE, new AlphaParseInfo(3));
        authRespFields.put(MessageFields.AUTH_ODE, new LllvarParseInfo());
        channelFactory.getMessageFactory().setParseMap(MessageTypes.AUTHORIZATION_RESPONSE, authRespFields);

        Map<Integer, FieldParseInfo> reverseRespFields = new HashMap<Integer, FieldParseInfo>();
        reverseRespFields.put(MessageFields.PRIMARY_ACCOUNT_NUMBER, new LlvarParseInfo());
        reverseRespFields.put(MessageFields.PROCESSING_CODE, new NumericParseInfo(6));
        reverseRespFields.put(MessageFields.AMOUNT, new NumericParseInfo(12));
        reverseRespFields.put(MessageFields.LOCAL_TIME, new NumericParseInfo(12));
        reverseRespFields.put(MessageFields.ACQUIRER_REFERENCE, new LlvarParseInfo());
        reverseRespFields.put(MessageFields.ACTION_CODE, new NumericParseInfo(3));
        reverseRespFields.put(MessageFields.CARD_ACCEPTOR_TERMINAL_ID, new AlphaParseInfo(8));
        reverseRespFields.put(MessageFields.CARD_ACCEPTOR_IDENTIFICATION_CODE, new AlphaParseInfo(15));
        reverseRespFields.put(MessageFields.CURRENCY_CODE, new AlphaParseInfo(3));
        reverseRespFields.put(MessageFields.AUTH_ODE, new LllvarParseInfo());
        channelFactory.getMessageFactory().setParseMap(MessageTypes.REVERSAL_ADVICE_RESPONSE, reverseRespFields);

        Map<Integer, FieldParseInfo> captureRespFields = new HashMap<Integer, FieldParseInfo>();
        captureRespFields.put(MessageFields.PRIMARY_ACCOUNT_NUMBER, new LlvarParseInfo());
        captureRespFields.put(MessageFields.PROCESSING_CODE, new NumericParseInfo(6));
        captureRespFields.put(MessageFields.AMOUNT, new NumericParseInfo(12));
        captureRespFields.put(MessageFields.LOCAL_TIME, new NumericParseInfo(12));
        captureRespFields.put(MessageFields.ACQUIRER_REFERENCE, new LlvarParseInfo());
        captureRespFields.put(MessageFields.ACTION_CODE, new NumericParseInfo(3));
        captureRespFields.put(MessageFields.CARD_ACCEPTOR_TERMINAL_ID, new AlphaParseInfo(8));
        captureRespFields.put(MessageFields.CARD_ACCEPTOR_IDENTIFICATION_CODE, new AlphaParseInfo(15));
        captureRespFields.put(MessageFields.ADDITIONAL_RESPONSE_DATA, new LlvarParseInfo());
        captureRespFields.put(MessageFields.ADDITIONAL_DATA_NATIONAL, new LllvarParseInfo());
        captureRespFields.put(MessageFields.CURRENCY_CODE, new AlphaParseInfo(3));
        captureRespFields.put(MessageFields.AUTH_ODE, new LllvarParseInfo());
        channelFactory.getMessageFactory().setParseMap(MessageTypes.CAPTURE_RESPONSE, captureRespFields);

        LOG.info("Nets instance initialized.");

    }

    public Nets(ChannelFactory channelFactory, Repository<TransactionData, String> persistence) {
        this.channelFactory = channelFactory;
        this.repository = persistence;

        expireFormat.setMinimumIntegerDigits(2);
        expireFormat.setMaximumIntegerDigits(2);
        cvdFormat.setMinimumIntegerDigits(3);
        cvdFormat.setMaximumIntegerDigits(3);

        init();
    }

    public void setMaxRequestAttempts(int maxRequestAttempts) {
        maxRequestAttempts = maxRequestAttempts > 0 ? maxRequestAttempts : DEFAULT_MAX_ATTEMPTS_PER_REQUEST;
        this.maxRequestAttempts = maxRequestAttempts;
    }

    public int getMaxRequestAttempts() {
        return maxRequestAttempts;
    }

    public int getMinWaitBetweenAttempts() {
        return minWaitBetweenAttempts;
    }

    public void setMinWaitBetweenAttempts(int minWaitBetweenAttempts) {
        this.minWaitBetweenAttempts = minWaitBetweenAttempts;
    }
    
    

    public void authorize(Merchant merchant, Card card, Money money, String orderId) throws IOException, NetsException {
        AuthorizeRequest request = new AuthorizeRequest(merchant, card, money, orderId);
        NetsResponse response = request.send();

        if (response.getActionCode().getMerchantAction() == MerchantAction.Approved) {
            TransactionData data = new TransactionData();
            data.setId(buildTransactionDataId(merchant, orderId));
            data.setActionCode(response.getActionCode());
            data.setApprovedAmount(money);
            data.setOde(response.getOde());
            data.setApprovalCode(response.getApprovalCode());
            data.setCard(card);
            repository.save(data);
        } else {
            throw new NetsException("Authorize not approved.", response.getActionCode());
        }
    }

    public void capture(Merchant merchant, Money money, String orderId, boolean refund) throws IOException, NetsException {
        TransactionData data = repository.findOne(buildTransactionDataId(merchant, orderId));
        CaptureRequest request = new CaptureRequest(merchant, data.getCard(), money, orderId, data.getOde(), data.getApprovalCode(), data.getActionCode());
        request.setRefund(refund);
        NetsResponse response = request.send();

        if (response.getActionCode().getMerchantAction() == MerchantAction.Approved) {
            data.setActionCode(response.getActionCode());
            data.setOde(response.getOde());
            repository.save(data);
        } else {
            throw new NetsException("Capture not approved.", response.getActionCode());
        }
    }

    public void reverse(Merchant merchant, String orderId) throws IOException, NetsException {
        TransactionData data = repository.findOne(buildTransactionDataId(merchant, orderId));
        ReverseRequest request = new ReverseRequest(merchant, data.getCard(), data.getApprovedAmount(), orderId, data.getOde(), data.getApprovalCode());
        NetsResponse response = request.send();

        if (response.getActionCode().getMerchantAction() == MerchantAction.Approved) {
            data.setActionCode(response.getActionCode());
            data.setOde(response.getOde());
            repository.save(data);
        } else {
            throw new NetsException("Reverse not approved.", response.getActionCode());
        }
    }

    private String buildTransactionDataId(Merchant merchant, String orderId) {
        return merchant.getMerchantId() + "_" + orderId;
    }
}
