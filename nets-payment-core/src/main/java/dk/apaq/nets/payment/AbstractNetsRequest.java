package dk.apaq.nets.payment;

import java.io.IOException;
import java.text.NumberFormat;

import com.solab.iso8583.IsoMessage;
import dk.apaq.framework.common.beans.finance.Card;
import dk.apaq.nets.payment.io.Channel;
import dk.apaq.nets.payment.io.ChannelFactory;
import org.apache.commons.lang.Validate;
import org.jasypt.encryption.StringEncryptor;
import org.joda.money.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class for Nets Requests.
 *
 * @param <T> Type of Request.
 */
abstract class AbstractNetsRequest<T> {
    
    public static final int DEFAULT_MAX_ATTEMPTS_PER_REQUEST = 5;
    public static final int DEFAULT_MIN_WAIT_BETWEEN_ATTEMPTS = 60000;
    public static final String DATE_FORMAT = "yyMMddHHmmss";
    public static final NumberFormat EXPIRE_FORMAT = NumberFormat.getIntegerInstance();
    public static final NumberFormat CVD_FORMAT = NumberFormat.getIntegerInstance();
    public static final NumberFormat BUSINESS_CODE_FORMAT = NumberFormat.getIntegerInstance();
    private static final int ADDRESS_WITHOUT_COUNTRY_LENGTH = 96;
    private static final Logger LOG = LoggerFactory.getLogger(AbstractNetsRequest.class);
    
    private final StringEncryptor encryptor;

    static {
        //CHECKSTYLE:OFF
        EXPIRE_FORMAT.setMaximumIntegerDigits(2);
        EXPIRE_FORMAT.setMinimumIntegerDigits(2);
        CVD_FORMAT.setMaximumIntegerDigits(3);
        CVD_FORMAT.setMinimumIntegerDigits(3);
        BUSINESS_CODE_FORMAT.setMaximumIntegerDigits(4);
        BUSINESS_CODE_FORMAT.setMinimumIntegerDigits(4);
        BUSINESS_CODE_FORMAT.setGroupingUsed(false);
        //CHECKSTYLE:ON
    }

    private final Merchant merchant;
    private final Card card;
    private final Money money;
    private final String orderId;
    private final String ode;
    private String terminalId = "";
    private final ChannelFactory channelFactory;
    private int maxRequestAttempts = DEFAULT_MAX_ATTEMPTS_PER_REQUEST;
    private int minWaitBetweenAttempts = DEFAULT_MIN_WAIT_BETWEEN_ATTEMPTS;
    
    public AbstractNetsRequest(Merchant merchant, Card card, Money money, String orderId, ChannelFactory channelFactory,
            StringEncryptor encryptor) {
        this(merchant, card, money, orderId, null, channelFactory, encryptor);
    }
    
    public AbstractNetsRequest(Merchant merchant, Card card, Money money, String orderId, String ode, ChannelFactory channelFactory,
            StringEncryptor encryptor) {
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
        this.encryptor = encryptor;
    }

    public StringEncryptor getEncryptor() {
        return encryptor;
    }
    
    public ChannelFactory getChannelFactory() {
        return channelFactory;
    }
    
    public int getMaxRequestAttempts() {
        return maxRequestAttempts;
    }
    
    public void setMaxRequestAttempts(int value) {
        this.maxRequestAttempts = value;
    }
    
    public int getMinWaitBetweenAttempts() {
        return minWaitBetweenAttempts;
    }
    
    public void setMinWaitBetweenAttempts(int value) {
        this.minWaitBetweenAttempts = value;
    }
    
    public T setTerminalId(String value) {
        this.terminalId = value;
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
        fillString(address, ' ', ADDRESS_WITHOUT_COUNTRY_LENGTH);
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
        IOException lastIOException = null;
        
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
                lastIOException = ex;
                doAttempt = response == null && attempts < maxRequestAttempts;
                //Error occurred - if we are gonna try again then sleep a little first according to Nets requirements.
                if (doAttempt) {
                    long timeTillNextRequest = Math.max(0, minWaitBetweenAttempts - (System.currentTimeMillis() - start));
                    sleep(timeTillNextRequest);
                }
            }
        }
        if (response == null) {
            throw new IOException("Unable to communicate with Nets.", lastIOException);
        }
        String processingCode = response.getField(MessageFields.PROCESSING_CODE).toString();
        String actionCodeString = response.getField(MessageFields.ACTION_CODE).toString();
        String newOde = response.getField(MessageFields.AUTH_ODE).toString();
        String approvalCode = null;
        if (response.hasField(MessageFields.APPROVAL_CODE)) {
            approvalCode = response.getField(MessageFields.APPROVAL_CODE).toString();
        }
        return new NetsResponse(ActionCode.fromCode(actionCodeString), newOde, processingCode, approvalCode);
    }
    
    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            LOG.error("Error occured when trying to sleep between request attempts.", ex);
        }
    }
}
