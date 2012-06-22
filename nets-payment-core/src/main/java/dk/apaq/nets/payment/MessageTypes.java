package dk.apaq.nets.payment;

/**
 *
 * @author krog
 */
public class MessageTypes {
    public static final int AUTHORIZATION_REQUEST = 4352; //HEX: 1100
    public static final int AUTHORIZATION_RESPONSE = 4368; //HEX: 1110
    public static final int REVERSAL_ADVICE_REQUEST = 5152; //HEX: 1420
    public static final int REVERSAL_ADVICE_RESPONSE = 5168; //HEX: 1430
    public static final int CAPTURE_REQUEST = 4640; //HEX: 1220
    public static final int CAPTURE_RESPONSE = 4656; //HEX: 1230
    
}
