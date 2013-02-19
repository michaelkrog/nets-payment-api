package dk.apaq.nets.test;

import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author michael
 */
public class MockNetsSocketServerTest {
    
    public MockNetsSocketServerTest() {
        System.setProperty("javax.net.ssl.trustStore", "src/test/resources/keystore");

    }

    /**
     * Test of start method, of class MockNetsSocketServer.
     */
    @Test
    public void testStart() throws Exception {
        System.out.println("start");
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword("qwerty");
        MockNetsSocketServer server = new MockNetsSocketServer(encryptor);
        server.start(12345);
        try {
            SocketFactory sf = SSLSocketFactory.getDefault();
            Socket socket = sf.createSocket(InetAddress.getLocalHost(), 12345);
            
            byte[] bytes = new byte[32];
            Arrays.fill(bytes, new Byte("99"));
            
            socket.getOutputStream().write(bytes);
            Thread.sleep(1000);
            socket.close();
        } finally {
            server.stop();
        }
    }

}
