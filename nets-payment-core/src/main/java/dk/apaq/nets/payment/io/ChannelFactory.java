package dk.apaq.nets.payment.io;

import com.solab.iso8583.MessageFactory;

/**
 *
 * @author krog
 */
public interface ChannelFactory {

    Channel createChannel();
    MessageFactory getMessageFactory();
}
