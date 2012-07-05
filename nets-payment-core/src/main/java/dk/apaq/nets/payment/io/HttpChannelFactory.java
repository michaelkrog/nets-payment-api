package dk.apaq.nets.payment.io;

import com.solab.iso8583.MessageFactory;
import java.net.URL;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 *
 * @author krog
 */
public class HttpChannelFactory implements ChannelFactory {

    private static final MessageFactory messageFactory = new MessageFactory();
    private URL url;
    private HttpClient client;
    
    public HttpChannelFactory(URL url) {
        this.url = url;
        this.client = new DefaultHttpClient();
    }
    
    public Channel createChannel() {
        return new HttpChannel(messageFactory, client, url);
    }

    public MessageFactory getMessageFactory() {
        return messageFactory;
    }
    
}
