package dk.apaq.nets.payment.io;

/**
 * Abstract class for Channel Factories.
 * @author krog
 */
public abstract class AbstractChannelFactory implements ChannelFactory {
    
    private ChannelLogger channelLogger;

    /**
     * Constructs a new channel factory.
     * @param channelLogger The channel logger to use for this factory.
     */
    public AbstractChannelFactory(ChannelLogger channelLogger) {
        this.channelLogger = channelLogger;
    }

    /**
     * Sets the channel logger.
     * @param channelLogger 
     */
    public void setChannelLogger(ChannelLogger channelLogger) {
        this.channelLogger = channelLogger;
    }

    /**
     * Retrieves the channel logger.
     * @return The channel logger.
     */
    public ChannelLogger getChannelLogger() {
        return channelLogger;
    }
    
}
