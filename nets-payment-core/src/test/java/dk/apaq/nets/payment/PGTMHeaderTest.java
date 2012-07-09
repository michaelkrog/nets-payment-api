package dk.apaq.nets.payment;

import junit.framework.TestCase;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author krog
 */
public class PGTMHeaderTest {
    
    private String header = "00D8000052480002200000000000003200000000000000000000000000000000";

    /**
     * Test of toByteArray method, of class PGTMHeader.
     */
    @Test
    public void testToByteArray() throws DecoderException {
        System.out.println("toByteArray");
        
        PGTMHeader instance = new PGTMHeader((short)184, "0000524800022000000000000032000000000000000000000000", "0000");
        byte[] expResult = Hex.decodeHex(header.toCharArray());
        byte[] result = instance.toByteArray();
        assertArrayEquals(expResult, result);
        
    }

    /**
     * Test of fromByteArray method, of class PGTMHeader.
     */
    @Test
    public void testFromByteArray() throws DecoderException {
        System.out.println("fromByteArray");
        byte[] data = Hex.decodeHex(header.toCharArray());
        
        PGTMHeader expResult = new PGTMHeader((short)184, "0000524800022000000000000032000000000000000000000000", "0000");
        PGTMHeader result = PGTMHeader.fromByteArray(data);
        assertEquals(expResult, result);
    }
}
