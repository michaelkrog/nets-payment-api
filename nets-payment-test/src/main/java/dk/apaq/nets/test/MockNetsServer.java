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
import dk.apaq.nets.payment.Card;
import dk.apaq.nets.payment.MessageTypes;
import dk.apaq.nets.payment.PGTMHeader;
import dk.apaq.nets.payment.PsipHeader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author krog
 */
public class MockNetsServer implements HttpHandler {
    
    private Bank bank = new Bank();
    private HttpServer httpServer = null;
    private MessageFactory messageFactory = new MessageFactory();

    public MockNetsServer() {
        Map<Integer, FieldParseInfo> authReqFields = new HashMap<Integer, FieldParseInfo>();
        authReqFields.put(2, new LlvarParseInfo());
        authReqFields.put(3, new NumericParseInfo(6));
        authReqFields.put(4, new NumericParseInfo(12));
        authReqFields.put(12, new NumericParseInfo(12));
        authReqFields.put(14, new NumericParseInfo(4));
        authReqFields.put(22, new AlphaParseInfo(12));
        authReqFields.put(24, new NumericParseInfo(3));
        authReqFields.put(25, new NumericParseInfo(4));
        authReqFields.put(26, new NumericParseInfo(4));
        authReqFields.put(31, new LlvarParseInfo());
        authReqFields.put(41, new AlphaParseInfo(8));
        authReqFields.put(42, new AlphaParseInfo(15));
        authReqFields.put(43, new LlvarParseInfo());
        authReqFields.put(47, new LllvarParseInfo());
        authReqFields.put(49, new AlphaParseInfo(3));
        authReqFields.put(56, new LllbinParseInfo());
        authReqFields.put(57, new NumericParseInfo(3));
        
        messageFactory.setParseMap(MessageTypes.AUTHORIZATION_REQUEST, authReqFields);
    }
    
    
    
    public void handle(HttpExchange he) throws IOException {
        byte[] messageBuffer = readMessageBufffer(he.getRequestBody());
        byte[] headerData = Arrays.copyOfRange(messageBuffer, 0, 32);
        byte[] messageData = Arrays.copyOfRange(messageBuffer, 32, messageBuffer.length);
        
        PGTMHeader header = PGTMHeader.fromByteArray(headerData);
        
        try {
            IsoMessage message = messageFactory.parseMessage(messageData, 10);
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
            }
            
            response = messageHandler.handleMessage(bank, message);
            
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            response.write(buf, 0);
            
            he.sendResponseHeaders(200, buf.size());
            IOUtils.copy(new ByteArrayInputStream(buf.toByteArray()), he.getResponseBody());
            he.getResponseBody().flush();
        } catch (Exception ex) {
            throw new IOException("Unable to handle message.", ex);
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
        httpServer = HttpServer.create(adr, 1);
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

}
