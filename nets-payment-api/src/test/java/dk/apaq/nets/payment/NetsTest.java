package dk.apaq.nets.payment;

import com.solab.iso8583.parse.*;
import dk.apaq.nets.payment.io.*;
import dk.apaq.nets.test.AbstractMockNetsServer;
import dk.apaq.nets.test.Bank;
import dk.apaq.nets.test.MockNetsSocketServer;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

import dk.apaq.framework.common.beans.finance.Card;
import dk.apaq.nets.test.Slf4jChannelLogger;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author krog
 */
public class NetsTest {
    
    private static final String CARDNO_VALID_VISA_1 = "4571123412341234";
    private static final String CARDNO_VALID_VISA_2 = "4571123412341235";
    private static final String CARDNO_INVALID = "0000123412341234";
    
    static {
        System.setProperty("javax.net.ssl.trustStore", "src/test/resources/keystore");
    }
    
    private StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
    private AbstractMockNetsServer netsServer = new MockNetsSocketServer(encryptor, 4444);
    private String serverUrl;
    private File logDir = new File("target/log");
    private ChannelLogger channelLogger = new Slf4jChannelLogger();
    private Nets nets = null;
    private Merchant merchant = new Merchant("123", "Smith Radio", new Address("Boulevard 4", "3266", "Broby", "DNK"));
    
    @Before
    public void setUp() throws Exception {
        encryptor.setPassword("qwerty");
        logDir.mkdirs();
        
        InetAddress address = Inet4Address.getLocalHost();
        netsServer.getBank().addCard(new Card(CARDNO_VALID_VISA_1, 12, 12, "123", encryptor), 100000);
        netsServer.getBank().addCard(new Card(CARDNO_VALID_VISA_2, 12, 12, "123", encryptor), 1000000000000000000L);
        netsServer.start();
        
        serverUrl = "http://" + address.getHostName() + ":12345/service";
        nets = new Nets(new SslSocketChannelFactory(Inet4Address.getLocalHost().getHostAddress(), 4444, channelLogger, 500), encryptor);
        nets.setMinWaitBetweenAttempts(500);
    }
    
    @After
    public void tearDown() {
        netsServer.stop();
    }



    @Test
    public void testAuthorize() throws Exception {
        System.out.println("authorize");
        
        Card card = new Card(CARDNO_VALID_VISA_1, 12, 12, "123", encryptor);
        Money money = Money.of(CurrencyUnit.USD, 12.2);
        String orderId = "orderid";
        NetsResponse response = nets.authorize(merchant, card, money, orderId);
        
        assertEquals(ActionCode.Approved , response.getActionCode());
        assertNotNull(response.getOde());
        assertEquals(255, response.getOde().length());
        assertTrue(response.getOde().startsWith("oDe123"));
        
        Bank.Transaction t = netsServer.getBank().getTransaction(response.getOde());
        assertNotNull(t);
        assertTrue(t.isAuthorized());
        assertEquals(money.getAmountMinorInt(), t.getAmount());
    }
    
    @Test
    public void testNetworkError() throws Exception {
        System.out.println("authorizeNetworkError");
        
        nets.setMaxRequestAttempts(1);
        
        Card card = new Card(CARDNO_VALID_VISA_1, 12, 12, "123", encryptor);
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
        
        Card card = new Card(CARDNO_VALID_VISA_1, 12, 12, "123", encryptor);
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
        Card card = new Card(CARDNO_VALID_VISA_1, 12, 12, "123", encryptor);
        Money money = Money.of(CurrencyUnit.USD, 12.2);
        String orderId = "orderid";
        String terminalId = "test";
        String approvalCode = "app";
        
        //Need to authorize first
        NetsResponse response = nets.authorize(merchant, card, money, orderId);
        
        //Now cancel
        response = nets.reverse(merchant, money, orderId, card, response.getActionCode(), response.getOde(), response.getProcessingCode(), response.getApprovalCode());
        
        assertEquals(ActionCode.Approved , response.getActionCode());
        assertNotNull(response.getOde());
        
        Bank.Transaction t = netsServer.getBank().getTransaction(response.getOde());
        assertNotNull(t);
        assertTrue(t.isCancelled());
    }
    
    @Test
    public void testCapture() throws Exception {
        System.out.println("capture");
        Card card = new Card(CARDNO_VALID_VISA_1, 12, 12, "123", encryptor);
        Money money = Money.of(CurrencyUnit.USD, 12.2);
        String orderId = "orderid";
        //Need to authorize first
        NetsResponse response = nets.authorize(merchant, card, money, orderId);
        
        //Now capture
        response = nets.capture(merchant, money, orderId, card, response.getActionCode(), response.getOde(), response.getProcessingCode(), response.getApprovalCode());
        
        assertEquals(ActionCode.Approved , response.getActionCode());
        assertNotNull(response.getOde());
                
        Bank.Transaction t = netsServer.getBank().getTransaction(response.getOde());
        assertNotNull(t);
        assertTrue(t.isCaptured());
    }
    
    @Test
    public void testRefund() throws Exception {
        System.out.println("refund");
        Card card = new Card(CARDNO_VALID_VISA_1, 12, 12, "123", encryptor);
        Money money = Money.of(CurrencyUnit.USD, 12.2);
        String orderId = "orderid";
        nets.setMinWaitBetweenAttempts(10000);
        //Need to authorize first
        NetsResponse response = nets.authorize(merchant, card, money, orderId);
        String approvalCode = response.getApprovalCode();
        
        //Now capture
        response = nets.capture(merchant, money, orderId, card, response.getActionCode(), response.getOde(), response.getProcessingCode(), approvalCode);
        
        //Now refund
        response = nets.credit(merchant, money, orderId, card, response.getActionCode(), response.getOde(), response.getProcessingCode(), approvalCode);
        
        assertEquals(ActionCode.Approved , response.getActionCode());
        assertNotNull(response.getOde());
        
        Bank.Transaction t = netsServer.getBank().getTransaction(response.getOde());
        assertNotNull(t);
        assertTrue(t.isRefunded());
    }
    
    @Test
    public void testPerformance() throws Exception {
        System.out.println("performance");
        Card card = new Card(CARDNO_VALID_VISA_2, 12, 12, "123", encryptor);
        Money money = Money.of(CurrencyUnit.USD, 12.2);
        String orderId = "orderid";

        //Bootstrap
        nets.authorize(merchant, card, money, orderId);

        long start = System.currentTimeMillis();
        for(int i=0;i<100;i++) {
            String currentId = orderId + "_" + i;
            //Need to authorize first
            NetsResponse response = nets.authorize(merchant, card, money, currentId);

            //Now capture
            response = nets.capture(merchant, money, currentId, card, response.getActionCode(), response.getOde(), response.getProcessingCode(), response.getApprovalCode());
        }
        long end = System.currentTimeMillis();
        long time = end-start;
        
        System.out.println("Done in " + time + "ms.");
        assertTrue("Did not complete within 10000ms (it took " + time + "ms)", time<10000);
    }
}
