package dk.apaq.nets.payment.io;

import com.solab.iso8583.IsoMessage;
import java.io.IOException;
import java.net.Socket;

/**
 *
 * @author michael
 */
public class SslSocketChannel implements Channel {

    private Socket socket;

    public SslSocketChannel(Socket socket) {
        this.socket = socket;
    }
    
    public IsoMessage sendMessage(IsoMessage message) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
