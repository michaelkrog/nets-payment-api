package dk.apaq.nets.payment.io;

import com.solab.iso8583.MessageFactory;
import java.net.URL;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;

/**
 *
 * @author krog
 */
public class HttpChannelFactory extends AbstractChannelFactory {

    private static final MessageFactory messageFactory = new MessageFactory();
    private URL url;
    private HttpClient client;
    
    public HttpChannelFactory(URL url) {
        this(url, null);
    }

    public HttpChannelFactory(URL url, ChannelLogger channelLogger) {
        super(channelLogger);
        this.url = url;
        this.client = new DefaultHttpClient();
        this.client.getParams().setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 30000);
    }
    
    public Channel createChannel() {
        return new HttpChannel(channelLogger, messageFactory, client, url);
    }

    public MessageFactory getMessageFactory() {
        return messageFactory;
    }
    
}
