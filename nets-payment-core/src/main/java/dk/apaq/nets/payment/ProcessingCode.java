package dk.apaq.nets.payment;

/**
 *
 * @author krog
 */
public enum ProcessingCode {

    GoodsAndServices("000000"), QuasiCash("110000"), GoodsAndServiceCredit("200000");
    
    
    private String code;

    private ProcessingCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
    
    
}
