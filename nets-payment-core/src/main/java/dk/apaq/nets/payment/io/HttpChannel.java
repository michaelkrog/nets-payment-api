package dk.apaq.nets.payment.io;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.MessageFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author krog
 */
public class HttpChannel extends AbstractChannel {

    private static final Logger LOG = LoggerFactory.getLogger(HttpChannel.class);
    
    private final HttpClient client;
    private final URL url;
    private final ChannelLogger channelLogger;

    public HttpChannel(ChannelLogger channelLogger, MessageFactory messageFactory, HttpClient client, URL url) {
        super(messageFactory);
        this.client = client;
        this.url = url;
        this.channelLogger = channelLogger;
    }

    
    public IsoMessage sendMessage(IsoMessage message) throws IOException {
        byte[] msgData = messageToByteArray(message);
       
        if(channelLogger != null) {
            channelLogger.onMessageSent(msgData);
        }
        
        HttpHost host = new HttpHost(url.getHost(), url.getPort(), url.getProtocol());
        HttpPost postMethod = new HttpPost(url.getPath());
        ByteArrayEntity entity = new ByteArrayEntity(msgData);
        postMethod.setEntity(entity);
        HttpResponse response = client.execute(host, postMethod);
        
        if(response.getStatusLine().getStatusCode()!=200) {
            throw new IOException("The status code from the server was not ok. " + response.getStatusLine().getReasonPhrase());
        }
        
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        IOUtils.copy(response.getEntity().getContent(), buf);
        buf.flush();
        
        byte[] responseData = buf.toByteArray();
        
        if(channelLogger != null) {
            channelLogger.onMessageRecieved(responseData);
        }
        
        return byteArrayToMessage(responseData);
    }
    
}
