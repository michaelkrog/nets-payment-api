package dk.apaq.nets.payment;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

/**
 *
 * @author krog
 */
public class PGTMHeader {

    public static final String DEFAULT_IDENTITY = "0000524800022000000000000032000000000000000000000000";
    private final short length;
    private final String identity;
    private final String networkResponseCode;

    public PGTMHeader(int length, String networkResponseCode) {
        this(length, DEFAULT_IDENTITY, networkResponseCode);
    }
    
    public PGTMHeader(int length, String identity, String networkResponseCode) {
        this.length = (short) (length + 32);
        this.identity = identity;
        this.networkResponseCode = networkResponseCode;
    }

    public int getLength() {
        return length;
    }

    public String getIdentity() {
        return identity;
    }

    public String getNetworkResponseCode() {
        return networkResponseCode;
    }
    
    public byte[] toByteArray() {
        try {
            byte[] lengthData = new byte[]{(byte)(length>>>8),(byte)(length&0xFF)};
            byte[] identityData = Hex.decodeHex(identity.toCharArray());
            byte[] networkData = Hex.decodeHex(networkResponseCode.toCharArray());
            byte[] fixed = Hex.decodeHex("0000".toCharArray());
            byte[] data = new byte[32];
            System.arraycopy(lengthData, 0, data, 0, 2);
            System.arraycopy(identityData, 0, data, 2, 26);
            System.arraycopy(networkData, 0, data, 28, 2);
            System.arraycopy(fixed, 0, data, 30, 2);
            return data;
        } catch (DecoderException ex) {
            throw new IllegalStateException("The header contains data that cannot be converted to bytes.", ex);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PGTMHeader other = (PGTMHeader) obj;
        if (this.length != other.length) {
            return false;
        }
        if ((this.identity == null) ? (other.identity != null) : !this.identity.equals(other.identity)) {
            return false;
        }
        if ((this.networkResponseCode == null) ? (other.networkResponseCode != null) : !this.networkResponseCode.equals(other.networkResponseCode)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + this.length;
        hash = 79 * hash + (this.identity != null ? this.identity.hashCode() : 0);
        hash = 79 * hash + (this.networkResponseCode != null ? this.networkResponseCode.hashCode() : 0);
        return hash;
    }
    
    
    
    public static PGTMHeader fromByteArray(byte[] data) {
        if(data.length != 32) {
            throw new IllegalArgumentException("Length of data must be 32 bytes");
        }
        byte[] lengthData = Arrays.copyOfRange(data, 0, 2);
        byte[] identityData = Arrays.copyOfRange(data, 2, 28);
        byte[] networkData = Arrays.copyOfRange(data, 28, 30);
        byte[] fixedData = Arrays.copyOfRange(data, 30, 32);
        
        short length = (short)(lengthData[1] & 0xFF | (lengthData[0] << 8) );
        length-=32; //We take away the length of the header - 32 bytes
        String identity = new String(Hex.encodeHex(identityData));
        String networkResponseCode = new String(Hex.encodeHex(networkData));
        
        return new PGTMHeader(length, identity, networkResponseCode);
    }
}
