package dk.apaq.nets.payment.io;

import java.io.IOException;

import com.solab.iso8583.IsoMessage;

/**
 *
 * @author krog
 */
public interface Channel {
    /**
     * Sends a message though the channel and gets a message back in response.
     * @param message The message to send.
     * @return The message retrieved in response.
     * @throws IOException Thrown if any erro occurs while transporting data through the channel.
     */
    IsoMessage sendMessage(IsoMessage message) throws IOException;
    
}
