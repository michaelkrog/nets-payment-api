package dk.apaq.nets.payment.io;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.IsoValue;
import com.solab.iso8583.MessageFactory;
import com.solab.iso8583.parse.AlphaParseInfo;
import com.solab.iso8583.parse.FieldParseInfo;
import com.solab.iso8583.parse.LllvarParseInfo;
import com.solab.iso8583.parse.LlvarParseInfo;
import com.solab.iso8583.parse.NumericParseInfo;
import dk.apaq.nets.payment.ActionCode;
import dk.apaq.nets.payment.FunctionCode;
import dk.apaq.nets.payment.MessageFields;
import dk.apaq.nets.payment.MessageReason;
import dk.apaq.nets.payment.MessageTypes;
import dk.apaq.nets.payment.PGTMHeader;
import dk.apaq.nets.payment.PointOfService;
import dk.apaq.nets.payment.ProcessingCode;
import dk.apaq.nets.payment.PsipHeader;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicHttpResponse;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mockito.Mockito;

/**
 *
 * @author michael
 */
public class HttpChannelTest {
    
    public HttpChannelTest() {
        expireFormat.setMinimumIntegerDigits(2);
        expireFormat.setMaximumIntegerDigits(2);
        
        Map<Integer, FieldParseInfo> authRespFields = new HashMap<Integer, FieldParseInfo>();
        authRespFields.put(MessageFields.PRIMARY_ACCOUNT_NUMBER, new LlvarParseInfo());
        authRespFields.put(MessageFields.PROCESSING_CODE, new NumericParseInfo(6));
        authRespFields.put(MessageFields.AMOUNT, new NumericParseInfo(12));
        authRespFields.put(MessageFields.LOCAL_TIME, new NumericParseInfo(12));
        authRespFields.put(MessageFields.ACQUIRER_REFERENCE, new LlvarParseInfo());
        authRespFields.put(MessageFields.APPROVAL_CODE, new AlphaParseInfo(6));
        authRespFields.put(MessageFields.ACTION_CODE, new NumericParseInfo(3));
        authRespFields.put(MessageFields.CARD_ACCEPTOR_TERMINAL_ID, new AlphaParseInfo(8));
        authRespFields.put(MessageFields.CARD_ACCEPTOR_IDENTIFICATION_CODE, new AlphaParseInfo(15));
        authRespFields.put(MessageFields.ADDITIONAL_RESPONSE_DATA, new LlvarParseInfo());
        authRespFields.put(MessageFields.ADDITIONAL_DATA_NATIONAL, new LllvarParseInfo());
        authRespFields.put(MessageFields.CURRENCY_CODE, new AlphaParseInfo(3));
        authRespFields.put(MessageFields.AUTH_ODE, new LllvarParseInfo());
        messageFactory.setParseMap(MessageTypes.AUTHORIZATION_RESPONSE, authRespFields);
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    private MessageFactory messageFactory = new MessageFactory();
    private final NumberFormat expireFormat = NumberFormat.getIntegerInstance();
    private final DateFormat df = new SimpleDateFormat("yyMMddHHmmss");
    
    
    /**
     * Test of sendMessage method, of class HttpChannel.
     */
    @Test
    public void testSendMessage() throws Exception {
        System.out.println("sendMessage");
        
        IsoMessage requestMessage = buildMessage();
        
        PGTMHeader responseHeader = new PGTMHeader(32, "0000");
        IsoMessage responseMessage = new IsoMessage();
        responseMessage.setIsoHeader("PSIP100000");
        responseMessage.setType(MessageTypes.AUTHORIZATION_RESPONSE);
        responseMessage.copyFieldsFrom(requestMessage, MessageFields.PRIMARY_ACCOUNT_NUMBER,
                                        MessageFields.PROCESSING_CODE,
                                        MessageFields.AMOUNT,
                                        MessageFields.LOCAL_TIME,
                                        MessageFields.CARD_ACCEPTOR_TERMINAL_ID,
                                        MessageFields.CARD_ACCEPTOR_IDENTIFICATION_CODE,
                                        MessageFields.ADDITIONAL_DATA_NATIONAL,
                                        MessageFields.CURRENCY_CODE);
        
        responseMessage.setValue(MessageFields.APPROVAL_CODE, "12345", IsoType.ALPHA, 6);
        responseMessage.setValue(MessageFields.ACTION_CODE, ActionCode.Approved.getCode(), IsoType.NUMERIC, 3);
        responseMessage.setValue(MessageFields.AUTH_ODE, "11", IsoType.LLLVAR, 255);
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        buf.write(responseHeader.toByteArray());
        responseMessage.write(buf, 0);
        
        HttpClient client = Mockito.mock(HttpClient.class);
        HttpResponse response = new BasicHttpResponse(new ProtocolVersion("http", 1, 1), 200, "OK");
        response.setEntity(new ByteArrayEntity(buf.toByteArray()));
        
        Mockito.when(client.execute(Mockito.any(HttpHost.class), Mockito.any(HttpPost.class))).thenReturn(response);
        
        HttpChannel instance = new HttpChannel(messageFactory, client, new URL("http://localhost:1234"));
        IsoMessage result = instance.sendMessage(requestMessage);
        
        assertNotNull(result);
        
    }
    
    protected IsoMessage buildMessage() {
        IsoMessage message = messageFactory.newMessage(MessageTypes.AUTHORIZATION_REQUEST);
        message.setIsoHeader(PsipHeader.OK.toString());
        String expire = expireFormat.format(2012) + expireFormat.format(6);
        String processingCode = ProcessingCode.GoodsAndServices.getCode();
        String pointOfService = PointOfService.InternetMerchant.getCode();
        String function = FunctionCode.Authorize_Original_Accurate_Amount.getCode();
        String reason = MessageReason.NormalTransaction.getCode();
        String address = buildAddressField();
        message.setField(2, new IsoValue<String>(IsoType.LLVAR, "Cardholder"));
        message.setField(3, new IsoValue<String>(IsoType.NUMERIC, processingCode, 6));
        message.setField(4, new IsoValue<Integer>(IsoType.NUMERIC, 123, 12));
        message.setField(12, new IsoValue<String>(IsoType.NUMERIC, df.format(new Date()), 12));
        message.setField(14, new IsoValue<String>(IsoType.NUMERIC, expire, 4));
        message.setField(22, new IsoValue<String>(IsoType.ALPHA, pointOfService, 12));
        message.setField(24, new IsoValue<String>(IsoType.NUMERIC, function, 3));
        message.setField(25, new IsoValue<String>(IsoType.NUMERIC, reason, 4));
        message.setField(26, new IsoValue<String>(IsoType.NUMERIC, "0000", 4));
        message.setField(31, new IsoValue<String>(IsoType.LLVAR, "orderId"));
        message.setField(41, new IsoValue<String>(IsoType.ALPHA, fillString("123", ' ', 8), 8));
        message.setField(42, new IsoValue<String>(IsoType.ALPHA, "merchantID", 15));
        message.setField(43, new IsoValue<String>(IsoType.LLVAR, address, 99));
        message.setField(47, new IsoValue<String>(IsoType.LLLVAR, "V503" + "123"));
        message.setField(49, new IsoValue<String>(IsoType.ALPHA, "DNK", 3));
        return message;
    }
    
    protected String buildAddressField() {
            StringBuilder address = new StringBuilder();
            address.append("John Doe");
            address.append("\\");
            address.append("11 Doe Street");
            address.append("\\");
            address.append("Doe City");
            address.append("\\");

            address.append("12345");
            fillString(address, ' ', 96);

            address.append("DNK");
            return address.toString();
        }
    
    protected void fillString(StringBuilder builder, char character, int length) {
            if (builder.length() > length) {
                builder.delete(length, builder.length());
            } else {
                for (int i = builder.length(); i < length; i++) {
                    builder.append(character);
                }
            }
        }
    
    protected String fillString(String value, char character, int length) {
            StringBuilder sb = new StringBuilder(value);
            fillString(sb, character, length);
            return sb.toString();
        }
}
