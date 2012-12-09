package dk.apaq.nets.payment.io;

import java.io.IOException;

import com.solab.iso8583.MessageFactory;

/**
 * Interface for a channel factory.
 */
public interface ChannelFactory {
    /**
     * Create a new channel for sending data.
     * @return The new channel.
     * @throws IOException Thrown if an error occurs while connecting the channel.
     */
    Channel createChannel() throws IOException;

    /**
     * Retrieves the message factory used by this channel factory.
     * @return 
     */
    MessageFactory getMessageFactory();
}
