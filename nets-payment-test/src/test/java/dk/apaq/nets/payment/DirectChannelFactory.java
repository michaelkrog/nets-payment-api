
package dk.apaq.nets.payment;

import java.io.IOException;

import com.solab.iso8583.MessageFactory;
import dk.apaq.nets.payment.io.AbstractChannelFactory;
import dk.apaq.nets.payment.io.Channel;
import dk.apaq.nets.payment.io.ChannelLogger;

/**
 * Javadoc
 */
public class DirectChannelFactory extends AbstractChannelFactory {

    private MessageFactory messageFactory = new MessageFactory();
    private final MockNetsDirectServer directServer;

    public DirectChannelFactory(MockNetsDirectServer directServer, ChannelLogger channelLogger) {
        super(channelLogger);
        this.directServer = directServer;
    }
    
    @Override
    public Channel createChannel() throws IOException {
        return new DirectChannel(directServer, messageFactory);
    }

    @Override
    public MessageFactory getMessageFactory() {
        return messageFactory;
    }

    @Override
    public void setMessageFactory(MessageFactory messageFactory) {
        this.messageFactory = messageFactory;
    }

}
