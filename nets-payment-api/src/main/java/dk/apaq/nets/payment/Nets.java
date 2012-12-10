package dk.apaq.nets.payment;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

import com.solab.iso8583.parse.AlphaParseInfo;
import com.solab.iso8583.parse.FieldParseInfo;
import com.solab.iso8583.parse.LllvarParseInfo;
import com.solab.iso8583.parse.LlvarParseInfo;
import com.solab.iso8583.parse.NumericParseInfo;
import dk.apaq.framework.repository.Repository;
import dk.apaq.nets.payment.io.ChannelFactory;
import org.joda.money.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static dk.apaq.nets.payment.MessageFields.*;

/**
 * The main class of the Nets Payment API. <br>
 * This class gives access to all the needed methods for processing payments via Nets.
 */
public class Nets {

    private static final Logger LOG = LoggerFactory.getLogger(Nets.class);
    private final ChannelFactory channelFactory;
    private final Repository<TransactionData, String> repository;
    private int maxRequestAttempts = AbstractNetsRequest.DEFAULT_MAX_ATTEMPTS_PER_REQUEST;
    private int minWaitBetweenAttempts = AbstractNetsRequest.DEFAULT_MIN_WAIT_BETWEEN_ATTEMPTS;

    private void init() {
        LOG.info("Initializing Nets instance.");

        Map<Integer, FieldParseInfo> authRespFields = new HashMap<Integer, FieldParseInfo>();
        authRespFields.put(PRIMARY_ACCOUNT_NUMBER, new LlvarParseInfo());
        authRespFields.put(PROCESSING_CODE, new NumericParseInfo(PROCESSING_CODE_LENGTH));
        authRespFields.put(AMOUNT, new NumericParseInfo(AMOUNT_LENGTH));
        authRespFields.put(LOCAL_TIME, new NumericParseInfo(LOCAL_TIME_LENGTH));
        authRespFields.put(ACQUIRER_REFERENCE, new LlvarParseInfo());
        authRespFields.put(APPROVAL_CODE, new AlphaParseInfo(APPROVAL_CODE_LENGTH));
        authRespFields.put(ACTION_CODE, new NumericParseInfo(ACTION_CODE_LENGTH));
        authRespFields.put(CARD_ACCEPTOR_TERMINAL_ID, new AlphaParseInfo(CARD_ACCEPTOR_TERMINAL_ID_LENGTH));
        authRespFields.put(CARD_ACCEPTOR_IDENTIFICATION_CODE, new AlphaParseInfo(CARD_ACCEPTOR_IDENTIFICATION_CODE_LENGTH));
        authRespFields.put(ADDITIONAL_RESPONSE_DATA, new LlvarParseInfo());
        authRespFields.put(ADDITIONAL_DATA_NATIONAL, new LllvarParseInfo());
        authRespFields.put(CURRENCY_CODE, new AlphaParseInfo(CURRENCY_CODE_LENGTH));
        authRespFields.put(AUTH_ODE, new LllvarParseInfo());
        channelFactory.getMessageFactory().setParseMap(MessageTypes.AUTHORIZATION_RESPONSE, authRespFields);

        Map<Integer, FieldParseInfo> reverseRespFields = new HashMap<Integer, FieldParseInfo>();
        reverseRespFields.put(PRIMARY_ACCOUNT_NUMBER, new LlvarParseInfo());
        reverseRespFields.put(PROCESSING_CODE, new NumericParseInfo(PROCESSING_CODE_LENGTH));
        reverseRespFields.put(AMOUNT, new NumericParseInfo(AMOUNT_LENGTH));
        reverseRespFields.put(LOCAL_TIME, new NumericParseInfo(LOCAL_TIME_LENGTH));
        reverseRespFields.put(ACQUIRER_REFERENCE, new LlvarParseInfo());
        reverseRespFields.put(ACTION_CODE, new NumericParseInfo(ACTION_CODE_LENGTH));
        reverseRespFields.put(CARD_ACCEPTOR_TERMINAL_ID, new AlphaParseInfo(CARD_ACCEPTOR_TERMINAL_ID_LENGTH));
        reverseRespFields.put(CARD_ACCEPTOR_IDENTIFICATION_CODE, new AlphaParseInfo(CARD_ACCEPTOR_IDENTIFICATION_CODE_LENGTH));
        reverseRespFields.put(CURRENCY_CODE, new AlphaParseInfo(CURRENCY_CODE_LENGTH));
        reverseRespFields.put(AUTH_ODE, new LllvarParseInfo());
        channelFactory.getMessageFactory().setParseMap(MessageTypes.REVERSAL_ADVICE_RESPONSE, reverseRespFields);

        Map<Integer, FieldParseInfo> captureRespFields = new HashMap<Integer, FieldParseInfo>();
        captureRespFields.put(PRIMARY_ACCOUNT_NUMBER, new LlvarParseInfo());
        captureRespFields.put(PROCESSING_CODE, new NumericParseInfo(PROCESSING_CODE_LENGTH));
        captureRespFields.put(AMOUNT, new NumericParseInfo(AMOUNT_LENGTH));
        captureRespFields.put(LOCAL_TIME, new NumericParseInfo(LOCAL_TIME_LENGTH));
        captureRespFields.put(ACQUIRER_REFERENCE, new LlvarParseInfo());
        captureRespFields.put(ACTION_CODE, new NumericParseInfo(ACTION_CODE_LENGTH));
        captureRespFields.put(CARD_ACCEPTOR_TERMINAL_ID, new AlphaParseInfo(CARD_ACCEPTOR_TERMINAL_ID_LENGTH));
        captureRespFields.put(CARD_ACCEPTOR_IDENTIFICATION_CODE, new AlphaParseInfo(CARD_ACCEPTOR_IDENTIFICATION_CODE_LENGTH));
        captureRespFields.put(ADDITIONAL_RESPONSE_DATA, new LlvarParseInfo());
        captureRespFields.put(ADDITIONAL_DATA_NATIONAL, new LllvarParseInfo());
        captureRespFields.put(CURRENCY_CODE, new AlphaParseInfo(CURRENCY_CODE_LENGTH));
        captureRespFields.put(AUTH_ODE, new LllvarParseInfo());
        channelFactory.getMessageFactory().setParseMap(MessageTypes.CAPTURE_RESPONSE, captureRespFields);

        LOG.info("Nets instance initialized.");

    }

    /**
     * Consctructs a new Nets instance.
     * @param channelFactory The channelFactory to use.
     * @param persistence The Repository to use for storing data needed the this instance.
     */
    public Nets(ChannelFactory channelFactory, Repository<TransactionData, String> persistence) {
        this.channelFactory = channelFactory;
        this.repository = persistence;
        init();
    }

    /**
     * Sets the maximum times a request will be attempted in case of error. 
     * @param maxRequestAttempts The number of requests.
     */
    public void setMaxRequestAttempts(int maxRequestAttempts) {
        maxRequestAttempts = maxRequestAttempts > 0 ? maxRequestAttempts : AbstractNetsRequest.DEFAULT_MAX_ATTEMPTS_PER_REQUEST;
        this.maxRequestAttempts = maxRequestAttempts;
    }

    /**
     * Retrieves the maximum number of times this instance will try a request if it fails.
     * @return The nuber of requests.
     */
    public int getMaxRequestAttempts() {
        return maxRequestAttempts;
    }

    /**
     * Retrieves the minimum time this instance will wait between retries on errors.
     * @return The time in milliseconds
     */
    public int getMinWaitBetweenAttempts() {
        return minWaitBetweenAttempts;
    }

    /**
     * Sets the time this instance should wait between each retry when erros occurs.
     * @param minWaitBetweenAttempts The time in milliseconds.
     */
    public void setMinWaitBetweenAttempts(int minWaitBetweenAttempts) {
        this.minWaitBetweenAttempts = minWaitBetweenAttempts;
    }

    /**
     * Same as calling authorize(merchant, card, money, orderId, false, false, false,false).
     * @param merchant The merchant.
     * @param card The card.
     * @param money The amount.
     * @param orderId The reference order id.
     */
    public void authorize(Merchant merchant, Card card, Money money, String orderId) throws IOException, NetsException {
        authorize(merchant, card, money, orderId, false, false, false, false);
    }

    /**
     * Authorizes an amount. <br>
     * Authorisations can be requested for the ‘Accurate amount’ or an ‘Estimated amount’. Following the first ‘Original
     * Authorization’, a ‘Supplementary Authorization’ (function code '106') can be sent. This is an option depending on acquirer
     * participation.<br><br> 
     * An example of use of a ‘Supplementary Authorization’:<br> 
     * If a merchant having received an order and checked for validity of card and CDV, is not able to deliver the goods or services 
     * within the period of time for which funds are guaranteed (typically within seven days), he sends an ‘Original Authorization’ to 
     * the amount of zero(0). Shortly, before this dispatch, the authorisation is send for the full amount, securing the card is still 
     * active and the amount disposable.
     *
     * @param merchant The merchant used when authorizing.
     * @param card The creditcard information.
     * @param money The amount to capture which can be be less or equals to the amount authorised(except for special merchante categories).
     * @param orderId The orderId for which money were authorized.
     * @param recurring Whether this is an authorization for recurring payment.
     * @param estimatedAmount Wether the amount is just estimated.
     * @param fraudSuspect Whether fraud is suspected in relation to this authorization.
     * @param gambling Whether this payment is related to gambling.
     * @throws IOException Thrown if communication with Nets fails.
     * @throws NetsException Thrown if the reverse was not approved.
     */
    public void authorize(Merchant merchant, Card card, Money money, String orderId, boolean recurring, boolean estimatedAmount, 
            boolean fraudSuspect, boolean gambling) throws IOException, NetsException {
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
            data.setProcessingCode(response.getProcessingCode());
            data.setCard(card);
            repository.save(data);
        } else {
            throw new NetsException("Authorize not approved.", response.getActionCode());
        }
    }

    /**
     * Captures an authorization. As a standard, the amount captured cannot exceed the amount authorised, except for specific merchant categories e.g.
     * where gratuity is common. Additionally, the merchant and acquirer may have made special arrangements, which the payment module must be able to
     * support.
     *
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
     * Credits a captured amount. The merchant must be registered with his/hers acquirer for this option. The credited amount cannot exceed the
     * captured amount.<br><br> A credit transaction - also known as a capture credit - may be used when a cardholder cancels his order or returns the
     * merchandise to the merchant, asking his account to be credited. The amount may be the full original capture amount, or part of the amount.
     *
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
     * Reverses an authorization. If a merchant following a successful authorisation is unable to dispatch the order on time, an ‘Authorization
     * Reversal’ should be sent. The amount reversed must be equivalent to the amount authorised. When involving debit cards in particular, it is
     * critical for the cardholder’s disposal of his account that the authorisation is reversed. If the authorisation is not reversed it can affect
     * the cardholder’s possibilities of placing a replacement order.
     *
     * @param merchant The merchant used when authorizing.
     * @param orderId The orderId for which money were authorized.
     * @throws IOException Thrown if communication with Nets fails.
     * @throws NetsException Thrown if the reverse was not approved.
     */
    public void reverse(Merchant merchant, String orderId) throws IOException, NetsException {
        TransactionData data = repository.findOne(buildTransactionDataId(merchant, orderId));
        ReverseRequest request = new ReverseRequest(merchant, data.getCard(), data.getApprovedAmount(), orderId, data.getOde(),
                data.getProcessingCode(), data.getApprovalCode(), channelFactory);
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
        CaptureRequest request = new CaptureRequest(merchant, data.getCard(), money, orderId, data.getOde(), data.getApprovalCode(), 
                data.getActionCode(), channelFactory);
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
