package dk.apaq.nets.test;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoValue;
import com.solab.iso8583.MessageFactory;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import dk.apaq.nets.payment.Card;
import dk.apaq.nets.payment.MessageTypes;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
    
    private List<CardEntry> cards = new ArrayList<CardEntry>();
    private HttpServer httpServer = null;
    private MessageFactory messageFactory = new MessageFactory();
    
    private class CardEntry {
        private final Card card;
        private int amount;

        public CardEntry(Card card, int amount) {
            this.card = card;
            this.amount = amount;
        }

        public Card getCard() {
            return card;
        }

        public int getAmount() {
            return amount;
        }
    }
    
    private class PGTMHeader {
        int length;
        String identity;
        String networkResponseCode;

        public PGTMHeader(int length, String identity, String networkResponseCode) {
            this.length = length;
            this.identity = identity;
            this.networkResponseCode = networkResponseCode;
        }
        
        
    }
    
    public void handle(HttpExchange he) throws IOException {
        byte[] messageBuffer = readMessageBufffer(he.getRequestBody());
        byte[] headerData = Arrays.copyOfRange(messageBuffer, 0, 32);
        byte[] messageData = Arrays.copyOfRange(messageBuffer, 32, messageBuffer.length);
        
        PGTMHeader header = parseHeader(headerData);
        
        try {
            IsoMessage message = messageFactory.parseMessage(messageData, 10);
            int type = message.getType();
            IsoMessage response;
            switch(type) {
                case MessageTypes.AUTHORIZATION_REQUEST:
                    response = handleAuthorization(message);
                    break;
                case MessageTypes.CAPTURE_REQUEST:
                    response = handleCapture(message);
                    break;
                case MessageTypes.REVERSAL_ADVICE_REQUEST:
                    response = handleCapture(message);
                    break;
            }
        } catch (Exception ex) {
            throw new IOException("Unable to parse message.", ex);
        }
    }
    
    private byte[] readMessageBufffer(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.copy(in, out);
        return out.toByteArray();
    }
    
    private PGTMHeader parseHeader(byte[] data) {
        byte[] lengthData = Arrays.copyOfRange(data, 0, 2);
        byte[] identityData = Arrays.copyOfRange(data, 2, 28);
        byte[] networkData = Arrays.copyOfRange(data, 28, 30);
        byte[] fixedData = Arrays.copyOfRange(data, 30, 32);
        
        int length = lengthData[0]*256 + lengthData[1];
        String identity = new String(Hex.encodeHex(identityData));
        String networkResponseCode = new String(Hex.encodeHex(networkData));
        
        return new PGTMHeader(length, identity, networkResponseCode);
    }
    
    private IsoMessage handleAuthorization(IsoMessage message) {
        return null;
    }
    
    private IsoMessage handleCapture(IsoMessage message) {
        return null;
    }
    
    private IsoMessage handleReversal(IsoMessage message) {
        return null;
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
    
    public void addCard(Card card, int amount) {
        cards.add(new CardEntry(card, amount));
    }
}
