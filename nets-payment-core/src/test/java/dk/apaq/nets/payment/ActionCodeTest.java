package dk.apaq.nets.payment;

import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author krog
 */
public class ActionCodeTest {
    

    /**
     * Test of values method, of class ActionCode.
     */
    @Test
    public void testBeanPattern() {
        ActionCode code = ActionCode.fromCode("000");
        assertEquals(code.getCode(), "000");
        assertEquals(code.getCustomerText(), "Approved");
        assertEquals(code.getMerchantText(), "Approved");
        assertEquals(code.getMerchantAction(), MerchantAction.Approved);
    }

}
