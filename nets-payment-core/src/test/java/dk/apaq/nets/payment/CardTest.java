package dk.apaq.nets.payment;

import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author krog
 */
public class CardTest {
    
   
@Test
    public void testBeanPattern() {
        Card card = new Card("Michael Krog", "4571", 12, 11, "124");
        assertEquals("Michael Krog", card.getCardHolder());
        assertEquals("4571", card.getCardNumber());
        assertEquals(12, card.getExpireYear());
        assertEquals(11, card.getExpireMonth());
        assertEquals("124", card.getCvd());
        
        Card card2 = new Card("Michael Krog", "4571", 12, 11, "124");
        Card card3 = new Card("John Doe", "4571", 12, 11, "124");
        
        assertEquals(card, card2);
        assertEquals(card.hashCode(), card2.hashCode());
        assertFalse(card.equals(card3));
        assertFalse(card2.hashCode() == card3.hashCode());
    }

    
}
