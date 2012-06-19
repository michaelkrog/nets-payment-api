package dk.apaq.nets.payment;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.MessageFactory;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.joda.money.Money;

/**
 *
 * @author michael
 */
public class Nets implements IPaymentSystem {

    private static final int MESSAGE_TYPE_AUTHORIZE_REQUEST = 1100;
    
    private final URL netsServiceUrl;
    private final HttpClient httpClient;
    private final MessageFactory messageFactory;
    

    public Nets(String netsServiceUrl, HttpClient httpClient, MessageFactory messageFactory) throws MalformedURLException {
        this.netsServiceUrl = new URL(netsServiceUrl);
        this.httpClient = httpClient;
        this.messageFactory = messageFactory;
    }
    
    
    public void authorize(Merchant merchant, Card card, Money money, String orderId, boolean recurring, boolean fraudSuspect, String terminalId) throws IOException {
        IsoMessage message = messageFactory.newMessage(MESSAGE_TYPE_AUTHORIZE_REQUEST);
        
    
        HttpHost host = new HttpHost(netsServiceUrl.getHost(), netsServiceUrl.getPort(), netsServiceUrl.getProtocol());
        HttpPost postMethod = new HttpPost(netsServiceUrl.getPath());
        ByteArrayEntity entity = new ByteArrayEntity(message.writeToBuffer(200).array());
        postMethod.setEntity(entity);
        HttpResponse response = httpClient.execute(host, postMethod);
    }

    public void renewAuthorization() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void refund() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void capture() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void cancel() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
