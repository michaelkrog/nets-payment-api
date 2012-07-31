package dk.apaq.nets.test;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.MessageFactory;
import com.solab.iso8583.parse.*;
import dk.apaq.nets.payment.MessageFields;
import dk.apaq.nets.payment.MessageTypes;
import dk.apaq.nets.payment.PGTMHeader;
import dk.apaq.nets.payment.PsipHeader;
import java.io.*;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author krog
 */
public abstract class AbstractMockNetsServer {
    protected Bank bank = new Bank();
    protected MessageFactory messageFactory = new MessageFactory();
    private boolean nextRequestFails = false;

    public AbstractMockNetsServer() {
        Map<Integer, FieldParseInfo> authReqFields = new HashMap<Integer, FieldParseInfo>();
        authReqFields.put(MessageFields.PRIMARY_ACCOUNT_NUMBER, new LlvarParseInfo());
        authReqFields.put(MessageFields.PROCESSING_CODE, new NumericParseInfo(6));
        authReqFields.put(MessageFields.AMOUNT, new NumericParseInfo(12));
        authReqFields.put(MessageFields.LOCAL_TIME, new NumericParseInfo(12));
        authReqFields.put(MessageFields.EXPIRATION, new NumericParseInfo(4));
        authReqFields.put(MessageFields.POINT_OF_SERVICE, new AlphaParseInfo(12));
        authReqFields.put(MessageFields.FUNCTION_CODE, new NumericParseInfo(3));
        authReqFields.put(MessageFields.MESSAGE_REASON_CODE, new NumericParseInfo(4));
        authReqFields.put(MessageFields.CARD_ACCEPTOR_BUSINESS_CODE, new NumericParseInfo(4));
        authReqFields.put(MessageFields.ACQUIRER_REFERENCE, new LlvarParseInfo());
        authReqFields.put(MessageFields.CARD_ACCEPTOR_TERMINAL_ID, new AlphaParseInfo(8));
        authReqFields.put(MessageFields.CARD_ACCEPTOR_IDENTIFICATION_CODE, new AlphaParseInfo(15));
        authReqFields.put(MessageFields.CARD_ACCEPTOR_NAME_LOCATION, new LlvarParseInfo());
        authReqFields.put(MessageFields.ADDITIONAL_DATA_NATIONAL, new LllvarParseInfo());
        authReqFields.put(MessageFields.CURRENCY_CODE, new AlphaParseInfo(3));
        authReqFields.put(MessageFields.AUTH_ODE, new LllvarParseInfo());
        
        messageFactory.setParseMap(MessageTypes.AUTHORIZATION_REQUEST, authReqFields);
        
        Map<Integer, FieldParseInfo> reverseReqFields = new HashMap<Integer, FieldParseInfo>();
        reverseReqFields.put(MessageFields.PRIMARY_ACCOUNT_NUMBER, new LlvarParseInfo());
        reverseReqFields.put(MessageFields.PROCESSING_CODE, new NumericParseInfo(6));
        reverseReqFields.put(MessageFields.AMOUNT, new NumericParseInfo(12));
        reverseReqFields.put(MessageFields.LOCAL_TIME, new NumericParseInfo(12));
        reverseReqFields.put(MessageFields.FUNCTION_CODE, new NumericParseInfo(3));
        reverseReqFields.put(MessageFields.MESSAGE_REASON_CODE, new NumericParseInfo(4));
        reverseReqFields.put(MessageFields.CARD_ACCEPTOR_BUSINESS_CODE, new NumericParseInfo(4));
        reverseReqFields.put(MessageFields.ACQUIRER_REFERENCE, new LlvarParseInfo());
        reverseReqFields.put(MessageFields.APPROVAL_CODE, new AlphaParseInfo(6));
        reverseReqFields.put(MessageFields.CARD_ACCEPTOR_TERMINAL_ID, new AlphaParseInfo(8));
        reverseReqFields.put(MessageFields.CARD_ACCEPTOR_IDENTIFICATION_CODE, new AlphaParseInfo(15));
        reverseReqFields.put(MessageFields.CARD_ACCEPTOR_NAME_LOCATION, new LlvarParseInfo());
        reverseReqFields.put(MessageFields.CURRENCY_CODE, new AlphaParseInfo(3));
        reverseReqFields.put(MessageFields.AUTH_ODE, new LllvarParseInfo());
        
        messageFactory.setParseMap(MessageTypes.REVERSAL_ADVICE_REQUEST, reverseReqFields);      
        
        Map<Integer, FieldParseInfo> captureReqFields = new HashMap<Integer, FieldParseInfo>();
        captureReqFields.put(MessageFields.PRIMARY_ACCOUNT_NUMBER, new LlvarParseInfo());
        captureReqFields.put(MessageFields.PROCESSING_CODE, new NumericParseInfo(6));
        captureReqFields.put(MessageFields.AMOUNT, new NumericParseInfo(12));
        captureReqFields.put(MessageFields.LOCAL_TIME, new NumericParseInfo(12));
        captureReqFields.put(MessageFields.EXPIRATION, new NumericParseInfo(4));
        captureReqFields.put(MessageFields.POINT_OF_SERVICE, new AlphaParseInfo(12));
        captureReqFields.put(MessageFields.FUNCTION_CODE, new NumericParseInfo(3));
        captureReqFields.put(MessageFields.CARD_ACCEPTOR_BUSINESS_CODE, new NumericParseInfo(4));
        captureReqFields.put(MessageFields.ACQUIRER_REFERENCE, new LlvarParseInfo());
        captureReqFields.put(MessageFields.APPROVAL_CODE, new AlphaParseInfo(6));
        captureReqFields.put(MessageFields.ACTION_CODE, new NumericParseInfo(3));
        captureReqFields.put(MessageFields.CARD_ACCEPTOR_TERMINAL_ID, new AlphaParseInfo(8));
        captureReqFields.put(MessageFields.CARD_ACCEPTOR_IDENTIFICATION_CODE, new AlphaParseInfo(15));
        captureReqFields.put(MessageFields.CARD_ACCEPTOR_NAME_LOCATION, new LlvarParseInfo());
        captureReqFields.put(MessageFields.ADDITIONAL_DATA_NATIONAL, new LllvarParseInfo());
        captureReqFields.put(MessageFields.CURRENCY_CODE, new AlphaParseInfo(3));
        captureReqFields.put(MessageFields.AUTH_ODE, new LllvarParseInfo());
        
        messageFactory.setParseMap(MessageTypes.CAPTURE_REQUEST, captureReqFields);  
    }

    public Bank getBank() {
        return bank;
    }

    public abstract void start(int port) throws UnknownHostException, IOException;
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
        if(numberOfBytes<=0) {
            return new byte[0];
        }
        
        byte[] buf = new byte[numberOfBytes];
        int offset = 0;
        
        while(offset < numberOfBytes) {
            offset += in.read(buf, offset, buf.length - offset);
        }
        return buf;
    }
    
    protected PGTMHeader readHeader(InputStream in) throws IOException {
        return PGTMHeader.fromByteArray(readData(in, 32));
    }
    
    protected byte[] doHandle(PGTMHeader header, byte[] messageData) throws IOException, ParseException {
        //byte[] headerData = Arrays.copyOfRange(messageBuffer, 0, 32);
        //byte[] messageData = Arrays.copyOfRange(messageBuffer, 32, messageBuffer.length);
        
            if(!"0000".equals(header.getNetworkResponseCode())) {
                throw new IOException("Network code in request is invalid.");
            }
            
            if(nextRequestFails) {
                nextRequestFails = false;
                throw new IOException("FAIL");
            }
            
            
            IsoMessage message = messageFactory.parseMessage(messageData, 10);
            if(message == null) {
                throw new NullPointerException("Message not recognized");
            }
            
            PsipHeader psipHeader = PsipHeader.fromString(message.getIsoHeader());
            if(!psipHeader.isValid() && psipHeader.getErrorCode() != PsipHeader.ErrorCode.OK) {
                throw new IOException("Psipheader not valid");
            }
            
            int type = message.getType();
            MessageHandler messageHandler = null;
            IsoMessage response;
            switch(type) {
                case MessageTypes.AUTHORIZATION_REQUEST:
                    messageHandler = new AuthorizeMessageHandler();
                    break;
                case MessageTypes.CAPTURE_REQUEST:
                    messageHandler = new CaptureMessageHandler();
                    break;
                case MessageTypes.REVERSAL_ADVICE_REQUEST:
                    messageHandler = new ReversalMessageHandler();
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
