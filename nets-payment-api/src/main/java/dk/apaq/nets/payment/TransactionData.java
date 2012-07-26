package dk.apaq.nets.payment;

import org.joda.money.Money;

/**
 *
 * @author krog
 */
public class TransactionData {
    
    private String _id;
    private String ode;
    private String approvalCode;
    private Money approvedAmount;
    private ActionCode actionCode;

    public String getId() {
        return _id;
    }

    public void setId(String id) {
        this._id = id;
    }

    public ActionCode getActionCode() {
        return actionCode;
    }

    public void setActionCode(ActionCode actionCode) {
        this.actionCode = actionCode;
    }

    public String getApprovalCode() {
        return approvalCode;
    }

    public void setApprovalCode(String approvalCode) {
        this.approvalCode = approvalCode;
    }

    public Money getApprovedAmount() {
        return approvedAmount;
    }

    public void setApprovedAmount(Money approvedAmount) {
        this.approvedAmount = approvedAmount;
    }

    public String getOde() {
        return ode;
    }

    public void setOde(String ode) {
        this.ode = ode;
    }
    
    
}
