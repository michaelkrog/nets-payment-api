package dk.apaq.nets.payment.io;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.MessageFactory;
import dk.apaq.nets.payment.PGTMHeader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Arrays;
import org.apache.commons.io.HexDump;
import org.apache.commons.lang.Validate;

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
            Validate.isTrue(data.length>32, "The data array does not contain the minimum number of bytes required(32).");
            byte[] headerData = Arrays.copyOfRange(data, 0, 32);
            byte[] messageData = Arrays.copyOfRange(data, 32, data.length);
            
            PGTMHeader header = PGTMHeader.fromByteArray(headerData);
            if(!"0000".equals(header.getNetworkResponseCode())) {
                throw new IOException("Unknown networkcode returned from server. [networkcode="+header.getNetworkResponseCode()+"]");
            }
            
            return messageFactory.parseMessage(messageData, 10);
        } catch (ParseException ex) {
            throw new IOException("Unable to parse response.", ex);
        } catch (UnsupportedEncodingException ex) {
            throw new IOException("Unable to parse response because of encoding issues.", ex);
        } catch(IllegalArgumentException ex) {
            throw new IOException("Unable to parse response because data did not parse validation.", ex);
        }
    }

    protected byte[] messageToByteArray(IsoMessage message) throws IOException {
        byte[] packet = message.writeToBuffer(0).array();
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        //Append special Nets header
        PGTMHeader pgtmh = new PGTMHeader(packet.length, "0000");
        buf.write(pgtmh.toByteArray());
        buf.write(packet);
        return buf.toByteArray();
    }
    
}
