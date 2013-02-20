/*
 * Copyright by Apaq 2011-2013
 */

package dk.apaq.nets.test;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;

import dk.apaq.nets.payment.PGTMHeader;
import org.jasypt.encryption.StringEncryptor;

/**
 * Javadoc
 */
public class MockNetsDirectServer extends AbstractMockNetsServer{

    public MockNetsDirectServer(StringEncryptor encryptor) {
        super(encryptor);
    }

    @Override
    public void start() throws IOException {
        
    }

    @Override
    public void stop() {
        
    }

    byte[] handleRequest(byte[] data) throws IOException, ParseException {
        PGTMHeader header = PGTMHeader.fromByteArray(Arrays.copyOf(data, PGTMHeader.HEADER_LENGTH));
        return doHandle(header, data);
    }
}
