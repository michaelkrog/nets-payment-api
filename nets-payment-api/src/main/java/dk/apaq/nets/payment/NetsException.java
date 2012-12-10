package dk.apaq.nets.payment;

/**
 * Exception for erros returned by the Nets server.
 */
public class NetsException extends Exception {
    
    private ActionCode actionCode;

    /**
     * Constructs a new instance.
     * @param message The message explaining the error.
     * @param actionCode The actions code from Nets.
     */
    public NetsException(String message, ActionCode actionCode) {
        super(message);
        this.actionCode = actionCode;
    }

    /**
     * Constructs a new instance.
     * @param actionCode The actions code from Nets.
     */
    public NetsException(ActionCode actionCode) {
        this.actionCode = actionCode;
    }

    /**
     * Retrieves the action code.
     * @return The code
     */
    public ActionCode getActionCode() {
        return actionCode;
    }
    
}
