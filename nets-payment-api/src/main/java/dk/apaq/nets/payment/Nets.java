package dk.apaq.nets.payment;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.IsoValue;
import com.solab.iso8583.MessageFactory;
import com.solab.iso8583.parse.AlphaParseInfo;
import com.solab.iso8583.parse.FieldParseInfo;
import com.solab.iso8583.parse.LllbinParseInfo;
import com.solab.iso8583.parse.LllvarParseInfo;
import com.solab.iso8583.parse.LlvarParseInfo;
import com.solab.iso8583.parse.NumericParseInfo;
import dk.apaq.nets.payment.io.Channel;
import dk.apaq.nets.payment.io.ChannelFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.IOUtils;
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
public class Nets {

    private static final byte[] NETS_CUSTOM_HEADER = {0, -40, 0, 0, 82, 72, 0, 2, 32, 0, 0, 0, 0, 0, 0, 50, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    
    private final ChannelFactory channelFactory;
    private final DateFormat df = new SimpleDateFormat("yyMMddHHmmss");
    private final NumberFormat expireFormat = NumberFormat.getIntegerInstance();
    private final NumberFormat cvdFormat = NumberFormat.getIntegerInstance();
    
    private void init() {
        Map<Integer, FieldParseInfo> authRespFields = new HashMap<Integer, FieldParseInfo>();
        authRespFields.put(MessageFields.FIELD_INDEX_PRIMARY_ACCOUNT_NUMBER, new LlvarParseInfo());
        authRespFields.put(MessageFields.FIELD_INDEX_PROCESSING_CODE, new NumericParseInfo(6));
        authRespFields.put(MessageFields.FIELD_INDEX_AMOUNT, new NumericParseInfo(12));
        authRespFields.put(MessageFields.FIELD_INDEX_LOCAL_TIME, new NumericParseInfo(12));
        authRespFields.put(MessageFields.FIELD_INDEX_ACQUIRER_REFERENCE, new LlvarParseInfo());
        authRespFields.put(MessageFields.FIELD_INDEX_APPROVAL_CODE, new AlphaParseInfo(6));
        authRespFields.put(MessageFields.FIELD_INDEX_ACTION_CODE, new NumericParseInfo(3));
        authRespFields.put(MessageFields.FIELD_INDEX_CARD_ACCEPTOR_TERMINAL_ID, new AlphaParseInfo(8));
        authRespFields.put(MessageFields.FIELD_INDEX_CARD_ACCEPTOR_IDENTIFICATION_CODE, new AlphaParseInfo(15));
        authRespFields.put(MessageFields.FIELD_INDEX_ADDITIONAL_RESPONSE_DATA, new LlvarParseInfo());
        authRespFields.put(MessageFields.FIELD_INDEX_ADDITIONAL_DATA_NATIONAL, new LllvarParseInfo());
        authRespFields.put(MessageFields.FIELD_INDEX_CURRENCY_CODE, new AlphaParseInfo(3));
        authRespFields.put(MessageFields.FIELD_INDEX_AUTH_ODE, new LllbinParseInfo());
        
        channelFactory.getMessageFactory().setParseMap(MessageTypes.AUTHORIZATION_RESPONSE, authRespFields);
    }

    //TODO Change from using a http client to a socket client
    public Nets(ChannelFactory channelFactory) throws MalformedURLException {
        this.channelFactory = channelFactory;
        
        expireFormat.setMinimumIntegerDigits(2);
        expireFormat.setMaximumIntegerDigits(2);
        cvdFormat.setMinimumIntegerDigits(3);
        cvdFormat.setMaximumIntegerDigits(3);
        
        init();
    }
    
    
    public NetsResponse authorize(Merchant merchant, Card card, Money money, String orderId, boolean recurring, boolean fraudSuspect, String terminalId) throws IOException {
        IsoMessage message = channelFactory.getMessageFactory().newMessage(MessageTypes.AUTHORIZATION_REQUEST);
        
        message.setIsoHeader("PSIP100000");
        
        String expire = expireFormat.format(card.getExpireYear()) + expireFormat.format(card.getExpireMonth());
        String pointOfService = recurring ? "K00540K00130" : "K00500K00130";
        String function = FunctionCode.Original_Accurate_Amount.getCode();
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
        
        String ode = "";
        for(int i=0;i<255;i++) {
            ode +="x";
        }
        
        
        message.setField(2, new IsoValue<String>(IsoType.LLVAR, card.getCardNumber()));
        message.setField(3, new IsoValue<Integer>(IsoType.NUMERIC, 000000, 6));
        message.setField(4, new IsoValue<Integer>(IsoType.NUMERIC, money.getAmountMinorInt(), 12));
        message.setField(12, new IsoValue<String>(IsoType.NUMERIC, df.format(new Date()), 12));
        message.setField(14, new IsoValue<String>(IsoType.NUMERIC, expire, 4));
        message.setField(22, new IsoValue<String>(IsoType.ALPHA, pointOfService, 12));
        message.setField(24, new IsoValue<String>(IsoType.NUMERIC, function, 3));
        message.setField(25, new IsoValue<String>(IsoType.NUMERIC, reason, 4));
        message.setField(26, new IsoValue<String>(IsoType.NUMERIC, "0000", 4));
        message.setField(31, new IsoValue<String>(IsoType.LLVAR, orderId));
        message.setField(41, new IsoValue<String>(IsoType.ALPHA, terminalId, 8));
        message.setField(42, new IsoValue<String>(IsoType.ALPHA, merchant.getMerchantId(), 15));
        message.setField(43, new IsoValue<String>(IsoType.LLVAR, address.toString(), 99));
        message.setField(47, new IsoValue<String>(IsoType.LLLVAR, "V503" + cvdFormat.format(card.getCvd())));
        message.setField(49, new IsoValue<String>(IsoType.ALPHA, money.getCurrencyUnit().getCurrencyCode(), 3));
        message.setField(56, new IsoValue<String>(IsoType.LLLVAR, ode, 255));
        message.setField(57, new IsoValue<String>(IsoType.BINARY, "220", 3));
        
        Channel channel = channelFactory.createChannel();
        message = channel.sendMessage(message);
        
        if(message == null) {
            throw new IOException("Message could not be parsed. Unknown message type?");
        }
        
        String actionCode = message.getField(MessageFields.FIELD_INDEX_ACTION_CODE).toString();
        ode = message.getField(MessageFields.FIELD_INDEX_AUTH_ODE).toString();
        
        //TODO Do something with the message and return meaningfull stuff
        return new NetsResponse(ActionCode.fromCode(actionCode), ode);
        
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
