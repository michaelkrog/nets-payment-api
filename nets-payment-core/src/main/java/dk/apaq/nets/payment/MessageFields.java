package dk.apaq.nets.payment;

/**
 *
 * @author michaelzachariassenkrog
 */
public class MessageFields {
    public static final int PRIMARY_ACCOUNT_NUMBER = 2;
    public static final int PROCESSING_CODE = 3;
    public static final int AMOUNT = 4;
    public static final int LOCAL_TIME = 12;
    public static final int EXPIRATION = 14;
    public static final int POINT_OF_SERVICE = 22;
    public static final int FUNCTION_CODE = 24;
    public static final int MESSAGE_REASON_CODE = 25;
    public static final int CARD_ACCEPTOR_BUSINESS_CODE = 26;
    public static final int ACQUIRER_REFERENCE = 31;
    public static final int APPROVAL_CODE= 38;
    public static final int ACTION_CODE= 39;
    public static final int CARD_ACCEPTOR_TERMINAL_ID= 41;
    public static final int CARD_ACCEPTOR_IDENTIFICATION_CODE= 42;
    public static final int CARD_ACCEPTOR_NAME_LOCATION= 43;
    public static final int ADDITIONAL_RESPONSE_DATA= 44;
    public static final int ADDITIONAL_DATA_NATIONAL= 47;
    public static final int CURRENCY_CODE= 49;
    public static final int AUTH_ODE= 56;
    public static final int AUTHORIZATION_LIFE_CYCLE= 57;
    
    public static final int PROCESSING_CODE_LENGTH = 6;
    public static final int AMOUNT_LENGTH = 12;
    public static final int LOCAL_TIME_LENGTH = 12;
    public static final int EXPIRATION_LENGTH = 4;
    public static final int POINT_OF_SERVICE_LENGTH = 12;
    public static final int FUNCTION_CODE_LENGTH = 3;
    public static final int MESSAGE_REASON_CODE_LENGTH = 4;
    public static final int CARD_ACCEPTOR_BUSINESS_CODE_LENGTH = 4;
    public static final int APPROVAL_CODE_LENGTH = 6;
    public static final int ACTION_CODE_LENGTH = 3;
    public static final int CARD_ACCEPTOR_TERMINAL_ID_LENGTH = 8;
    public static final int CARD_ACCEPTOR_IDENTIFICATION_CODE_LENGTH = 15;
    public static final int CARD_ACCEPTOR_NAME_LOCATION_LENGTH = 99;
    public static final int CURRENCY_CODE_LENGTH = 3;
    public static final int AUTH_ODE_LENGTH= 255;
    
    
    private MessageFields() {}
}
