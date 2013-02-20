
package dk.apaq.nets.payment;

import java.io.IOException;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.MessageFactory;
import dk.apaq.nets.payment.io.AbstractChannel;

/**
 * Javadoc
 */
public class DirectChannel extends AbstractChannel {

    private final MockNetsDirectServer directServer;

    public DirectChannel(MockNetsDirectServer directServer, MessageFactory messageFactory) {
        super(messageFactory);
        this.directServer = directServer;
    }

    @Override
    public IsoMessage sendMessage(IsoMessage message) throws IOException {
        try {
            return byteArrayToMessage(directServer.handleRequest(messageToByteArray(message)));
        } catch (ParseException ex) {
            throw new IOException("Unable to parse message.", ex);
        }
    }

}
