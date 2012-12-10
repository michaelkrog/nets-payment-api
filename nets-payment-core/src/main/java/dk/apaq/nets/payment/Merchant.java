package dk.apaq.nets.payment;

/**
 * Defines a Merchant.
 */
public class Merchant {
    
    private String name;
    private String merchantId;
    private Address address;
    private int businessCode;
    
    /**
     * Constructs a new Merchant with no data specified.
     */
    public Merchant() {
    }

    /**
     * Creates a new MERchant.
     * @param merchantId The id of the merchant.
     * @param name The name of the merchant.
     * @param address The address of the merchant. 
     */
    public Merchant(String merchantId, String name, Address address) {
        this.name = name;
        this.merchantId = merchantId;
        this.address = address;
    }

    /**
     * Retrieves the busniess code as given by Nets.
     * @return The busniesscode.
     */
    public int getBusinessCode() {
        return businessCode;
    }

    /**
     * Sets the busniess code.
     * @param businessCode 
     */
    public void setBusinessCode(int businessCode) {
        this.businessCode = businessCode;
    }
    
    /**
     * Retrieves the address of the Merchant.
     * @return The address or null.
     */
    public Address getAddress() {
        return address;
    }

    /**
     * Sets the address.
     * @param address 
     */
    public void setAddress(Address address) {
        this.address = address;
    }
    
    /**
     * Sets the address
     * @param street The streetname incl. housenumber
     * @param postalCode The postal code
     * @param city The city name
     * @param countryCode The 3-letter countrycode.
     */
    public void setAddress(String street, String postalCode, String city, String countryCode) {
        this.address = new Address(street, postalCode, city, countryCode);
    }

    /**
     * Retrieves the merchant id.
     * @return The id or null.
     */
    public String getMerchantId() {
        return merchantId;
    }

    /**
     * Sets the merchant id.
     * @param merchantId 
     */
    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    /**
     * Retrieves the name of the merchant.
     * @return The name or null
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the merchant.
     * @param name 
     */
    public void setName(String name) {
        this.name = name;
    }
    
    
}
