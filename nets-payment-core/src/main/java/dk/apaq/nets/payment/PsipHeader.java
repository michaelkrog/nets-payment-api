package dk.apaq.nets.payment;

/**
 * Defines a PSIPHeader.<br> The ‘PGTM Routing Header’ (‘Payment Gateway Transaction Monitor Routing Header’) is a header used by Nets ‘Payment
 * Gateway’<br> for internal routing. The merchant server uses the PGTM routing header to verify the status of the networking connection by<br>
 * checking the network response code.<br> <br> In case the network response code is '0000', the application should proceed to verify the status of
 * the authorisation/capture in PSIP header.
 */
public class PsipHeader {

    /**
     * A default 'OK PsipHeader.
     */
    public static final PsipHeader OK = new PsipHeader(ErrorCode.OK, true);
    /**
     * The valid length for a Psip Header
     */
    public static final int VALID_HEADER_LENGTH = 10;
    private static final int ERROR_CODE_INDEX = 7;

    /**
     * Error codes used in PSIP header as specified byNets.
     */
    public enum ErrorCode {
        //CHECKSTYLE:OFF

        OK("000"), Bad_Request("400"), Internal_Server_Error("500"), Not_Implemented("501"), Service_Unavailable("503");
        //CHECKSTYLE:ON
        private String code;

        private ErrorCode(String code) {
            this.code = code;
        }

        /**
         * Retrieves the code.
         *
         * @return The code.
         */
        public String getCode() {
            return code;
        }

        /**
         * Retrieves an error code from a strinb error code.
         *
         * @param code The string error code.
         * @return The Error Code.
         */
        public static ErrorCode fromCode(String code) {
            for (ErrorCode current : ErrorCode.values()) {
                if (current.getCode().equals(code)) {
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

    /**
     * Whether the header is valid.
     *
     * @return True/False
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Retrieves the error code.
     *
     * @return The code.
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    @Override
    public String toString() {
        return "PSIP100" + errorCode.getCode();
    }

    /**
     * Builds a PsipHeader from a String defining the header.
     *
     * @param header The header.
     * @return The PsipHeader.
     */
    public static PsipHeader fromString(String header) {
        boolean valid = header.length() == VALID_HEADER_LENGTH && header.startsWith("PSIP100");
        ErrorCode code = valid ? ErrorCode.fromCode(header.substring(ERROR_CODE_INDEX)) : ErrorCode.OK;
        return new PsipHeader(code, valid);
    }
}
