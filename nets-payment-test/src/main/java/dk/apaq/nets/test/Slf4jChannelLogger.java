/*
 * Copyright by Apaq 2011-2013
 */

package dk.apaq.nets.test;

import java.util.Arrays;
import dk.apaq.nets.payment.io.ChannelLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Javadoc
 */
public class Slf4jChannelLogger implements ChannelLogger {

    private static final Logger LOG = LoggerFactory.getLogger(Slf4jChannelLogger.class);
    
    @Override
    public void onMessageSent(byte[] data) {
        LOG.info("Message Sent:\n{}", Arrays.toString(data));
    }

    @Override
    public void onMessageRecieved(byte[] data) {
        LOG.info("Message Received:\n{}", Arrays.toString(data));
    }

}
