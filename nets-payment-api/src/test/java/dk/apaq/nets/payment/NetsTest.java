package dk.apaq.nets.payment;

import com.solab.iso8583.parse.*;
import dk.apaq.crud.Crud;
import dk.apaq.crud.core.CollectionCrud;
import dk.apaq.nets.payment.io.*;
import dk.apaq.nets.test.AbstractMockNetsServer;
import dk.apaq.nets.test.MockNetsHttpServer;
import dk.apaq.nets.test.MockNetsSocketServer;
import java.io.File;
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
    
    static {
        System.setProperty("javax.net.ssl.trustStore", "src/test/resources/keystore");
    }
    
    private AbstractMockNetsServer netsServer = new MockNetsSocketServer();
    //private AbstractMockNetsServer netsServer = new MockNetsHttpServer();
    private String serverUrl;
    private File logDir = new File("target/log");
    private HexDumpChannelLogger channelLogger = new HexDumpChannelLogger(logDir);
    Nets nets = null;
    Merchant merchant = new Merchant("123", "Smith Radio", new Address("Boulevard 4", "3266", "Broby", "DNK"));
        
    private Crud.Editable<String, TransactionData> crud = new CollectionCrud<TransactionData>(new CollectionCrud.IdResolver<TransactionData>() {

        public String getIdForBean(TransactionData bean) {
            return bean.getId();
        }
    });
    
    @Before
    public void setUp() throws Exception {
        logDir.mkdirs();
        
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
        nets = new Nets(new SslSocketChannelFactory("nkm17v85", 12345, channelLogger), crud);
        //nets = new Nets(new HttpChannelFactory(new URL(serverUrl), channelLogger), crud);

    }
    
    @After
    public void tearDown() {
        netsServer.stop();
    }



    @Test
    public void testAuthorize() throws Exception {
        System.out.println("authorize");
        
        Card card = new Card(CARDNO_VALID_VISA_1, 12, 12, "123");
        Money money = Money.of(CurrencyUnit.USD, 12.2);
        String orderId = "orderid";
        nets.authorize(merchant, card, money, orderId);
        
        TransactionData data = crud.read(merchant.getMerchantId()+"_"+orderId);
        assertEquals(ActionCode.Approved , data.getActionCode());
        assertNotNull(data.getOde());
        assertEquals(255, data.getOde().length());
        assertTrue(data.getOde().startsWith("oDe123"));

    }
    
    @Test
    public void testNetworkError() throws Exception {
        System.out.println("authorizeNetworkError");
        
        nets.setMaxRequestAttempts(1);
        
        Card card = new Card(CARDNO_VALID_VISA_1, 12, 12, "123");
        Money money = Money.of(CurrencyUnit.USD, 12.2);
        String orderId = "orderid";
        netsServer.setNextRequestFails(true);
        
        try {
            nets.authorize(merchant, card, money, orderId);
            fail("Should have failed");
        } catch(IOException ex) {
            
        }

    }
    
     @Test
    public void testAuthorizeInsuffecientFunds() throws Exception {
        System.out.println("authorizeInsuffecientFunds");
        
        Card card = new Card(CARDNO_VALID_VISA_1, 12, 12, "123");
        Money money = Money.of(CurrencyUnit.USD, 131231.2);
        String orderId = "orderid";
        
        try {
            nets.authorize(merchant, card, money, orderId);
            fail("Should not be approved.");
        } catch(NetsException ex) {
            assertEquals(ActionCode.Insufficient_Funds , ex.getActionCode());
        }

    }

    @Test
    public void testCancel() throws Exception {
        System.out.println("cancel");
        Card card = new Card(CARDNO_VALID_VISA_1, 12, 12, "123");
        Money money = Money.of(CurrencyUnit.USD, 12.2);
        String orderId = "orderid";
        String terminalId = "test";
        String approvalCode = "app";
        
        //Need to authorize first
        nets.authorize(merchant, card, money, orderId);
        
        //Now cancel
        nets.reverse(merchant, card, orderId);
        
        TransactionData data = crud.read(merchant.getMerchantId()+"_"+orderId);
        assertEquals(ActionCode.Approved , data.getActionCode());
        assertNotNull(data.getOde());
    }
    
    @Test
    public void testCapture() throws Exception {
        System.out.println("cancel");
        Card card = new Card(CARDNO_VALID_VISA_1, 12, 12, "123");
        Money money = Money.of(CurrencyUnit.USD, 12.2);
        String orderId = "orderid";
        
        //Need to authorize first
        nets.authorize(merchant, card, money, orderId);
        
        //Now capture
        nets.capture(merchant, card, money, orderId);
        
        TransactionData data = crud.read(merchant.getMerchantId()+"_"+orderId);
        assertEquals(ActionCode.Approved , data.getActionCode());
        assertNotNull(data.getOde());
    }
    
    /*
    
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
