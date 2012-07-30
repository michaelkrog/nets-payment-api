package dk.apaq.nets.payment.io;

import com.solab.iso8583.MessageFactory;
import java.io.IOException;
import java.net.Socket;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

/**
 *
 * @author michael
 */
public class SslSocketChannelFactory extends AbstractChannelFactory {
    
    private static final MessageFactory messageFactory = new MessageFactory();
    private String host;
    private int port;
    private SocketFactory socketFactory = SSLSocketFactory.getDefault();

    public SslSocketChannelFactory(String host, int port) {
        this(host, port, null);
    }
    
    public SslSocketChannelFactory(String host, int port, ChannelLogger channelLogger) {
        super(channelLogger);
        this.host = host;
        this.port = port;
    }

    public Channel createChannel() throws IOException {
        Socket socket = socketFactory.createSocket(host, port);
        socket.setSoTimeout(30000);
        return new SslSocketChannel(messageFactory, socket);
    }

    public MessageFactory getMessageFactory() {
        return messageFactory;
    }
    
    
    
    
}
