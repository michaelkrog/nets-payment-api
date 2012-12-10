package dk.apaq.nets.payment;

/**
 *
 * @author krog
 */
public final class MessageTypes {
    //CHECKSTYLE:OFF
    public static final int AUTHORIZATION_REQUEST = 4352; //HEX: 1100
    public static final int AUTHORIZATION_RESPONSE = 4368; //HEX: 1110
    public static final int REVERSAL_ADVICE_REQUEST = 5152; //HEX: 1420
    public static final int REVERSAL_ADVICE_RESPONSE = 5168; //HEX: 1430
    public static final int CAPTURE_REQUEST = 4640; //HEX: 1220
    public static final int CAPTURE_RESPONSE = 4656; //HEX: 1230
    //CHECKSTYLE:ON

    private MessageTypes() { }
}
