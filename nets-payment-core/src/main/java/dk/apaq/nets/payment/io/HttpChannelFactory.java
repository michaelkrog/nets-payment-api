package dk.apaq.nets.payment.io;

import com.solab.iso8583.MessageFactory;
import java.net.URL;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;

/**
 * An implementation of ChannelFactory that creates new HttpChannels.
 */
public class HttpChannelFactory extends AbstractChannelFactory {

    private static final MessageFactory MESSAGE_FACTORY = new MessageFactory();
    private URL url;
    private HttpClient client;
    
    /**
     * Creates new instance.
     * @param url 
     */
    public HttpChannelFactory(URL url) {
        this(url, null, null);
    }

    /**
     * Create new instance.
     * @param url
     * @param channelLogger 
     */
    public HttpChannelFactory(URL url, ChannelLogger channelLogger) {
        this(url, null, channelLogger);
    }

    /**
     * Creates new instance.
     * @param url
     * @param client 
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
    public Channel createChannel() {
        return new HttpChannel(getChannelLogger(), MESSAGE_FACTORY, client, url);
    }

    /**
     * Retrieves the message factory ued by this factory.
     * @return 
     */
    public MessageFactory getMessageFactory() {
        return MESSAGE_FACTORY;
    }
    
}
