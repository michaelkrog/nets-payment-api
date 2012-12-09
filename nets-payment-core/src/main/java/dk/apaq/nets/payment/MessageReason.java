package dk.apaq.nets.payment;

/**
 * MEssageReasons as defined by Nets.
 */
public enum MessageReason {
    //CHECKSTYLE:OFF
    NormalTransaction("0000"), SuspeciousOfFraud("1511"), CustomerCancellation("4000"),
    Unspecified("4001"), SuspectedMalfunction("4002"), SuspectedFraud("4207");
    //CHECKSTYLE:ON
    
    private String code;

    private MessageReason(String code) {
        this.code = code;
    }

    /**
     * Retrieves the code of the messagereason.
     * @return The code.
     */
    public String getCode() {
        return code;
    }
    
    
}
