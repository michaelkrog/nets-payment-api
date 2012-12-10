package dk.apaq.nets.payment.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.HexDump;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A channel logger which creates a Hex Dump of the data sent through a channel.
 */
public class HexDumpChannelLogger implements ChannelLogger {

    private static final Logger LOG = LoggerFactory.getLogger(HexDumpChannelLogger.class);
    private final File logDirectory;

    /**
     * Constructs a new instance.
     *
     * @param logDirectory The directory to write the log files to.
     */
    public HexDumpChannelLogger(File logDirectory) {
        this.logDirectory = logDirectory;
    }

    /**
     * @{@inheritDoc}
     */
    @Override
    public void onMessageSent(byte[] data) {
        doLog("to_server", data);
    }

    /**
     * @{@inheritDoc}
     */
    @Override
    public void onMessageRecieved(byte[] data) {
        doLog("from_server", data);
    }

    private void doLog(String description, byte[] data) {
        try {
            OutputStream out = createOutputStream(description);
            HexDump.dump(data, 0, out, 0);
            out.close();
        } catch (IOException ex) {
            LOG.error("Unable to log hex dump.", ex);
        }
    }

    private OutputStream createOutputStream(String description) throws FileNotFoundException {
        File file = new File(logDirectory, System.currentTimeMillis() + "_" + description + ".log");
        return new FileOutputStream(file);
    }
}
