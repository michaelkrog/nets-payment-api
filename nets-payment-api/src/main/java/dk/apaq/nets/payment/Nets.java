package dk.apaq.nets.payment;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.IsoValue;
import com.solab.iso8583.MessageFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.codec.binary.Hex;
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

    private static final byte[] NETS_CUSTOM_HEADER = {0, -40, 0, 0, 82, 72, 0, 2, 32, 0, 0, 0, 0, 0, 0, 50, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private static final int MESSAGE_TYPE_AUTHORIZE_REQUEST = 4352; //1100 hex
    
    private final URL netsServiceUrl;
    private final HttpClient httpClient;
    private final MessageFactory messageFactory;
    private final DateFormat df = new SimpleDateFormat("yyMMddHHmmss");
    private final NumberFormat expireFormat = NumberFormat.getIntegerInstance();
    private final NumberFormat cvdFormat = NumberFormat.getIntegerInstance();
    
    static {
        
    }

    public Nets(String netsServiceUrl, HttpClient httpClient, MessageFactory messageFactory) throws MalformedURLException {
        this.netsServiceUrl = new URL(netsServiceUrl);
        this.httpClient = httpClient;
        this.messageFactory = messageFactory;
        
        expireFormat.setMinimumIntegerDigits(2);
        expireFormat.setMaximumIntegerDigits(2);
        cvdFormat.setMinimumIntegerDigits(3);
        cvdFormat.setMaximumIntegerDigits(3);
    }
    
    
    public void authorize(Merchant merchant, Card card, Money money, String orderId, boolean recurring, boolean fraudSuspect, String terminalId) throws IOException {
        IsoMessage message = messageFactory.newMessage(MESSAGE_TYPE_AUTHORIZE_REQUEST);
        
        message.setIsoHeader("PSIP100000");
        
        String expire = expireFormat.format(card.getExpireYear()) + expireFormat.format(card.getExpireMonth());
        String pointOfService = recurring ? "K00540K00130" : "K00500K00130";
        String function = "100"; //'100' ‘Original Authorization’ - amount accurate '101' ‘Original Authorization’ - amount estimated '106' ‘Supplementary Authorization’ - amount accurate
        String reason = fraudSuspect ? "1511" : "0000";
        
        StringBuilder address = new StringBuilder();
        address.append(merchant.getName());
        address.append("\\");
        address.append(merchant.getAddress().getStreet());
        address.append("\\");
        address.append(merchant.getAddress().getCity());
        address.append("\\");
        
        address.append(merchant.getAddress().getPostalCode());
        for(int i=address.length();i<96;i++) {
            address.append(" ");
        }
        
        address.append(merchant.getAddress().getCountryCode());
        
        
        message.setField(2, new IsoValue<String>(IsoType.LLVAR, card.getCardNumber()));
        message.setField(3, new IsoValue<Integer>(IsoType.NUMERIC, 000000, 6));
        message.setField(4, new IsoValue<Integer>(IsoType.NUMERIC, money.getAmountMinorInt(), 12));
        message.setField(12, new IsoValue<String>(IsoType.NUMERIC, df.format(new Date()), 12));
        message.setField(14, new IsoValue<String>(IsoType.NUMERIC, expire, 4));
        message.setField(22, new IsoValue<String>(IsoType.ALPHA, pointOfService, 12));
        message.setField(24, new IsoValue<String>(IsoType.NUMERIC, function, 3));
        message.setField(25, new IsoValue<String>(IsoType.NUMERIC, reason, 4));
        message.setField(26, new IsoValue<String>(IsoType.NUMERIC, "0000", 4));
        message.setField(31, new IsoValue<String>(IsoType.LLVAR, orderId, 20));
        message.setField(41, new IsoValue<String>(IsoType.ALPHA, terminalId, 8));
        message.setField(42, new IsoValue<String>(IsoType.ALPHA, merchant.getMerchantId(), 15));
        message.setField(43, new IsoValue<String>(IsoType.LLVAR, address.toString(), 99));
        message.setField(47, new IsoValue<String>(IsoType.LLVAR, "V503" + cvdFormat.format(card.getCvd())));
        message.setField(49, new IsoValue<String>(IsoType.ALPHA, money.getCurrencyUnit().getCurrencyCode(), 3));
        //message.setField(56, new IsoValue<String>(IsoType.BINARY, "701405C200E28000", 8));
        //message.setField(57, new IsoValue<String>(IsoType.BINARY, "701405C200E28000", 8));
        
        byte[] packet = message.writeToBuffer(0).array();
        
        //Append special Nets header
        IsoValue length = new IsoValue(IsoType.BINARY, packet.length + 32, 2);
        IsoValue fixedHeader = new IsoValue(IsoType.BINARY, new byte[]{0, 0, 82, 72, 0, 2, 32, 0, 0, 0, 0, 0, 0, 50, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, 26);
        IsoValue networkCode = new IsoValue(IsoType.BINARY, "0000", 2);
        IsoValue fixedField = new IsoValue(IsoType.BINARY, "0000", 2);
        
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        length.write(buf, true);
        fixedHeader.write(buf, true);
        networkCode.write(buf, true);
        fixedField.write(buf, true);
        buf.write(packet);
        
        
        HttpHost host = new HttpHost(netsServiceUrl.getHost(), netsServiceUrl.getPort(), netsServiceUrl.getProtocol());
        HttpPost postMethod = new HttpPost(netsServiceUrl.getPath());
        ByteArrayEntity entity = new ByteArrayEntity(buf.toByteArray());
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
