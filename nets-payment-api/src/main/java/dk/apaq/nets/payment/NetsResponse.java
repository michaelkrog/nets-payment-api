package dk.apaq.nets.payment;

/**
 *
 * @author michael
 */
public class NetsResponse {
    private ActionCode actionCode;
    private String ode;

    public NetsResponse(ActionCode actionCode, String ode) {
        this.actionCode = actionCode;
        this.ode = ode;
    }

    public ActionCode getActionCode() {
        return actionCode;
    }

    public String getOde() {
        return ode;
    }
    
    
}
