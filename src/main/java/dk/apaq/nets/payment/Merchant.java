package dk.apaq.nets.payment;

/**
 *
 * @author krog
 */
public class Merchant {
    
    private String name;
    private String merchantId;
    private Address address;

    public Merchant() {
    }

    public Merchant(String merchantId, String name, Address address) {
        this.name = name;
        this.merchantId = merchantId;
        this.address = address;
    }
    
    

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
    
    public void setAddress(String street, String postalCode, String city, String countryCode) {
        this.address = new Address(street, postalCode, city, countryCode);
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    
}
