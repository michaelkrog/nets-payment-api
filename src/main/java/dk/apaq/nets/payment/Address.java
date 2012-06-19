package dk.apaq.nets.payment;

/**
 *
 * @author krog
 */
public class Address {
    
    private String street;
    private String postalCode;
    private String city;
    private String countryCode;

    public Address() {
    }

    public Address(String street, String postalCode, String city, String countryCode) {
        this.street = street;
        this.postalCode = postalCode;
        this.city = city;
        this.countryCode = countryCode;
    }
    
    

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }
    
    
}
