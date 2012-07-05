package dk.apaq.nets.payment;

/**
 *
 * @author krog
 */
public enum MessageReason {

    NormalTransaction("0000"), SuspeciousOfFraud("1511"), CustomerCancellation("4000"),
    Unspecified("4001"), SuspectedMalfunction("4002"), SuspectedFraud("4207");

    private String code;

    private MessageReason(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
    
    
}
