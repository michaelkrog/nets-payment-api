package dk.apaq.nets.payment.io;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.MessageFactory;
import dk.apaq.nets.payment.PGTMHeader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;

/**
 *
 * @author michael
 */
public abstract class AbstractChannel implements Channel {

    private final MessageFactory messageFactory;

    public AbstractChannel(MessageFactory messageFactory) {
        this.messageFactory = messageFactory;
    }
    
    
    protected IsoMessage byteArrayToMessage(byte[] data) throws IOException {
        try {
            return messageFactory.parseMessage(data, 10);
        } catch (ParseException ex) {
            throw new IOException("Unable to parse response.", ex);
        } catch (UnsupportedEncodingException ex) {
            throw new IOException("Unable to parse response because of encoding issues.", ex);
        }
    }

    protected byte[] messageToByteArray(IsoMessage message) throws IOException {
        byte[] packet = message.writeToBuffer(0).array();
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        //Append special Nets header
        PGTMHeader pgtmh = new PGTMHeader((short) (packet.length + 32), "0000524800022000000000000032000000000000000000000000", "0000");
        buf.write(pgtmh.toByteArray());
        buf.write(packet);
        return buf.toByteArray();
    }
    
}
