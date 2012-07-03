package dk.apaq.nets.payment;

/**
 *
 * @author michaelzachariassenkrog
 */
public class MessageFields {
    public static final int FIELD_INDEX_PRIMARY_ACCOUNT_NUMBER = 2;
    public static final int FIELD_INDEX_PROCESSING_CODE = 3;
    public static final int FIELD_INDEX_AMOUNT = 4;
    public static final int FIELD_INDEX_LOCAL_TIME = 12;
    public static final int FIELD_INDEX_EXPIRATION = 14;
    public static final int FIELD_INDEX_POINT_OF_SERVICE = 22;
    public static final int FIELD_INDEX_FUNCTION_CODE = 24;
    public static final int FIELD_INDEX_MESSAGE_REASON_CODE = 25;
    public static final int FIELD_INDEX_CARD_ACCEPTOR_BUSINESS_CODE = 26;
    public static final int FIELD_INDEX_ACQUIRER_REFERENCE = 31;
    public static final int FIELD_INDEX_APPROVAL_CODE= 38;
    public static final int FIELD_INDEX_ACTION_CODE= 39;
    public static final int FIELD_INDEX_CARD_ACCEPTOR_TERMINAL_ID= 41;
    public static final int FIELD_INDEX_CARD_ACCEPTOR_IDENTIFICATION_CODE= 42;
    public static final int FIELD_INDEX_CARD_ACCEPTOR_NAME_LOCATION= 43;
    public static final int FIELD_INDEX_ADDITIONAL_RESPONSE_DATA= 44;
    public static final int FIELD_INDEX_ADDITIONAL_DATA_NATIONAL= 47;
    public static final int FIELD_INDEX_CURRENCY_CODE= 49;
    public static final int FIELD_INDEX_AUTH_ODE= 56;
    public static final int FIELD_INDEX_AUTHORIZATION_LIFE_CYCLE= 57;
    
    private MessageFields() {}
}
