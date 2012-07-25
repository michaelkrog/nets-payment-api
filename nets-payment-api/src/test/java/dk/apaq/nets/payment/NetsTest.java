package dk.apaq.nets.payment;

import com.solab.iso8583.parse.*;
import dk.apaq.nets.payment.io.Channel;
import dk.apaq.nets.payment.io.ChannelFactory;
import dk.apaq.nets.payment.io.HttpChannelFactory;
import dk.apaq.nets.test.MockNetsServer;
import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author krog
 */
public class NetsTest {
    
    private static final String CARDNO_VALID_VISA_1 = "45711234123412341234";
    private static final String CARDNO_VALID_VISA_2 = "45711234123412341235";
    private static final String CARDNO_INVALID = "00001234123412341234";
    
    private MockNetsServer netsServer = new MockNetsServer();
    private String serverUrl;
    
    @Before
    public void setUp() throws Exception {
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
        netsServer.getBank().addCard(new Card(CARDNO_VALID_VISA_1, 12, 12, "123"), 100000);
        netsServer.start(12345);
        
        serverUrl = "http://" + address.getHostName() + ":12345/service";
    }
    
    @After
    public void tearDown() {
        netsServer.stop();
    }



    @Test
    public void testAuthorize() throws Exception {
        System.out.println("authorize");
        
        Nets nets = new Nets(new HttpChannelFactory(new URL(serverUrl)), new MemPersistence());
        
        Merchant merchant = new Merchant("123", "Smith Radio", new Address("Boulevard 4", "3266", "Broby", "DNK"));
        Card card = new Card(CARDNO_VALID_VISA_1, 12, 12, "123");
        Money money = Money.of(CurrencyUnit.USD, 12.2);
        String orderId = "orderid";
        NetsResponse response = nets.authorize(merchant, card, money, orderId).send();
        
        assertEquals(ActionCode.Approved , response.getActionCode());
        assertNotNull(response.getOde());
        assertEquals(255, response.getOde().length());
        assertTrue(response.getOde().startsWith("oDe123"));

    }
    
    @Test
    public void testNetworkError() throws Exception {
        System.out.println("authorizeNetworkError");
        
        Nets nets = new Nets(new HttpChannelFactory(new URL(serverUrl)), new MemPersistence());
        
        Merchant merchant = new Merchant("123", "Smith Radio", new Address("Boulevard 4", "3266", "Broby", "DNK"));
        Card card = new Card(CARDNO_VALID_VISA_1, 12, 12, "123");
        Money money = Money.of(CurrencyUnit.USD, 12.2);
        String orderId = "orderid";
        netsServer.setNextRequestFails(true);
        
        try {
            NetsResponse response = nets.authorize(merchant, card, money, orderId).send();
            fail("Should have failed");
        } catch(IOException ex) {
            
        }

    }
    
     @Test
    public void testAuthorizeInsuffecientFunds() throws Exception {
        System.out.println("authorizeInsuffecientFunds");
        
        Nets nets = new Nets(new HttpChannelFactory(new URL(serverUrl)), new MemPersistence());
        
        Merchant merchant = new Merchant("123", "Smith Radio", new Address("Boulevard 4", "3266", "Broby", "DNK"));
        Card card = new Card(CARDNO_VALID_VISA_1, 12, 12, "123");
        Money money = Money.of(CurrencyUnit.USD, 131231.2);
        String orderId = "orderid";
        NetsResponse response = nets.authorize(merchant, card, money, orderId).send();
        
        assertEquals(ActionCode.Insufficient_Funds , response.getActionCode());
        assertNotNull(response.getOde());

    }

    @Test
    public void testCancel() throws Exception {
        System.out.println("cancel");
        Nets nets = new Nets(new HttpChannelFactory(new URL(serverUrl)), new MemPersistence());
        
        Merchant merchant = new Merchant("123", "Smith Radio", new Address("Boulevard 4", "3266", "Broby", "DNK"));
        Card card = new Card(CARDNO_VALID_VISA_1, 12, 12, "123");
        Money money = Money.of(CurrencyUnit.USD, 12.2);
        String orderId = "orderid";
        String terminalId = "test";
        String approvalCode = "app";
        
        //Need to authorize first
        NetsResponse response = nets.authorize(merchant, card, money, orderId).send();
        assertEquals(ActionCode.Approved , response.getActionCode());
        
        //Now cancel
        response = nets.reverse(merchant, card, orderId).send();
        
        assertEquals(ActionCode.Approved , response.getActionCode());
        assertNotNull(response.getOde());
    }
    
    @Test
    public void testCapture() throws Exception {
        System.out.println("cancel");
        Nets nets = new Nets(new HttpChannelFactory(new URL(serverUrl)), new MemPersistence());
        
        Merchant merchant = new Merchant("123", "Smith Radio", new Address("Boulevard 4", "3266", "Broby", "DNK"));
        Card card = new Card(CARDNO_VALID_VISA_1, 12, 12, "123");
        Money money = Money.of(CurrencyUnit.USD, 12.2);
        String orderId = "orderid";
        
        //Need to authorize first
        NetsResponse response = nets.authorize(merchant, card, money, orderId).send();
        assertEquals(ActionCode.Approved , response.getActionCode());
        
        //Now capture
        String approvalCode = ""; //TODO Get from auth response instead
        response = nets.capture(merchant, card, money, orderId).send();
        
        assertEquals(ActionCode.Approved , response.getActionCode());
        assertNotNull(response.getOde());
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
