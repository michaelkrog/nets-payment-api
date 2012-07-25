package dk.apaq.nets.payment;

/**
 *
 * @author krog
 */
public class NetsException extends Exception {
    
    private ActionCode actionCode;

    public NetsException(String message, ActionCode actionCode) {
        super(message);
        this.actionCode = actionCode;
    }

    public NetsException(ActionCode actionCode) {
        this.actionCode = actionCode;
    }

    public ActionCode getActionCode() {
        return actionCode;
    }
    
}
