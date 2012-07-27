package dk.apaq.nets.payment.io;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.MessageFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.HexDump;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author michael
 */
public class SslSocketChannel extends AbstractChannel {

    private static final Logger LOG = LoggerFactory.getLogger(SslSocketChannel.class);
    private Socket socket;

    public SslSocketChannel(MessageFactory messageFactory, Socket socket) {
        super(messageFactory);
        this.socket = socket;
    }
    
    public IsoMessage sendMessage(IsoMessage message) throws IOException {
        byte[] requestData = messageToByteArray(message);
        
        socket.getOutputStream().write(requestData);
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.copy(socket.getInputStream(), out);
        
        byte[] responseData = out.toByteArray();
        
        IsoMessage m = byteArrayToMessage(responseData);
        socket.close();
        return m;
    }
    
}
