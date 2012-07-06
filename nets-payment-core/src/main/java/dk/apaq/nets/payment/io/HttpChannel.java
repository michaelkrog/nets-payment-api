package dk.apaq.nets.payment.io;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.MessageFactory;
import dk.apaq.nets.payment.PGTMHeader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;

/**
 *
 * @author krog
 */
public class HttpChannel extends AbstractChannel {

    private final HttpClient client;
    private final URL url;

    public HttpChannel(MessageFactory messageFactory, HttpClient client, URL url) {
        super(messageFactory);
        this.client = client;
        this.url = url;
    }

    
    public IsoMessage sendMessage(IsoMessage message) throws IOException {
        byte[] msgData = messageToByteArray(message);
        
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
        
        return byteArrayToMessage(buf.toByteArray());
    }
    
}
