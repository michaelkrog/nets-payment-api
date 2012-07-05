package dk.apaq.nets.payment.io;

import com.solab.iso8583.IsoMessage;
import java.io.IOException;

/**
 *
 * @author krog
 */
public interface Channel {
    
    IsoMessage sendMessage(IsoMessage message) throws IOException;
    
}
