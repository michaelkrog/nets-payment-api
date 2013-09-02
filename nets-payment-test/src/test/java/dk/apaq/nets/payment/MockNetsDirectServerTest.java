
package dk.apaq.nets.payment;

import dk.apaq.framework.common.beans.finance.Card;
import dk.apaq.nets.payment.ActionCode;
import dk.apaq.nets.payment.Address;
import dk.apaq.nets.payment.AuthorizeRequest;
import dk.apaq.nets.payment.Merchant;
import dk.apaq.nets.payment.NetsResponse;
import dk.apaq.nets.test.Bank;
import dk.apaq.nets.test.Slf4jChannelLogger;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 * Javadoc
 */
public class MockNetsDirectServerTest {

    private static final String CARDNO_VALID_VISA_1 = "4571123412341234";
    private StringEncryptor encryptor = new StandardPBEStringEncryptor();
    private Merchant merchant = new Merchant("123", "Smith Radio", new Address("Boulevard 4", "3266", "Broby", "DNK"));
    private MockNetsDirectServer directServer = new MockNetsDirectServer(encryptor);
    private Slf4jChannelLogger channelLogger = new Slf4jChannelLogger();
    private DirectChannelFactory channelFactory = new DirectChannelFactory(directServer, channelLogger);
    
    @Before
    public void init() {
        ((StandardPBEStringEncryptor)encryptor).setPassword("qwerty");
        channelFactory.setMessageFactory(NetsMessageFactoryCreator.createFactory());
        directServer.getBank().addCard(new Card(CARDNO_VALID_VISA_1, 12, 12, "123", encryptor), 100000);
    }
    
    @Test
    public void testRefund() throws Exception {
        System.out.println("refund");
        Card card = new Card(CARDNO_VALID_VISA_1, 12, 12, "123", encryptor);
        Money money = Money.of(CurrencyUnit.USD, 12.2);
        String orderId = "orderid";
        
        //Need to authorize first
        NetsResponse response = new AuthorizeRequest(merchant, card, money, orderId, channelFactory, encryptor).send();
        String approvalCode = response.getApprovalCode();
        
        //Now capture
        response = new CaptureRequest(merchant, card, money, orderId, response.getOde(), approvalCode, response.getActionCode(), 
                channelFactory, encryptor). send();
        
        //Now refund
        CaptureRequest req = new CaptureRequest(merchant, card, money, orderId, response.getOde(), approvalCode, response.getActionCode(), 
                channelFactory, encryptor);
        req.setRefund(true);
        response = req.send();
        
        assertEquals("Action code was not 'approved'", ActionCode.Approved , response.getActionCode());
        assertNotNull("Ode was null", response.getOde());
        
        Bank.Transaction t = directServer.getBank().getTransaction(response.getOde());
        assertNotNull("Transaction does not exist.", t);
        assertTrue("Transaction not marked as refunded.", t.isRefunded());
    }
}
