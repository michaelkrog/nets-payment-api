package dk.apaq.nets.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.MessageFactory;
import com.solab.iso8583.parse.AlphaParseInfo;
import com.solab.iso8583.parse.FieldParseInfo;
import com.solab.iso8583.parse.LllvarParseInfo;
import com.solab.iso8583.parse.LlvarParseInfo;
import com.solab.iso8583.parse.NumericParseInfo;
import dk.apaq.nets.payment.MessageTypes;
import dk.apaq.nets.payment.PGTMHeader;
import dk.apaq.nets.payment.PsipHeader;
import org.apache.commons.io.IOUtils;

import static dk.apaq.nets.payment.MessageFields.*;
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
        Map<Integer, FieldParseInfo> authReqFields = new HashMap<Integer, FieldParseInfo>();
        authReqFields.put(PRIMARY_ACCOUNT_NUMBER, new LlvarParseInfo());
        authReqFields.put(PROCESSING_CODE, new NumericParseInfo(PROCESSING_CODE_LENGTH));
        authReqFields.put(AMOUNT, new NumericParseInfo(AMOUNT_LENGTH));
        authReqFields.put(LOCAL_TIME, new NumericParseInfo(LOCAL_TIME_LENGTH));
        authReqFields.put(EXPIRATION, new NumericParseInfo(EXPIRATION_LENGTH));
        authReqFields.put(POINT_OF_SERVICE, new AlphaParseInfo(POINT_OF_SERVICE_LENGTH));
        authReqFields.put(FUNCTION_CODE, new NumericParseInfo(FUNCTION_CODE_LENGTH));
        authReqFields.put(MESSAGE_REASON_CODE, new NumericParseInfo(MESSAGE_REASON_CODE_LENGTH));
        authReqFields.put(CARD_ACCEPTOR_BUSINESS_CODE, new NumericParseInfo(CARD_ACCEPTOR_BUSINESS_CODE_LENGTH));
        authReqFields.put(ACQUIRER_REFERENCE, new LlvarParseInfo());
        authReqFields.put(CARD_ACCEPTOR_TERMINAL_ID, new AlphaParseInfo(CARD_ACCEPTOR_TERMINAL_ID_LENGTH));
        authReqFields.put(CARD_ACCEPTOR_IDENTIFICATION_CODE, new AlphaParseInfo(CARD_ACCEPTOR_IDENTIFICATION_CODE_LENGTH));
        authReqFields.put(CARD_ACCEPTOR_NAME_LOCATION, new LlvarParseInfo());
        authReqFields.put(ADDITIONAL_DATA_NATIONAL, new LllvarParseInfo());
        authReqFields.put(CURRENCY_CODE, new AlphaParseInfo(CURRENCY_CODE_LENGTH));
        authReqFields.put(AUTH_ODE, new LllvarParseInfo());

        messageFactory.setParseMap(MessageTypes.AUTHORIZATION_REQUEST, authReqFields);

        Map<Integer, FieldParseInfo> reverseReqFields = new HashMap<Integer, FieldParseInfo>();
        reverseReqFields.put(PRIMARY_ACCOUNT_NUMBER, new LlvarParseInfo());
        reverseReqFields.put(PROCESSING_CODE, new NumericParseInfo(PROCESSING_CODE_LENGTH));
        reverseReqFields.put(AMOUNT, new NumericParseInfo(AMOUNT_LENGTH));
        reverseReqFields.put(LOCAL_TIME, new NumericParseInfo(LOCAL_TIME_LENGTH));
        reverseReqFields.put(FUNCTION_CODE, new NumericParseInfo(FUNCTION_CODE_LENGTH));
        reverseReqFields.put(MESSAGE_REASON_CODE, new NumericParseInfo(MESSAGE_REASON_CODE_LENGTH));
        reverseReqFields.put(CARD_ACCEPTOR_BUSINESS_CODE, new NumericParseInfo(CARD_ACCEPTOR_BUSINESS_CODE_LENGTH));
        reverseReqFields.put(ACQUIRER_REFERENCE, new LlvarParseInfo());
        reverseReqFields.put(APPROVAL_CODE, new AlphaParseInfo(APPROVAL_CODE_LENGTH));
        reverseReqFields.put(CARD_ACCEPTOR_TERMINAL_ID, new AlphaParseInfo(CARD_ACCEPTOR_TERMINAL_ID_LENGTH));
        reverseReqFields.put(CARD_ACCEPTOR_IDENTIFICATION_CODE, new AlphaParseInfo(CARD_ACCEPTOR_IDENTIFICATION_CODE_LENGTH));
        reverseReqFields.put(CARD_ACCEPTOR_NAME_LOCATION, new LlvarParseInfo());
        reverseReqFields.put(CURRENCY_CODE, new AlphaParseInfo(CURRENCY_CODE_LENGTH));
        reverseReqFields.put(AUTH_ODE, new LllvarParseInfo());

        messageFactory.setParseMap(MessageTypes.REVERSAL_ADVICE_REQUEST, reverseReqFields);

        Map<Integer, FieldParseInfo> captureReqFields = new HashMap<Integer, FieldParseInfo>();
        captureReqFields.put(PRIMARY_ACCOUNT_NUMBER, new LlvarParseInfo());
        captureReqFields.put(PROCESSING_CODE, new NumericParseInfo(PROCESSING_CODE_LENGTH));
        captureReqFields.put(AMOUNT, new NumericParseInfo(AMOUNT_LENGTH));
        captureReqFields.put(LOCAL_TIME, new NumericParseInfo(LOCAL_TIME_LENGTH));
        captureReqFields.put(EXPIRATION, new NumericParseInfo(EXPIRATION_LENGTH));
        captureReqFields.put(POINT_OF_SERVICE, new AlphaParseInfo(POINT_OF_SERVICE_LENGTH));
        captureReqFields.put(FUNCTION_CODE, new NumericParseInfo(FUNCTION_CODE_LENGTH));
        captureReqFields.put(CARD_ACCEPTOR_BUSINESS_CODE, new NumericParseInfo(CARD_ACCEPTOR_BUSINESS_CODE_LENGTH));
        captureReqFields.put(ACQUIRER_REFERENCE, new LlvarParseInfo());
        captureReqFields.put(APPROVAL_CODE, new AlphaParseInfo(APPROVAL_CODE_LENGTH));
        captureReqFields.put(ACTION_CODE, new NumericParseInfo(ACTION_CODE_LENGTH));
        captureReqFields.put(CARD_ACCEPTOR_TERMINAL_ID, new AlphaParseInfo(CARD_ACCEPTOR_TERMINAL_ID_LENGTH));
        captureReqFields.put(CARD_ACCEPTOR_IDENTIFICATION_CODE, new AlphaParseInfo(CARD_ACCEPTOR_IDENTIFICATION_CODE_LENGTH));
        captureReqFields.put(CARD_ACCEPTOR_NAME_LOCATION, new LlvarParseInfo());
        captureReqFields.put(ADDITIONAL_DATA_NATIONAL, new LllvarParseInfo());
        captureReqFields.put(CURRENCY_CODE, new AlphaParseInfo(CURRENCY_CODE_LENGTH));
        captureReqFields.put(AUTH_ODE, new LllvarParseInfo());

        messageFactory.setParseMap(MessageTypes.CAPTURE_REQUEST, captureReqFields);
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
    public abstract void start(int port) throws IOException;

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
