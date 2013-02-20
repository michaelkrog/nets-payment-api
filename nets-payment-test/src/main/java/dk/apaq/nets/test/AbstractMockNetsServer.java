package dk.apaq.nets.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.MessageFactory;
import dk.apaq.nets.payment.MessageTypes;
import dk.apaq.nets.payment.PGTMHeader;
import dk.apaq.nets.payment.PsipHeader;
import org.apache.commons.io.IOUtils;

import dk.apaq.nets.payment.NetsMessageFactoryCreator;
import org.jasypt.encryption.StringEncryptor;

/**
 *
 * @author krog
 */
public abstract class AbstractMockNetsServer {

    private Bank bank = new Bank();
    private MessageFactory messageFactory = new MessageFactory();
    private boolean nextRequestFails = false;
    private final StringEncryptor encryptor;

    public AbstractMockNetsServer(StringEncryptor encryptor) {
        this.encryptor = encryptor;
        this.messageFactory = NetsMessageFactoryCreator.createFactory();
    }

    /**
     * Retrieves the bank used by the server.
     *
     * @return The bank.
     */
    public Bank getBank() {
        return bank;
    }

    /**
     * Retrieves the message factory used by the server.
     *
     * @return The message factory.
     */
    public MessageFactory getMessageFactory() {
        return messageFactory;
    }

    /**
     * Starte this server.
     *
     * @param port The port the server must listen on.
     * @throws IOException Thrown on data transfer related errors.
     */
    public abstract void start() throws IOException;

    /**
     * Stops the server.
     */
    public abstract void stop();

    protected byte[] bytesFromInputStream(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.copy(in, out);
        return out.toByteArray();
    }

    protected void bytesToOutputStream(byte[] data, OutputStream out) throws IOException {
        IOUtils.copy(new ByteArrayInputStream(data), out);
        out.flush();
    }

    protected byte[] readData(InputStream in, int numberOfBytes) throws IOException {
        if (numberOfBytes <= 0) {
            return new byte[0];
        }

        byte[] buf = new byte[numberOfBytes];
        int offset = 0;

        while (offset < numberOfBytes) {
            offset += in.read(buf, offset, buf.length - offset);
        }
        return buf;
    }

    protected PGTMHeader readHeader(InputStream in) throws IOException {
        return PGTMHeader.fromByteArray(readData(in, PGTMHeader.HEADER_LENGTH));
    }

    protected byte[] doHandle(PGTMHeader header, byte[] messageData) throws IOException, ParseException {
        //byte[] headerData = Arrays.copyOfRange(messageBuffer, 0, 32);
        //byte[] messageData = Arrays.copyOfRange(messageBuffer, 32, messageBuffer.length);

        if (!"0000".equals(header.getNetworkResponseCode())) {
            throw new IOException("Network code in request is invalid.");
        }

        if (nextRequestFails) {
            nextRequestFails = false;
            throw new IOException("Failed by demand");
        }


        IsoMessage message = messageFactory.parseMessage(messageData, PsipHeader.VALID_HEADER_LENGTH);
        if (message == null) {
            throw new NullPointerException("Message not recognized");
        }

        PsipHeader psipHeader = PsipHeader.fromString(message.getIsoHeader());
        if (!psipHeader.isValid() && psipHeader.getErrorCode() != PsipHeader.ErrorCode.OK) {
            throw new IOException("Psipheader not valid");
        }

        int type = message.getType();
        MessageHandler messageHandler = null;
        IsoMessage response;
        switch (type) {
            case MessageTypes.AUTHORIZATION_REQUEST:
                messageHandler = new AuthorizeMessageHandler(encryptor);
                break;
            case MessageTypes.CAPTURE_REQUEST:
                messageHandler = new CaptureMessageHandler(encryptor);
                break;
            case MessageTypes.REVERSAL_ADVICE_REQUEST:
                messageHandler = new ReversalMessageHandler(encryptor);
                break;
            default:
                throw new IOException("Message type not recognized.");
        }

        response = messageHandler.handleMessage(bank, message);

        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        response.write(buf, 0);

        byte[] packet = buf.toByteArray();
        header = new PGTMHeader(packet.length, "0000");

        buf.reset();
        buf.write(header.toByteArray());
        buf.write(packet);

        return buf.toByteArray();
    }

    public void setNextRequestFails(boolean nextRequestFails) {
        this.nextRequestFails = nextRequestFails;
    }
}
