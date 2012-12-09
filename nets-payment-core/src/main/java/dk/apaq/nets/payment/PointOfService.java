package dk.apaq.nets.payment;

/**
 * Point of Services as defined by Nets.
 */
public enum PointOfService {
    //CHECKSTYLE:OFF
    InternetMerchant("K00500K00130"), InternetMerchantRecurring("K00540K00130");
    //CHECKSTYLE:ON
    private String code;

    private PointOfService(String code) {
        this.code = code;
    }

    /**
     * The code for the point of service.
     * @return The code.
     */
    public String getCode() {
        return code;
    }
    
    
}
