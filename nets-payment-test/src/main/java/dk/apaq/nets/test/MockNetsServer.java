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
    
    private static final int FIELD_INDEX_PRIMARY_ACCOUNT_NUMBER = 2;
    private static final int FIELD_INDEX_PROCESSING_CODE = 3;
    private static final int FIELD_INDEX_AMOUNT = 4;
    private static final int FIELD_INDEX_LOCAL_TIME = 12;
    private static final int FIELD_INDEX_EXPIRATION = 14;
    private static final int FIELD_INDEX_POINT_OF_SERVICE = 22;
    private static final int FIELD_INDEX_FUNCTION_CODE = 24;
    private static final int FIELD_INDEX_MESSAGE_REASON_CODE = 25;
    private static final int FIELD_INDEX_CARD_ACCEPTOR_BUSINESS_CODE = 26;
    private static final int FIELD_INDEX_ACQUIRER_REFERENCE = 31;
    private static final int FIELD_INDEX_APPROVAL_CODE= 38;
    private static final int FIELD_INDEX_ACTION_CODE= 39;
    private static final int FIELD_INDEX_CARD_ACCEPTOR_TERMINAL_ID= 41;
    private static final int FIELD_INDEX_CARD_ACCEPTOR_IDENTIFICATION_CODE= 42;
    private static final int FIELD_INDEX_CARD_ACCEPTOR_NAME_LOCATION= 43;
    private static final int FIELD_INDEX_ADDITIONAL_RESOPNSE_DATA= 44;
    private static final int FIELD_INDEX_ADDITIONAL_DATA_NATIONAL= 47;
    private static final int FIELD_INDEX_CURRENCY_CODE= 49;
    private static final int FIELD_INDEX_AUTH_ODE= 56;
    private static final int FIELD_INDEX_AUTHORIZATION_LIFE_CYCLE= 57;
    
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
    
    
    
    public void handle(HttpExchange he) throws IOException {
        byte[] messageBuffer = readMessageBufffer(he.getRequestBody());
        byte[] headerData = Arrays.copyOfRange(messageBuffer, 0, 32);
        byte[] messageData = Arrays.copyOfRange(messageBuffer, 32, messageBuffer.length);
        
        PGTMHeader header = PGTMHeader.fromByteArray(headerData);
        
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
    
    
    
    private IsoMessage handleAuthorization(IsoMessage message) {
        String card = message.getField(FIELD_INDEX_PRIMARY_ACCOUNT_NUMBER).toString();
        String expire = message.getField(FIELD_INDEX_EXPIRATION).toString(); 
        
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
