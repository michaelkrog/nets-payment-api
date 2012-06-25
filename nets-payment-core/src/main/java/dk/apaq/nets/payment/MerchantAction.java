package dk.apaq.nets.payment;

/**
 *
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

    public String getDescription() {
        return description;
    }

    public String getCode() {
        return code;
    }
    
    
}
