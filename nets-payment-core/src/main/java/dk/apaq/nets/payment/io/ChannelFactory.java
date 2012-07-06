package dk.apaq.nets.payment.io;

import com.solab.iso8583.MessageFactory;
import java.io.IOException;

/**
 *
 * @author krog
 */
public interface ChannelFactory {

    Channel createChannel() throws IOException;
    MessageFactory getMessageFactory();
}
