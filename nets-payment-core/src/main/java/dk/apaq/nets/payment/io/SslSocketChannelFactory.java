package dk.apaq.nets.payment.io;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import com.solab.iso8583.MessageFactory;

/**
 * Channel Factory that creates SslSocketChannels.
 */
public class SslSocketChannelFactory extends AbstractChannelFactory {
    
    private static final MessageFactory MESSAGE_FACTORY = new MessageFactory();
    private static final int DEFAULT_TIMEOUT = 30000;
    
    private String host;
    private int port;
    private int channelTimeout;
    private SocketFactory socketFactory = SSLSocketFactory.getDefault();

    /**
     * Creates new instance
     * @param host The host name to connect to.
     * @param port The port to connect to.
     */
    public SslSocketChannelFactory(String host, int port) {
        this(host, port, null);
    }
    
    /**
     * Creates new instance.
     * @param host The host name to connect to.
     * @param port The port to connect to.
     * @param channelLogger The channel logger to use.
     */
    public SslSocketChannelFactory(String host, int port, ChannelLogger channelLogger) {
        this(host, port, channelLogger, DEFAULT_TIMEOUT);
    }
    
    /**
     * Creates new instance.
     * @param host The host name to connect to.
     * @param port The port to connect to.
     * @param channelLogger The channel logger to use.
     * @param timeout The timeout in milliseconds.
     */
    public SslSocketChannelFactory(String host, int port, ChannelLogger channelLogger, int timeout) {
        super(channelLogger);
        this.host = host;
        this.port = port;
        this.channelTimeout = timeout > 0 ? timeout : DEFAULT_TIMEOUT;
    }
    
    /**
     * @{@inheritDoc}
     */
    @Override
    public Channel createChannel() throws IOException {
        Socket socket = socketFactory.createSocket();
        SocketAddress address = new InetSocketAddress(host, port);
        socket.connect(address, channelTimeout);
        socket.setSoTimeout(channelTimeout);
        return new SslSocketChannel(MESSAGE_FACTORY, socket);
    }

    /**
     * @{@inheritDoc}
     */
    @Override
    public MessageFactory getMessageFactory() {
        return MESSAGE_FACTORY;
    }

    /**
     * Sets the timeout in milliseconds.
     * @param channelTimeout 
     */
    public void setChannelTimeout(int channelTimeout) {
        this.channelTimeout = channelTimeout;
    }

    /**
     * Retrieves the timout value.
     * @return The timeout value.
     */
    public int getChannelTimeout() {
        return channelTimeout;
    }
    
}
