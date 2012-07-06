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
public class SslSocketChannelFactory implements ChannelFactory {
    
    private static final MessageFactory messageFactory = new MessageFactory();
    private String host;
    private int port;
    private SocketFactory socketFactory = SSLSocketFactory.getDefault();

    public SslSocketChannelFactory(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public Channel createChannel() throws IOException {
        Socket socket = socketFactory.createSocket(host, port);
        return new SslSocketChannel(socket);
    }

    public MessageFactory getMessageFactory() {
        return messageFactory;
    }
    
    
    
    
}
