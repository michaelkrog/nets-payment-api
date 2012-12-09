/*
 * Copyright by Apaq 2011-2013
 */
package dk.apaq.nets.payment;

/**
 *
 */
class NetsResponse {
    private final ActionCode actionCode;
    private final String ode;
    private final String approvalCode;
    private final String processingCode;
    
    public NetsResponse(ActionCode actionCode, String ode) {
        this.actionCode = actionCode;
        this.ode = ode;
        this.approvalCode = null;
        this.processingCode = null;
    }

    public NetsResponse(ActionCode actionCode, String ode, String processingCode, String approvalCode) {
        this.actionCode = actionCode;
        this.ode = ode;
        this.processingCode = processingCode;
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

    public String getProcessingCode() {
        return processingCode;
    }
    
}
