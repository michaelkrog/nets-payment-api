package dk.apaq.nets.payment;

/**
 * An Address Entity used for NetRequests.
 */
public class Address {
    
    private String street;
    private String postalCode;
    private String city;
    private String countryCode;

    /**
     * Constructs a new Address.
     */
    public Address() {
    }

    /**
     * Constructs a new Address.
     * @param street The street.
     * @param postalCode The postlcode of the city.
     * @param city The name of the city.
     * @param countryCode The 3-letter countrycode.
     */
    public Address(String street, String postalCode, String city, String countryCode) {
        this.street = street;
        this.postalCode = postalCode;
        this.city = city;
        this.countryCode = countryCode;
    }
    
    
    /**
     * Retrieves the city name.
     * @return The name of the city
     */
    public String getCity() {
        return city;
    }

    /**
     * Sets the name of the city.
     * @param city The name.
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * Retrieves the countrycode.
     * @return The countrycode.
     */
    public String getCountryCode() {
        return countryCode;
    }

    /**
     * Sets the countrycode.
     * @param countryCode 
     */
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    /**
     * Retrieves the postalcode.
     * @return The postalcode.
     */
    public String getPostalCode() {
        return postalCode;
    }

    /**
     * Sets the postalcode.
     * @param postalCode The postalcode.
     */
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    /**
     * Retrieves the streetname.
     * @return The streetname
     */
    public String getStreet() {
        return street;
    }

    /**
     * Sets the street.
     * @param street The street.
     */
    public void setStreet(String street) {
        this.street = street;
    }
    
    
}
