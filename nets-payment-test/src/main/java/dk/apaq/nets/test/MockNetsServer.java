package dk.apaq.nets.test;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoValue;
import com.solab.iso8583.MessageFactory;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import dk.apaq.nets.payment.Card;
import dk.apaq.nets.payment.MessageTypes;
import dk.apaq.nets.payment.PGTMHeader;
import dk.apaq.nets.payment.PsipHeader;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
            response.write(he.getResponseBody(), 0);
        } catch (Exception ex) {
            throw new IOException("Unable to parse message.", ex);
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
