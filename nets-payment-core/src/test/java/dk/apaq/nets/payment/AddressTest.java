
package dk.apaq.nets.payment;

import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author krog
 */
public class AddressTest {
    
    

    @Test
    public void testBeanPattern() {
        Address address = new Address("Danmarksgade 2", "9000", "Aalborg", "dk");
        assertEquals("Danmarksgade 2", address.getStreet());
        assertEquals("9000", address.getPostalCode());
        assertEquals("Aalborg", address.getCity());
        assertEquals("dk", address.getCountryCode());
    }

    
}
