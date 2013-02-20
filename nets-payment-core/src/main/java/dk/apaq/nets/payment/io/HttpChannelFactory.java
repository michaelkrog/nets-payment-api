package dk.apaq.nets.payment.io;

import java.net.URL;

import com.solab.iso8583.MessageFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;

/**
 * An implementation of ChannelFactory that creates new HttpChannels.
 */
public class HttpChannelFactory extends AbstractChannelFactory {

    private MessageFactory messageFactory = new MessageFactory();
    private URL url;
    private HttpClient client;
    
    /**
     * Creates new instance.
     * @param url The url to connect to
     */
    public HttpChannelFactory(URL url) {
        this(url, null, null);
    }

    /**
     * Create new instance.
     * @param url The url to connect to.
     * @param channelLogger The logger.
     */
    public HttpChannelFactory(URL url, ChannelLogger channelLogger) {
        this(url, null, channelLogger);
    }

    /**
     * Creates new instance.
     * @param url The url to connect to.
     * @param client The HttpClient to use.
     */
    public HttpChannelFactory(URL url, HttpClient client) {
        this(url, client, null);
    }
    
    /**
     * Cretaes new instance.
     * @param url
     * @param client
     * @param channelLogger 
     */
    public HttpChannelFactory(URL url, HttpClient client, ChannelLogger channelLogger) {
        super(channelLogger);
        this.url = url;
        this.client = client == null ? new DefaultHttpClient() : client;
        this.client.getParams().setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 30000);
    }
    
    
    /**
     * Creates a new Channel.
     * @return The channel created.
     */
    @Override
    public Channel createChannel() {
        return new HttpChannel(getChannelLogger(), messageFactory, client, url);
    }

    /**
     * Retrieves the message factory ued by this factory.
     * @return The message factory.
     */
    @Override
    public MessageFactory getMessageFactory() {
        return messageFactory;
    }

    @Override
    public void setMessageFactory(MessageFactory messageFactory) {
        this.messageFactory = messageFactory;
    }
    
}
