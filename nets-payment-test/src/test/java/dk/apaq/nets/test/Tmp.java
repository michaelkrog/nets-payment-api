package dk.apaq.nets.test;

import dk.apaq.nets.test.MockNetsSocketServer;
import dk.apaq.nets.test.MockNetsSocketServer;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

/**
 *
 * @author krog
 */
public class Tmp {

    public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
        System.setProperty("javax.net.ssl.trustStore", "src/test/resources/keystore");
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword("qwerty");
        MockNetsSocketServer server = new MockNetsSocketServer(encryptor,12345);
        server.start();
        try {
            SocketFactory sf = SSLSocketFactory.getDefault();
            Socket socket = sf.createSocket(InetAddress.getLocalHost(), 12345);
            
            byte[] bytes = new byte[32];
            Arrays.fill(bytes, new Byte("0"));
            
            socket.getOutputStream().write(bytes);
            Thread.sleep(5000);
            socket.close();
        } finally {
            server.stop();
        }
    }
}
