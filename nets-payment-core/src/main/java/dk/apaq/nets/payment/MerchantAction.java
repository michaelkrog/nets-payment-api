package dk.apaq.nets.payment;

/**
 * MerchantActions as deinfed by Nets.
 * @author michael
 */
public enum MerchantAction {
    //CHECKSTYLE:OFF
    Approved("A","Approved. Proceed with Capture."), 
    ContactCustomer("CC","Contact Customer"), Try_Again("TN","Try Again"), Recurring_Not_Allowed("RN","Recurring Payment not allowed"), 
    Data_Error("DE","Data Error");
    //CHECKSTYLE:ON
    
    private String description;
    private String code;

    private MerchantAction(String code, String description) {
        this.description = description;
        this.code = code;
    }

    /**
     * Retrieves a description for the MerchantAction.
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
