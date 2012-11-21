package dk.apaq.nets.payment;

import com.solab.iso8583.parse.AlphaParseInfo;
import com.solab.iso8583.parse.FieldParseInfo;
import com.solab.iso8583.parse.LllvarParseInfo;
import com.solab.iso8583.parse.LlvarParseInfo;
import com.solab.iso8583.parse.NumericParseInfo;
import dk.apaq.framework.repository.Repository;
import dk.apaq.nets.payment.io.ChannelFactory;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import org.joda.money.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author michael
 */
public class Nets {

    private static final Logger LOG = LoggerFactory.getLogger(Nets.class);
    private final ChannelFactory channelFactory;
    private final NumberFormat expireFormat = NumberFormat.getIntegerInstance();
    private final NumberFormat cvdFormat = NumberFormat.getIntegerInstance();
    private final Repository<TransactionData, String> repository;
    
    private int maxRequestAttempts = AbstractNetsRequest.DEFAULT_MAX_ATTEMPTS_PER_REQUEST;
    private int minWaitBetweenAttempts = AbstractNetsRequest.DEFAULT_MIN_WAIT_BETWEEN_ATTEMPTS;

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
        maxRequestAttempts = maxRequestAttempts > 0 ? maxRequestAttempts : AbstractNetsRequest.DEFAULT_MAX_ATTEMPTS_PER_REQUEST;
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
    
    /**
     * Same as calling authorize(merchant, card, money, orderId, false, false, false,false);
     */
    public void authorize(Merchant merchant, Card card, Money money, String orderId) throws IOException, NetsException {
        authorize(merchant, card, money, orderId, false, false, false, false);
    }
    
    /**
     * Authorizes an amount. Authorisations can be requested for the ‘Accurate amount’ or an ‘Estimated amount’.
     * Following the first ‘Original Authorization’, a ‘Supplementary Authorization’ (function code '106') can be sent. 
     * This is an option depending on acquirer participation.<br><br>
     * An example of use of a ‘Supplementary Authorization’:<br>
     * If a merchant having received an order and checked for validity of card and CDV, is not able to deliver the goods 
     * or services within the period of time for which funds are guaranteed (typical within seven days), he sends an 
     * ‘Original Authorization’ to the amount of zero(0). Shortly, before this dispatch, the authorisation is send for 
     * the full amount, securing the card is still active and the amount disposable.
     * @param merchant The merchant used when authorizing.
     * @param card The creditcard information.
     * @param money The amount to capture which can be be less or equals to the amount authorised(except for special merchante categories).
     * @param orderId The orderId for which money were authorized.
     * @throws IOException Thrown if communication with Nets fails.
     * @throws NetsException Thrown if the reverse was not approved.
     * @throws IOException
     * @throws NetsException 
     */
    public void authorize(Merchant merchant, Card card, Money money, String orderId, boolean recurring, boolean estimatedAmount, boolean fraudSuspect, boolean gambling) throws IOException, NetsException {
        AuthorizeRequest request = new AuthorizeRequest(merchant, card, money, orderId, channelFactory);
        request.setEstimatedAmount(estimatedAmount);
        request.setFraudSuspect(fraudSuspect);
        request.setGambling(gambling);
        request.setRecurring(recurring);
        setRequestRetryProperties(request);
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

    /**
     * Captures an authorization. As a standard, the amount captured cannot exceed the amount authorised, except for specific 
     * merchant categories e.g. where gratuity is common. Additionally, the merchant and acquirer may have made special arrangements, 
     * which the payment module must be able to support.
     * @param merchant The merchant used when authorizing.
     * @param money The amount to capture which can be be less or equals to the amount authorised(except for special merchante categories).
     * @param orderId The orderId for which money were authorized.
     * @throws IOException Thrown if communication with Nets fails.
     * @throws NetsException Thrown if the reverse was not approved.
     */
    public void capture(Merchant merchant, Money money, String orderId) throws IOException, NetsException {
        doCaptureRequest(merchant, money, orderId, false);
    }
    
    /**
     * Credits a captured amount. The merchant must be registered with his/hers acquirer for this option. The credited amount cannot 
     * exceed the captured amount.<br><br>
     * A credit transaction - also known as a capture credit - may be used when a cardholder cancels his order or returns the merchandise 
     * to the merchant, asking his account to be credited. The amount may be the full original capture amount, or part of the amount.
     * @param merchant The merchant used when authorizing.
     * @param money The amount to refund which can be be less or equals to the amount captured.
     * @param orderId The orderId for which money were authorized.
     * @throws IOException Thrown if communication with Nets fails.
     * @throws NetsException Thrown if the reverse was not approved.
     */
    public void credit(Merchant merchant, Money money, String orderId) throws IOException, NetsException {
        doCaptureRequest(merchant, money, orderId, true);
    }

    /**
     * Reverses an authorization. If a merchant following a successful authorisation is unable to dispatch the order on time,
     * an ‘Authorization Reversal’ should be sent. The amount reversed must be equivalent to the amount authorised.
     * When involving debit cards in particular, it is critical for the cardholder’s disposal of his account that the 
     * authorisation is reversed. If the authorisation is not reversed it can affect the cardholder’s possibilities of placing 
     * a replacement order.
     * @param merchant The merchant used when authorizing.
     * @param orderId The orderId for which money were authorized.
     * @throws IOException Thrown if communication with Nets fails.
     * @throws NetsException Thrown if the reverse was not approved.
     */
    public void reverse(Merchant merchant, String orderId) throws IOException, NetsException {
        TransactionData data = repository.findOne(buildTransactionDataId(merchant, orderId));
        ReverseRequest request = new ReverseRequest(merchant, data.getCard(), data.getApprovedAmount(), orderId, data.getOde(), data.getApprovalCode(), channelFactory);
        setRequestRetryProperties(request);
        NetsResponse response = request.send();

        if (response.getActionCode().getMerchantAction() == MerchantAction.Approved) {
            data.setActionCode(response.getActionCode());
            data.setOde(response.getOde());
            repository.save(data);
        } else {
            throw new NetsException("Reverse not approved.", response.getActionCode());
        }
    }
    
    private void doCaptureRequest(Merchant merchant, Money money, String orderId, boolean refund) throws IOException, NetsException {
        TransactionData data = repository.findOne(buildTransactionDataId(merchant, orderId));
        CaptureRequest request = new CaptureRequest(merchant, data.getCard(), money, orderId, data.getOde(), data.getApprovalCode(), data.getActionCode(), channelFactory);
        setRequestRetryProperties(request);
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

    private String buildTransactionDataId(Merchant merchant, String orderId) {
        return merchant.getMerchantId() + "_" + orderId;
    }
    
    private void setRequestRetryProperties(AbstractNetsRequest req) {
        req.setMaxRequestAttempts(maxRequestAttempts);
        req.setMinWaitBetweenAttempts(minWaitBetweenAttempts);
    }
}
