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
public class HttpChannel implements Channel {

    private final MessageFactory messageFactory;
    private final HttpClient client;
    private final URL url;

    public HttpChannel(MessageFactory messageFactory, HttpClient client, URL url) {
        this.messageFactory = messageFactory;
        this.client = client;
        this.url = url;
    }

    
    public IsoMessage sendMessage(IsoMessage message) throws IOException {
        byte[] packet = message.writeToBuffer(0).array();
        
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        
        //Append special Nets header
        PGTMHeader pgtmh = new PGTMHeader((short)(packet.length + 32), "0000524800022000000000000032000000000000000000000000", "0000");
        buf.write(pgtmh.toByteArray());
        
        buf.write(packet);
        
        
        HttpHost host = new HttpHost(url.getHost(), url.getPort(), url.getProtocol());
        HttpPost postMethod = new HttpPost(url.getPath());
        ByteArrayEntity entity = new ByteArrayEntity(buf.toByteArray());
        postMethod.setEntity(entity);
        HttpResponse response = client.execute(host, postMethod);
        
        if(response.getStatusLine().getStatusCode()!=200) {
            throw new IOException("The status code from the server was not ok. " + response.getStatusLine().getReasonPhrase());
        }
        
        buf.reset();
        IOUtils.copy(response.getEntity().getContent(), buf);
        buf.flush();
        try {
            return messageFactory.parseMessage(buf.toByteArray(), 10);
        } catch (ParseException ex) {
            throw new IOException("Unable to parse response.", ex);
        } catch (UnsupportedEncodingException ex) {
            throw new IOException("Unable to parse response because of encoding issues.", ex);
        }
    }
    
}
