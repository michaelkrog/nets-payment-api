package dk.apaq.nets.payment.io;

/**
 *
 * @author krog
 */
public abstract class AbstractChannelFactory implements ChannelFactory {
    
    protected ChannelLogger channelLogger;

    public AbstractChannelFactory(ChannelLogger channelLogger) {
        this.channelLogger = channelLogger;
    }

    public void setChannelLogger(ChannelLogger channelLogger) {
        this.channelLogger = channelLogger;
    }

    public ChannelLogger getChannelLogger() {
        return channelLogger;
    }
    
}
