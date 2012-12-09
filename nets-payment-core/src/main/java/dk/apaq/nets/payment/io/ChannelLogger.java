package dk.apaq.nets.payment.io;

/**
 * Interface for channel loggers. These loggers are used to log everything sent through a channel.
 */
public interface ChannelLogger {

    /**
     * Called when a message is sent.
     * @param data The byte array of the mesage.
     */
    void onMessageSent(byte[] data);
    
    /**
     * Called when a message is recieved.
     * @param data  The byte array of the message
     */
    void onMessageRecieved(byte[] data);
}
