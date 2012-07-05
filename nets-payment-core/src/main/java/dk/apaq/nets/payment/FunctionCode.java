package dk.apaq.nets.payment;

/**
 *
 * @author michael
 */
public enum FunctionCode {
    Original_Accurate_Amount("100"), Original_Estimated_Amount("101"), Supplementary_Accurate_Amount("106"), FullReversal("400");
  
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
