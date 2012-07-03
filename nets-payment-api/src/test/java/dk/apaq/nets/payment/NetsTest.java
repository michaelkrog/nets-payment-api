package dk.apaq.nets.payment;

import com.solab.iso8583.MessageFactory;
import com.solab.iso8583.parse.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import dk.apaq.nets.test.MockNetsServer;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author krog
 */
public class NetsTest {
    
    private String authReqHex =   "00D80000524800022000000000000032"
                                + "00000000000000000000000000000000"
                                + "50534950313030303030313130307014"
                                + "05C200E2800031363530313939393430"
                                + "30303132333533313030303030303030"
                                + "30303030303030383735303930373037"
                                + "313131383336313431324B3030353030"
                                + "4B303031333031303030303030353733"
                                + "32303538383838383132332020202020"
                                + "31393738353531202020202020202034"
                                + "36536D69746820526164696F5C426F75"
                                + "6C657661726420345C42726F62795C33"
                                + "323636202020202020444B20444E4B30"
                                + "3033363034444B4B";
    
    
    private HttpServer server;
    private MockNetsServer netsServer = new MockNetsServer();
    private NetsHandler netsHandler = new NetsHandler();
    private String serverUrl;
    private MessageFactory messageFactory = new MessageFactory();
    
    private class NetsHandler implements HttpHandler {

        private byte[] lastMessageReceived;
        
        public void handle(HttpExchange he) throws IOException {
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            IOUtils.copy(he.getRequestBody(), buf);
            lastMessageReceived = buf.toByteArray();
            he.sendResponseHeaders(HttpURLConnection.HTTP_OK,0);
            he.getResponseBody().close();
        }

        public byte[] getLastMessageReceived() {
            return lastMessageReceived;
        }
        
        
        
    }

    @Before
    public void setUp() throws Exception {
        //messageFactory.setUseBinaryMessages(true);
        Map<Integer, FieldParseInfo> authReqFields = new HashMap<Integer, FieldParseInfo>();
        authReqFields.put(2, new LlvarParseInfo());
        authReqFields.put(3, new NumericParseInfo(6));
        authReqFields.put(4, new NumericParseInfo(12));
        authReqFields.put(12, new NumericParseInfo(12));
        authReqFields.put(14, new NumericParseInfo(4));
        authReqFields.put(22, new AlphaParseInfo(12));
        authReqFields.put(24, new NumericParseInfo(3));
        authReqFields.put(25, new NumericParseInfo(4));
        authReqFields.put(26, new NumericParseInfo(4));
                
        InetAddress address = Inet4Address.getLocalHost();
        netsServer.start(12345);
        
        serverUrl = "http://" + address.getHostName() + ":12345/service";
    }
    
    @After
    public void tearDown() {
        netsServer.stop();
    }


    /**
     * Test of authorize method, of class Nets.
     */
    @Test
    public void testAuthorize() throws Exception {
        System.out.println("authorize");
        
        DefaultHttpClient client = new DefaultHttpClient();
        Nets nets = new Nets(serverUrl, client);
        
        Merchant merchant = new Merchant("123", "Smith Radio", new Address("Boulevard 4", "3266", "Broby", "DNK"));
        Card card = new Card("cardno", 12, 11, 123);
        Money money = Money.of(CurrencyUnit.USD, 12.2);
        String orderId = "orderid";
        boolean recurring = false;
        boolean fraudSuspect = false;
        String terminalId = "test";
        NetsResponse response = nets.authorize(merchant, card, money, orderId, recurring, fraudSuspect, terminalId);
        
        assertEquals(ActionCode.Approved , response.getActionCode());
        assertNotNull(response.getOde());

    }

    @Test
    public void testCancel() throws Exception {
        System.out.println("cancel");
        DefaultHttpClient client = new DefaultHttpClient();
        Nets nets = new Nets(serverUrl, client);
        /*
        Merchant merchant = new Merchant("123", "Smith Radio", new Address("Boulevard 4", "3266", "Broby", "DNK"));
        Card card = new Card("cardno", 12, 11, 123);
        Money money = Money.of(CurrencyUnit.USD, 12.2);
        String orderId = "orderid";
        boolean recurring = false;
        boolean fraudSuspect = false;
        String terminalId = "test";
        NetsResponse response = nets.cancel();
        
        assertEquals(ActionCode.Approved , response.getActionCode());
        assertNotNull(response.getOde());*/
    }
    
    /*
    @Test
    public void testRenewAuthorization() {
        System.out.println("renewAuthorization");
        Nets instance = null;
        instance.renewAuthorization();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    
    @Test
    public void testRefund() {
        System.out.println("refund");
        Nets instance = null;
        instance.refund();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Test
    public void testCapture() {
        System.out.println("capture");
        Nets instance = null;
        instance.capture();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    
    */
}
