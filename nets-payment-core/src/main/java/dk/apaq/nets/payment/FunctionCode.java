package dk.apaq.nets.payment;

/**
 *
 * @author michael
 */
public enum FunctionCode {
    //Auth
    Authorize_Original_Accurate_Amount("100"), Authorize_Original_Estimated_Amount("101"), Authorize_Supplementary_Accurate_Amount("106"), 
    
    //Capture
    Capture_Original_Transaction("200"), Capture_Amount_Accurate("201"), Capture_Amount_Differs("202"), 
    
    //Reverse
    Reverse_FullReversal("400");
  
    private String code;

    private FunctionCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
    
    
    public static FunctionCode fromCode(String code) {
        for(FunctionCode current : FunctionCode.values()) {
            if(code.equals(current.code)) {
                return current;
            }
        }
        return null;
    }
    
    
}
