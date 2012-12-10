package dk.apaq.nets.payment;

import com.solab.iso8583.parse.*;
import dk.apaq.framework.repository.CollectionRepository;
import dk.apaq.framework.repository.Repository;
import dk.apaq.nets.payment.io.*;
import dk.apaq.nets.test.AbstractMockNetsServer;
import dk.apaq.nets.test.Bank;
import dk.apaq.nets.test.MockNetsSocketServer;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

import dk.apaq.nets.test.Slf4jChannelLogger;
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
    private ChannelLogger channelLogger = new Slf4jChannelLogger();
    Nets nets = null;
    Merchant merchant = new Merchant("123", "Smith Radio", new Address("Boulevard 4", "3266", "Broby", "DNK"));
        
    private Repository<TransactionData, String> repository = new CollectionRepository<TransactionData>(new CollectionRepository.IdResolver<TransactionData>() {

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
        netsServer.getBank().addCard(new Card(CARDNO_VALID_VISA_2, 12, 12, "123"), 1000000000000000000L);
        netsServer.start(4444);
        
        serverUrl = "http://" + address.getHostName() + ":12345/service";
        nets = new Nets(new SslSocketChannelFactory(Inet4Address.getLocalHost().getHostAddress(), 4444, channelLogger, 500), repository);
        nets.setMinWaitBetweenAttempts(500);
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
        
        TransactionData data = repository.findOne(merchant.getMerchantId()+"_"+orderId);
        assertEquals(ActionCode.Approved , data.getActionCode());
        assertNotNull(data.getOde());
        assertEquals(255, data.getOde().length());
        assertTrue(data.getOde().startsWith("oDe123"));
        
        Bank.Transaction t = netsServer.getBank().getTransaction(data.getOde());
        assertNotNull(t);
        assertTrue(t.isAuthorized());
        assertEquals(money.getAmountMinorInt(), t.getAmount());
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
        nets.reverse(merchant, orderId);
        
        TransactionData data = repository.findOne(merchant.getMerchantId()+"_"+orderId);
        assertEquals(ActionCode.Approved , data.getActionCode());
        assertNotNull(data.getOde());
        
        Bank.Transaction t = netsServer.getBank().getTransaction(data.getOde());
        assertNotNull(t);
        assertTrue(t.isCancelled());
    }
    
    @Test
    public void testCapture() throws Exception {
        System.out.println("capture");
        Card card = new Card(CARDNO_VALID_VISA_1, 12, 12, "123");
        Money money = Money.of(CurrencyUnit.USD, 12.2);
        String orderId = "orderid";
        //Need to authorize first
        nets.authorize(merchant, card, money, orderId);
        
        //Now capture
        nets.capture(merchant, money, orderId);
        
        TransactionData data = repository.findOne(merchant.getMerchantId()+"_"+orderId);
        assertEquals(ActionCode.Approved , data.getActionCode());
        assertNotNull(data.getOde());
                
        Bank.Transaction t = netsServer.getBank().getTransaction(data.getOde());
        assertNotNull(t);
        assertTrue(t.isCaptured());
    }
    
    @Test
    public void testRefund() throws Exception {
        System.out.println("refund");
        Card card = new Card(CARDNO_VALID_VISA_1, 12, 12, "123");
        Money money = Money.of(CurrencyUnit.USD, 12.2);
        String orderId = "orderid";
        
        //Need to authorize first
        nets.authorize(merchant, card, money, orderId);
        
        //Now capture
        nets.capture(merchant, money, orderId);
        
        //Now refund
        nets.credit(merchant, money, orderId);
        
        TransactionData data = repository.findOne(merchant.getMerchantId()+"_"+orderId);
        assertEquals(ActionCode.Approved , data.getActionCode());
        assertNotNull(data.getOde());
        
        Bank.Transaction t = netsServer.getBank().getTransaction(data.getOde());
        assertNotNull(t);
        assertTrue(t.isRefunded());
    }
    
    @Test
    public void testPerformance() throws Exception {
        System.out.println("performance");
        Card card = new Card(CARDNO_VALID_VISA_2, 12, 12, "123");
        Money money = Money.of(CurrencyUnit.USD, 12.2);
        String orderId = "orderid";

        //Bootstrap
        nets.authorize(merchant, card, money, orderId);

        long start = System.currentTimeMillis();
        for(int i=0;i<100;i++) {
            String currentId = orderId + "_" + i;
            //Need to authorize first
            nets.authorize(merchant, card, money, currentId);

            //Now capture
            nets.capture(merchant, money, currentId);
        }
        long end = System.currentTimeMillis();
        long time = end-start;
        
        System.out.println("Done in " + time + "ms.");
        assertTrue("Did not complete within 10000ms (it took " + time + "ms)", time<10000);
    }
}
