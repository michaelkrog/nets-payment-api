package dk.apaq.nets.payment;

/**
 *
 * @author krog
 */
public enum PointOfService {
    
    InternetMerchant("K00500K00130"), InternetMerchantRecurring("K00540K00130");
    
    private String code;

    private PointOfService(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
    
    
}
