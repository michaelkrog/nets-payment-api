package dk.apaq.nets.test;

import dk.apaq.nets.payment.PGTMHeader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.text.ParseException;
import java.util.logging.Level;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author krog
 */
public class MockNetsSocketServer extends AbstractMockNetsServer {

    private static final Logger LOG = LoggerFactory.getLogger(MockNetsSocketServer.class);
    private ServerSocketFactory serverSocketFactory;
    private ServerSocket serverSocket = null;
    private boolean running;

    public MockNetsSocketServer() {
        String path = "src/test/resources/keystore";
        File file = new File(path);
        String password = "123456";
        
        if(!file.exists()) {
            LOG.error("KEYSTORE NOT FOUND!!!\n(Expected at {}.)", file.getAbsolutePath());
        } else {
            LOG.info("Keystore found at {}\n(Password expected be '{}')", file.getAbsolutePath(), password);
            System.setProperty("javax.net.ssl.keyStore", path);
            System.setProperty("javax.net.ssl.keyStorePassword", password);
        }

        serverSocketFactory = SSLServerSocketFactory.getDefault();
    }

    @Override
    public void start(int port) throws UnknownHostException, IOException {
        InetAddress address = Inet4Address.getLocalHost();
        serverSocket = serverSocketFactory.createServerSocket(port,0,address);
        LOG.info("ServerSocket created [address={},port={}].", address, port);
        running = true;
        Thread t = new Thread() {

            @Override
            public void run() {
                Socket socket = null;
                while (running) {
                    try {
                        socket = serverSocket.accept();
                        LOG.debug("Socket created [client={}].", socket.getRemoteSocketAddress());
                        
                        InputStream in = socket.getInputStream();
                        OutputStream out = socket.getOutputStream();
                        
                        LOG.debug("Reading header from socket");
                        PGTMHeader header = readHeader(in);
                        LOG.debug("Header read from socket [length={}]", header.getLength());
                        
                        if(header.getLength()<32) {
                            throw new ParseException("Lnegth of data specified by header is not enough. [length="+header.getLength()+"]", 0);
                        }
                        
                        LOG.debug("Reading data from socket.");
                        byte[] data = readData(in, header.getLength() - 32);
                        LOG.debug("Data read.");
                        
                        LOG.debug("Handling data.");
                        data = doHandle(header, data);
                        
                        LOG.debug("Writing data to socket.");
                        bytesToOutputStream(data, out);
                        
                        LOG.debug("Data written.");
                        
                        in.close();
                        out.close();
                        
                        
                    } catch (Exception ex) {
                        if (running) { //Only report errors if sever not marked as stopped
                            LOG.error("Unable to accept connection.", ex);
                            PGTMHeader failHeader = new PGTMHeader(0, "0001");
                            try {
                                bytesToOutputStream(failHeader.toByteArray(), socket.getOutputStream());
                            } catch (IOException ex1) {
                                LOG.error("Unable to send error data to client.", ex);
                            }
                        }
                    } finally {
                        try {
                            if (socket != null) {
                                LOG.debug("Closing socket.");
                                socket.close();
                            }
                        } catch (IOException ex) {
                            LOG.error("Unable to close socket.", ex);
                        }
                    }
                }
            }
        };
        t.start();
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
        }

    }

    @Override
    public void stop() {
        running = false;
        try {
            if(serverSocket!=null) {
                serverSocket.close();
            }
        } catch (IOException ex) {
            LOG.error("Unable to stop server.", ex);
        }
        serverSocket = null;

    }
}
