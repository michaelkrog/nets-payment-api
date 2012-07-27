package dk.apaq.nets.payment.io;

/**
 *
 * @author krog
 */
public interface ChannelLogger {

    void onMessageSent(byte[] data);
    void onMessageRecieved(byte[] data);
}
