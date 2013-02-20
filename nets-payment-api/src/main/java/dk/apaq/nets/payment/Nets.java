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
import dk.apaq.framework.common.beans.finance.Card;
import dk.apaq.nets.payment.io.ChannelFactory;
import org.joda.money.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static dk.apaq.nets.payment.MessageFields.*;
import org.jasypt.encryption.StringEncryptor;

/**
 * The main class of the Nets Payment API. <br>
 * This class gives access to all the needed methods for processing payments via Nets.
 */
public class Nets {

    private static final Logger LOG = LoggerFactory.getLogger(Nets.class);
    private final ChannelFactory channelFactory;
    private final StringEncryptor encryptor;
    private int maxRequestAttempts = AbstractNetsRequest.DEFAULT_MAX_ATTEMPTS_PER_REQUEST;
    private int minWaitBetweenAttempts = AbstractNetsRequest.DEFAULT_MIN_WAIT_BETWEEN_ATTEMPTS;

    private void init() {
        channelFactory.setMessageFactory(NetsMessageFactoryCreator.createFactory());
        LOG.info("Nets instance initialized.");

    }

    /**
     * Consctructs a new Nets instance.
     * @param channelFactory The channelFactory to use.
     * @param persistence The Repository to use for storing data needed the this instance.
     */
    public Nets(ChannelFactory channelFactory, StringEncryptor encryptor) {
        this.channelFactory = channelFactory;
        this.encryptor = encryptor;
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
    public NetsResponse authorize(Merchant merchant, Card card, Money money, String orderId) throws IOException, NetsException {
        return authorize(merchant, card, money, orderId, false, false, false, false);
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
    public NetsResponse authorize(Merchant merchant, Card card, Money money, String orderId, boolean recurring, boolean estimatedAmount, 
            boolean fraudSuspect, boolean gambling) throws IOException, NetsException {
        AuthorizeRequest request = new AuthorizeRequest(merchant, card, money, orderId, channelFactory, encryptor);
        request.setEstimatedAmount(estimatedAmount);
        request.setFraudSuspect(fraudSuspect);
        request.setGambling(gambling);
        request.setRecurring(recurring);
        setRequestRetryProperties(request);
        NetsResponse response = request.send();

        if (response.getActionCode().getMerchantAction() == MerchantAction.Approved) {
            return response;
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
    public NetsResponse capture(Merchant merchant, Money money, String orderId, Card card, ActionCode actionCode, String ode, String processingCode, 
            String approvalCode) throws IOException, NetsException {
        return doCaptureRequest(merchant, money, orderId, false, card, actionCode, ode, processingCode, approvalCode);
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
    public NetsResponse credit(Merchant merchant, Money money, String orderId, Card card, ActionCode actionCode, String ode, String processingCode, 
            String approvalCode) throws IOException, NetsException {
        return doCaptureRequest(merchant, money, orderId, true, card, actionCode, ode, processingCode, approvalCode);
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
    public NetsResponse reverse(Merchant merchant, Money money, String orderId, Card card, ActionCode actionCode, String ode, 
            String processingCode, String approvalCode) throws IOException, NetsException {
        ReverseRequest request = new ReverseRequest(merchant, card, money, orderId, ode, processingCode, approvalCode, channelFactory, 
                encryptor);
        setRequestRetryProperties(request);
        NetsResponse response = request.send();

        if (response.getActionCode().getMerchantAction() == MerchantAction.Approved) {
            return response;
        } else {
            throw new NetsException("Reverse not approved.", response.getActionCode());
        }
    }

    private NetsResponse doCaptureRequest(Merchant merchant, Money money, String orderId, boolean refund, Card card, ActionCode actionCode, 
            String ode, String processingCode, String approvalCode) throws IOException, NetsException {
        CaptureRequest request = new CaptureRequest(merchant, card, money, orderId, ode, approvalCode, actionCode, channelFactory, encryptor);
        setRequestRetryProperties(request);
        request.setRefund(refund);
        NetsResponse response = request.send();

        if (response.getActionCode().getMerchantAction() == MerchantAction.Approved) {
            return response;
        } else {
            throw new NetsException("Capture not approved.", response.getActionCode());
        }
    }

    private void setRequestRetryProperties(AbstractNetsRequest req) {
        req.setMaxRequestAttempts(maxRequestAttempts);
        req.setMinWaitBetweenAttempts(minWaitBetweenAttempts);
    }
}
