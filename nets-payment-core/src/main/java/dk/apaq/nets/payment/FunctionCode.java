package dk.apaq.nets.payment;

/**
 * FunctionCode foru se in NetRequests.
 *
 * @author michael
 */
public enum FunctionCode {
    //CHECKSTYLE:OFF
    //Auth

    Authorize_Original_Accurate_Amount("100"), Authorize_Original_Estimated_Amount("101"), Authorize_Supplementary_Accurate_Amount("106"),
    //Capture
    Capture_Original_Transaction("200"), Capture_Amount_Accurate("201"), Capture_Amount_Differs("202"),
    //Reverse
    Reverse_FullReversal("400");
    //CHECKSTYLE:ON
    private String code;

    private FunctionCode(String code) {
        this.code = code;
    }

    /**
     * Retrieves the code as string.
     *
     * @return The code.
     */
    public String getCode() {
        return code;
    }

    /**
     * Retrieves the FunctionCode from string code.
     *
     * @param code The string code.
     * @return The FunctionCode or null if not match found.
     */
    public static FunctionCode fromCode(String code) {
        for (FunctionCode current : FunctionCode.values()) {
            if (code.equals(current.code)) {
                return current;
            }
        }
        return null;
    }
}
