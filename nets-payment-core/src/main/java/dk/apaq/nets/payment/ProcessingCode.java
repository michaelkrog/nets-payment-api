package dk.apaq.nets.payment;

/**
 * Processing codes as specified by Nets.
 */
public enum ProcessingCode {

    //CHECKSTYLE:OFF
    GoodsAndServices("000000"), QuasiCash("110000"), GoodsAndServiceCredit("200000");
    //CHECKSTYLE:ON
    
    private String code;

    private ProcessingCode(String code) {
        this.code = code;
    }

    /**
     * Retrieves the code.
     * @return The code.
     */
    public String getCode() {
        return code;
    }
    
    
}
