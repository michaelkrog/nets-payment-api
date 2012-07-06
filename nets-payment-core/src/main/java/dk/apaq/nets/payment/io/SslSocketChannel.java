package dk.apaq.nets.payment.io;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.MessageFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author michael
 */
public class SslSocketChannel extends AbstractChannel {

    private Socket socket;

    public SslSocketChannel(MessageFactory messageFactory, Socket socket) {
        super(messageFactory);
        this.socket = socket;
    }
    
    public IsoMessage sendMessage(IsoMessage message) throws IOException {
        socket.getOutputStream().write(messageToByteArray(message));
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.copy(socket.getInputStream(), out);
        IsoMessage m = byteArrayToMessage(out.toByteArray());
        socket.close();
        return m;
    }
    
}
