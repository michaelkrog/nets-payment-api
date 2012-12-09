package dk.apaq.nets.payment;

/**
 * MerchantActions as deinfed by Nets.
 * @author michael
 */
public enum MerchantAction {
    
    Approved("A","Approved. Proceed with Capture."), 
    ContactCustomer("CC",""), Try_Again("TN",""), Recurring_Not_Allowed("RN",""), Data_Error("DE","");
    
    private String description;
    private String code;

    private MerchantAction(String code, String description) {
        this.description = description;
        this.code = code;
    }

    /**
     * Retreieves a description for the MerchantAction.
     * @return The description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Retrieves the code of the action.
     * @return The code.
     */
    public String getCode() {
        return code;
    }
    
    
}
