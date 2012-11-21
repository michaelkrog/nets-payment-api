/*
 * Copyright by Apaq 2011-2013
 */
package dk.apaq.nets.payment;

import dk.apaq.nets.payment.ActionCode;

/**
 *
 */
class NetsResponse {
    private ActionCode actionCode;
    private String ode;
    private String approvalCode;

    public NetsResponse(ActionCode actionCode, String ode) {
        this.actionCode = actionCode;
        this.ode = ode;
    }

    public NetsResponse(ActionCode actionCode, String ode, String approvalCode) {
        this.actionCode = actionCode;
        this.ode = ode;
        this.approvalCode = approvalCode;
    }

    public ActionCode getActionCode() {
        return actionCode;
    }

    public String getOde() {
        return ode;
    }

    public String getApprovalCode() {
        return approvalCode;
    }
    
}
