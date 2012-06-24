package dk.apaq.nets.test;

import com.solab.iso8583.IsoMessage;
import java.io.IOException;

/**
 *
 * @author michaelzachariassenkrog
 */
public interface MessageHandler {

    IsoMessage handleMessage(Bank bank, IsoMessage message) throws IOException;
    
}
