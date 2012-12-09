/*
 * Copyright by Apaq 2011-2013
 */
package dk.apaq.nets.payment;

/**
 * Defines a response from Nets.
 */
public class NetsResponse {
    private final ActionCode actionCode;
    private final String ode;
    private final String approvalCode;
    private final String processingCode;
    
    /**
     * Create a new instance.
     * @param actionCode
     * @param ode 
     */
    public NetsResponse(ActionCode actionCode, String ode) {
        this.actionCode = actionCode;
        this.ode = ode;
        this.approvalCode = null;
        this.processingCode = null;
    }

    /**
     * Create a new instance.
     * @param actionCode
     * @param ode
     * @param processingCode
     * @param approvalCode 
     */
    public NetsResponse(ActionCode actionCode, String ode, String processingCode, String approvalCode) {
        this.actionCode = actionCode;
        this.ode = ode;
        this.processingCode = processingCode;
        this.approvalCode = approvalCode;
    }

    /**
     * Retrieves the action code.
     * @return The action code.
     */
    public ActionCode getActionCode() {
        return actionCode;
    }

    /**
     * Retrieves the ode.
     * @return The ode.
     */
    public String getOde() {
        return ode;
    }

    /**
     * Retrieve the approval code.
     * @return 
     */
    public String getApprovalCode() {
        return approvalCode;
    }

    /**
     * Retrieves the processing code.
     * @return 
     */
    public String getProcessingCode() {
        return processingCode;
    }
    
}
