package dk.apaq.nets.payment.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Arrays;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.MessageFactory;
import dk.apaq.nets.payment.PGTMHeader;
import dk.apaq.nets.payment.PsipHeader;
import org.apache.commons.lang.Validate;

/**
 * Abstract class for a Communicatin Channel.
 */
public abstract class AbstractChannel implements Channel {

    private final MessageFactory messageFactory;

    /**
     * Consctructor for a new Channel.
     *
     * @param messageFactory The messagefactory.
     */
    public AbstractChannel(MessageFactory messageFactory) {
        this.messageFactory = messageFactory;
    }

    /**
     * Parses an IsoMessage from byte array.
     *
     * @param data The byte array.
     * @return The IsoMessage.
     * @throws IOException Thrown is networkcode specified by PGTMHeader in data indicates an error.
     */
    protected IsoMessage byteArrayToMessage(byte[] data) throws IOException {

        try {
            Validate.isTrue(data.length >= PGTMHeader.HEADER_LENGTH,
                    "The data array does not contain the minimum number of bytes required(" + PGTMHeader.HEADER_LENGTH + ").");
            byte[] headerData = Arrays.copyOfRange(data, 0, PGTMHeader.HEADER_LENGTH);
            byte[] messageData = Arrays.copyOfRange(data, PGTMHeader.HEADER_LENGTH, data.length);

            PGTMHeader header = PGTMHeader.fromByteArray(headerData);
            if (!"0000".equals(header.getNetworkResponseCode())) {
                throw new IOException("Unknown networkcode returned from server. [networkcode=" + header.getNetworkResponseCode() + "]");
            }

            return messageFactory.parseMessage(messageData, PsipHeader.VALID_HEADER_LENGTH);
        } catch (ParseException ex) {
            throw new IOException("Unable to parse response.", ex);
        } catch (UnsupportedEncodingException ex) {
            throw new IOException("Unable to parse response because of encoding issues.", ex);
        } catch (IllegalArgumentException ex) {
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
