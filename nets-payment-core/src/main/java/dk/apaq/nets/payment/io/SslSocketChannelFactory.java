package dk.apaq.nets.payment.io;

import com.solab.iso8583.MessageFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

/**
 *
 * @author michael
 */
public class SslSocketChannelFactory extends AbstractChannelFactory {
    
    private static final MessageFactory messageFactory = new MessageFactory();
    private static final int DEFAULT_TIMEOUT = 30000;
    
    private String host;
    private int port;
    private int channelTimeout;
    private SocketFactory socketFactory = SSLSocketFactory.getDefault();

    public SslSocketChannelFactory(String host, int port) {
        this(host, port, null);
    }
    
    public SslSocketChannelFactory(String host, int port, ChannelLogger channelLogger) {
        this(host, port, channelLogger, DEFAULT_TIMEOUT);
    }
    
    public SslSocketChannelFactory(String host, int port, ChannelLogger channelLogger, int timeout) {
        super(channelLogger);
        this.host = host;
        this.port = port;
        this.channelTimeout = timeout > 0 ? timeout : DEFAULT_TIMEOUT;
    }
    
    
    

    public Channel createChannel() throws IOException {
        Socket socket = socketFactory.createSocket();
        SocketAddress address = new InetSocketAddress(host, port);
        socket.connect(address, channelTimeout);
        socket.setSoTimeout(channelTimeout);
        return new SslSocketChannel(messageFactory, socket);
    }

    public MessageFactory getMessageFactory() {
        return messageFactory;
    }

    public void setChannelTimeout(int channelTimeout) {
        this.channelTimeout = channelTimeout;
    }

    public int getChannelTimeout() {
        return channelTimeout;
    }
    
}
