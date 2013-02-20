package dk.apaq.nets.test;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import dk.apaq.nets.payment.PGTMHeader;
import org.jasypt.encryption.StringEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author krog
 */
public class MockNetsHttpServer extends AbstractMockNetsServer implements HttpHandler {
    
    private static final Logger LOG = LoggerFactory.getLogger(MockNetsHttpServer.class);
    private HttpServer httpServer = null;
    private final int port;

    public MockNetsHttpServer(StringEncryptor encryptor, int port) {
        super(encryptor);
        this.port = port;
    }
    
    /**
     * @{@inheritDoc} 
     */
    @Override
    public void handle(HttpExchange he) throws IOException {
        byte[] messageBuffer = bytesFromInputStream(he.getRequestBody());
        
        try {
            PGTMHeader header = readHeader(he.getRequestBody());
            byte[] packet = doHandle(header, messageBuffer);
            he.sendResponseHeaders(200, packet.length);
            
            bytesToOutputStream(packet, he.getResponseBody());
        } catch (Exception ex) {
            LOG.error("Error handling http request.", ex);
            PGTMHeader failHeader = new PGTMHeader(0, "0001");
            he.sendResponseHeaders(200, failHeader.getLength());
            bytesToOutputStream(failHeader.toByteArray(), he.getResponseBody());
        } finally {
            he.getResponseBody().close();
        }
    }


    /**
     * @{@inheritDoc} 
     */
    @Override
    public void start() throws UnknownHostException, IOException {
        InetSocketAddress adr = new InetSocketAddress(Inet4Address.getLocalHost(), port);
        httpServer = HttpServer.create(adr, 0);
        httpServer.createContext("/", this);
        httpServer.start();
    }
    
    /**
     * @{@inheritDoc} 
     */
    @Override
    public void stop() {
        if(httpServer!=null) {
            httpServer.stop(0);
        }
    }
}
