package dk.apaq.nets.payment;

/**
 *
 * @author michael
 */
public class PsipHeader {
    
    public static final PsipHeader OK = new PsipHeader(ErrorCode.OK, true);
    
    public enum ErrorCode {
        OK("000"),Bad_Request("400"), Internal_Server_Error("500"), Not_Implemented("501"), Service_Unavailable("503");

        private String code;
        
        private ErrorCode(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
        
        
        public static ErrorCode fromCode(String code) {
            for(ErrorCode current : ErrorCode.values()) {
                if(current.getCode().equals(code)) {
                    return current;
                }
            }
            return null;
        }
    }
    
    
    
    private final ErrorCode errorCode;
    private final boolean valid;
    
    
    protected PsipHeader(ErrorCode code, boolean valid) {
        this.errorCode = code;
        this.valid = valid;
    }

    public boolean isValid() {
        return valid;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    
    @Override
    public String toString() {
        return "PSIP100" + errorCode.getCode();
    }
    
    public static PsipHeader fromString(String header) {
        boolean valid = header.length() == 10 && header.startsWith("PSIP100");
        ErrorCode code = valid ? ErrorCode.fromCode(header.substring(7)) : ErrorCode.OK;
        return new PsipHeader(code, valid);
    }
}
