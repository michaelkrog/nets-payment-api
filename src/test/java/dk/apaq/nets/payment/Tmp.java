package dk.apaq.nets.payment;

import java.util.Arrays;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

/**
 *
 * @author michael
 */
public class Tmp {
    public static void main(String[] args) throws DecoderException {
        char[] chars = new char[]{'0','0','0','0','5','2','4','8','0','0','0','2','2','0','0','0','0','0','0','0','0','0','0','0','0','0','3','2','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0'};
        byte[] tmp = Hex.decodeHex(chars);
        System.out.println(Arrays.toString(tmp));
    }
}
