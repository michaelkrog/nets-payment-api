package dk.apaq.nets.payment;

import com.solab.iso8583.IsoMessage;
import dk.apaq.nets.payment.io.Channel;
import dk.apaq.nets.payment.io.ChannelFactory;
import java.io.IOException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import org.apache.commons.lang.Validate;
import org.joda.money.Money;

/**
 * Abstract class for Requests.
 * @author michael
 */
abstract class AbstractNetsRequest<T> {
    
    public static final int DEFAULT_MAX_ATTEMPTS_PER_REQUEST = 5;
    public static final int DEFAULT_MIN_WAIT_BETWEEN_ATTEMPTS = 60000;
    
    protected final Merchant merchant;
    protected final Card card;
    protected final Money money;
    protected final String orderId;
    protected final String ode;
    protected final DateFormat df = new SimpleDateFormat("yyMMddHHmmss");
    protected final NumberFormat expireFormat = NumberFormat.getIntegerInstance();
    protected final NumberFormat cvdFormat = NumberFormat.getIntegerInstance();
    
    protected String terminalId = "";
    protected final ChannelFactory channelFactory;
    protected int maxRequestAttempts = DEFAULT_MAX_ATTEMPTS_PER_REQUEST;
    protected int minWaitBetweenAttempts = DEFAULT_MIN_WAIT_BETWEEN_ATTEMPTS;

    public AbstractNetsRequest(Merchant merchant, Card card, Money money, String orderId, ChannelFactory channelFactory) {
        this(merchant, card, money, orderId, null, channelFactory);
    }

    public AbstractNetsRequest(Merchant merchant, Card card, Money money, String orderId, String ode, ChannelFactory channelFactory) {
        Validate.notNull(merchant, "merchant must be specified");
        Validate.notNull(card, "card must be specified");
        Validate.notNull(money, "money must be specified");
        Validate.notNull(orderId, "orderId must be specified");
        this.merchant = merchant;
        this.card = card;
        this.money = money;
        this.orderId = orderId;
        this.ode = ode;
        this.channelFactory = channelFactory;
    }

    public int getMaxRequestAttempts() {
        return maxRequestAttempts;
    }

    public void setMaxRequestAttempts(int maxRequestAttempts) {
        this.maxRequestAttempts = maxRequestAttempts;
    }

    public int getMinWaitBetweenAttempts() {
        return minWaitBetweenAttempts;
    }

    public void setMinWaitBetweenAttempts(int minWaitBetweenAttempts) {
        this.minWaitBetweenAttempts = minWaitBetweenAttempts;
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
                if (doAttempt) {
                    long timeTillNextRequest = Math.max(0, minWaitBetweenAttempts - (System.currentTimeMillis() - start));
                    try {
                        Thread.sleep(timeTillNextRequest);
                    } catch (InterruptedException ex2) {
                        /* EMPTY */
                        /* EMPTY */
                    }
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
