package dk.apaq.nets.payment.io;

import java.io.*;
import org.apache.commons.io.HexDump;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author krog
 */
public class HexDumpChannelLogger implements ChannelLogger {

    private static final Logger LOG = LoggerFactory.getLogger(HexDumpChannelLogger.class);
    private final File logDirectory;

    public HexDumpChannelLogger(File logDirectory) {
        this.logDirectory = logDirectory;
    }
    
    public void onMessageSent(byte[] data) {
        doLog("to_server", data);
    }

    public void onMessageRecieved(byte[] data) {
        doLog("from_server", data);
    }
    
    private void doLog(String description, byte[] data) {
        try {
            OutputStream out = createOutputStream(description);
            HexDump.dump(data, 0, out, 0);
            out.close();
        } catch(IOException ex) {
            LOG.error("Unable to log hex dump.", ex);
        }
    }
    private OutputStream createOutputStream(String description) throws FileNotFoundException {
        File file = new File(logDirectory, System.currentTimeMillis()+"_"+description+".log");
        return new FileOutputStream(file);
    }
    
}
