package dk.apaq.nets.payment;

import com.solab.iso8583.parse.*;
import dk.apaq.nets.payment.io.HttpChannelFactory;
import dk.apaq.nets.test.MockNetsServer;
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
        netsServer.getBank().addCard(new Card("45711234123412341234", 12, 12, "123"), 100000);
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
        
        Nets nets = new Nets(new HttpChannelFactory(new URL(serverUrl)));
        
        Merchant merchant = new Merchant("123", "Smith Radio", new Address("Boulevard 4", "3266", "Broby", "DNK"));
        Card card = new Card("45711234123412341234", 12, 12, "123");
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
        Nets nets = new Nets(new HttpChannelFactory(new URL(serverUrl)));
        
        Merchant merchant = new Merchant("123", "Smith Radio", new Address("Boulevard 4", "3266", "Broby", "DNK"));
        Card card = new Card("45711234123412341234", 12, 12, "123");
        Money money = Money.of(CurrencyUnit.USD, 12.2);
        String orderId = "orderid";
        boolean recurring = false;
        boolean fraudSuspect = false;
        String terminalId = "test";
        String approvalCode = "app";
        String ode = "ode                                                                                                                                                                                                                                                            ";
        NetsResponse response = nets.cancel(merchant, card, money, orderId, terminalId, approvalCode, ode);
        
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
