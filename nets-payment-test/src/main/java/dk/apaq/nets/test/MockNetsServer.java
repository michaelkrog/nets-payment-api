package dk.apaq.nets.test;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.IsoValue;
import com.solab.iso8583.MessageFactory;
import com.solab.iso8583.parse.AlphaParseInfo;
import com.solab.iso8583.parse.FieldParseInfo;
import com.solab.iso8583.parse.LllbinParseInfo;
import com.solab.iso8583.parse.LllvarParseInfo;
import com.solab.iso8583.parse.LlvarParseInfo;
import com.solab.iso8583.parse.NumericParseInfo;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import dk.apaq.nets.payment.MessageFields;
import dk.apaq.nets.payment.MessageTypes;
import dk.apaq.nets.payment.PGTMHeader;
import dk.apaq.nets.payment.PsipHeader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.*;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author krog
 */
public class MockNetsServer implements HttpHandler {
    
    private Bank bank = new Bank();
    private HttpServer httpServer = null;
    private MessageFactory messageFactory = new MessageFactory();
    private boolean nextRequestFails = false;

    public MockNetsServer() {
        Map<Integer, FieldParseInfo> authReqFields = new HashMap<Integer, FieldParseInfo>();
        authReqFields.put(MessageFields.FIELD_INDEX_PRIMARY_ACCOUNT_NUMBER, new LlvarParseInfo());
        authReqFields.put(MessageFields.FIELD_INDEX_PROCESSING_CODE, new NumericParseInfo(6));
        authReqFields.put(MessageFields.FIELD_INDEX_AMOUNT, new NumericParseInfo(12));
        authReqFields.put(MessageFields.FIELD_INDEX_LOCAL_TIME, new NumericParseInfo(12));
        authReqFields.put(MessageFields.FIELD_INDEX_EXPIRATION, new NumericParseInfo(4));
        authReqFields.put(MessageFields.FIELD_INDEX_POINT_OF_SERVICE, new AlphaParseInfo(12));
        authReqFields.put(MessageFields.FIELD_INDEX_FUNCTION_CODE, new NumericParseInfo(3));
        authReqFields.put(MessageFields.FIELD_INDEX_MESSAGE_REASON_CODE, new NumericParseInfo(4));
        authReqFields.put(MessageFields.FIELD_INDEX_CARD_ACCEPTOR_BUSINESS_CODE, new NumericParseInfo(4));
        authReqFields.put(MessageFields.FIELD_INDEX_ACQUIRER_REFERENCE, new LlvarParseInfo());
        authReqFields.put(MessageFields.FIELD_INDEX_CARD_ACCEPTOR_TERMINAL_ID, new AlphaParseInfo(8));
        authReqFields.put(MessageFields.FIELD_INDEX_CARD_ACCEPTOR_IDENTIFICATION_CODE, new AlphaParseInfo(15));
        authReqFields.put(MessageFields.FIELD_INDEX_CARD_ACCEPTOR_NAME_LOCATION, new LlvarParseInfo());
        authReqFields.put(MessageFields.FIELD_INDEX_ADDITIONAL_DATA_NATIONAL, new LllvarParseInfo());
        authReqFields.put(MessageFields.FIELD_INDEX_CURRENCY_CODE, new AlphaParseInfo(3));
        authReqFields.put(MessageFields.FIELD_INDEX_AUTH_ODE, new LllvarParseInfo());
        
        messageFactory.setParseMap(MessageTypes.AUTHORIZATION_REQUEST, authReqFields);
        
        Map<Integer, FieldParseInfo> reverseReqFields = new HashMap<Integer, FieldParseInfo>();
        reverseReqFields.put(MessageFields.FIELD_INDEX_PRIMARY_ACCOUNT_NUMBER, new LlvarParseInfo());
        reverseReqFields.put(MessageFields.FIELD_INDEX_PROCESSING_CODE, new NumericParseInfo(6));
        reverseReqFields.put(MessageFields.FIELD_INDEX_AMOUNT, new NumericParseInfo(12));
        reverseReqFields.put(MessageFields.FIELD_INDEX_LOCAL_TIME, new NumericParseInfo(12));
        reverseReqFields.put(MessageFields.FIELD_INDEX_FUNCTION_CODE, new NumericParseInfo(3));
        reverseReqFields.put(MessageFields.FIELD_INDEX_MESSAGE_REASON_CODE, new NumericParseInfo(4));
        reverseReqFields.put(MessageFields.FIELD_INDEX_CARD_ACCEPTOR_BUSINESS_CODE, new NumericParseInfo(4));
        reverseReqFields.put(MessageFields.FIELD_INDEX_ACQUIRER_REFERENCE, new LlvarParseInfo());
        reverseReqFields.put(MessageFields.FIELD_INDEX_APPROVAL_CODE, new AlphaParseInfo(6));
        reverseReqFields.put(MessageFields.FIELD_INDEX_CARD_ACCEPTOR_TERMINAL_ID, new AlphaParseInfo(8));
        reverseReqFields.put(MessageFields.FIELD_INDEX_CARD_ACCEPTOR_IDENTIFICATION_CODE, new AlphaParseInfo(15));
        reverseReqFields.put(MessageFields.FIELD_INDEX_CARD_ACCEPTOR_NAME_LOCATION, new LlvarParseInfo());
        reverseReqFields.put(MessageFields.FIELD_INDEX_CURRENCY_CODE, new AlphaParseInfo(3));
        reverseReqFields.put(MessageFields.FIELD_INDEX_AUTH_ODE, new LllvarParseInfo());
        
        messageFactory.setParseMap(MessageTypes.REVERSAL_ADVICE_REQUEST, reverseReqFields);      
        
        Map<Integer, FieldParseInfo> captureReqFields = new HashMap<Integer, FieldParseInfo>();
        captureReqFields.put(MessageFields.FIELD_INDEX_PRIMARY_ACCOUNT_NUMBER, new LlvarParseInfo());
        captureReqFields.put(MessageFields.FIELD_INDEX_PROCESSING_CODE, new NumericParseInfo(6));
        captureReqFields.put(MessageFields.FIELD_INDEX_AMOUNT, new NumericParseInfo(12));
        captureReqFields.put(MessageFields.FIELD_INDEX_LOCAL_TIME, new NumericParseInfo(12));
        captureReqFields.put(MessageFields.FIELD_INDEX_EXPIRATION, new NumericParseInfo(4));
        captureReqFields.put(MessageFields.FIELD_INDEX_POINT_OF_SERVICE, new AlphaParseInfo(12));
        captureReqFields.put(MessageFields.FIELD_INDEX_FUNCTION_CODE, new NumericParseInfo(3));
        captureReqFields.put(MessageFields.FIELD_INDEX_CARD_ACCEPTOR_BUSINESS_CODE, new NumericParseInfo(4));
        captureReqFields.put(MessageFields.FIELD_INDEX_ACQUIRER_REFERENCE, new LlvarParseInfo());
        captureReqFields.put(MessageFields.FIELD_INDEX_APPROVAL_CODE, new AlphaParseInfo(6));
        captureReqFields.put(MessageFields.FIELD_INDEX_ACTION_CODE, new NumericParseInfo(3));
        captureReqFields.put(MessageFields.FIELD_INDEX_CARD_ACCEPTOR_TERMINAL_ID, new AlphaParseInfo(8));
        captureReqFields.put(MessageFields.FIELD_INDEX_CARD_ACCEPTOR_IDENTIFICATION_CODE, new AlphaParseInfo(15));
        captureReqFields.put(MessageFields.FIELD_INDEX_CARD_ACCEPTOR_NAME_LOCATION, new LlvarParseInfo());
        captureReqFields.put(MessageFields.FIELD_INDEX_ADDITIONAL_DATA_NATIONAL, new LllvarParseInfo());
        captureReqFields.put(MessageFields.FIELD_INDEX_CURRENCY_CODE, new AlphaParseInfo(3));
        captureReqFields.put(MessageFields.FIELD_INDEX_AUTH_ODE, new LllvarParseInfo());
        
        messageFactory.setParseMap(MessageTypes.CAPTURE_REQUEST, captureReqFields);  
    }
    
    
    
    public void handle(HttpExchange he) throws IOException {
        byte[] messageBuffer = readMessageBufffer(he.getRequestBody());
        byte[] headerData = Arrays.copyOfRange(messageBuffer, 0, 32);
        byte[] messageData = Arrays.copyOfRange(messageBuffer, 32, messageBuffer.length);
        
        PGTMHeader header = PGTMHeader.fromByteArray(headerData);
        
        try {
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
                    he.sendResponseHeaders(500, -1);
                    return;
            }
            
            response = messageHandler.handleMessage(bank, message);
            
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            response.write(buf, 0);
            
            byte[] packet = buf.toByteArray();
            header = new PGTMHeader(buf.size(), "0000");
            he.sendResponseHeaders(200, header.getLength());
            
            buf.reset();
            buf.write(header.toByteArray());
            buf.write(packet);
            
            IOUtils.copy(new ByteArrayInputStream(buf.toByteArray()), he.getResponseBody());
            he.getResponseBody().flush();
        } catch (Exception ex) {
            PGTMHeader failHeader = new PGTMHeader(0, "0001");
            he.sendResponseHeaders(200, failHeader.getLength());
            IOUtils.copy(new ByteArrayInputStream(failHeader.toByteArray()), he.getResponseBody());
            he.getResponseBody().flush();
        } finally {
            he.getResponseBody().close();
        }
    }
    
    private byte[] readMessageBufffer(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.copy(in, out);
        return out.toByteArray();
    }

    public Bank getBank() {
        return bank;
    }
    
    public void start(int port) throws UnknownHostException, IOException {
        InetSocketAddress adr = new InetSocketAddress(Inet4Address.getLocalHost(), port);
        httpServer = HttpServer.create(adr, 0);
        httpServer.createContext("/", this);
        httpServer.start();
    }
    
    public void stop() {
        if(httpServer!=null) {
            httpServer.stop(0);
        }
    }
    
    public boolean isStarted() {
        return false;
    }

    public void setNextRequestFails(boolean nextRequestFails) {
        this.nextRequestFails = nextRequestFails;
    }

}
