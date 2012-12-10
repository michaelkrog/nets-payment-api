package dk.apaq.nets.payment;

/**
 * Defines a Payment Card.
 */
public class Card {
    
    private static final int HASH_1 = 3;
    private static final int HASH_2 = 23;
            
    private String cardHolder;
    private String cardNumber;
    private int expireYear;
    private int expireMonth;
    private String cvd;

    /**
     * Constructs a new instance of Card.
     * @param cardHolder The name of the Card holder.
     * @param cardNumber Thee card number.
     * @param expireYear The year of expiration fx. 2012
     * @param expireMonth The month of expiration (1=January, 2=February etc.)
     * @param cvd The CVD(security code) from the Card.
     */
    public Card(String cardHolder, String cardNumber, int expireYear, int expireMonth, String cvd) {
        this.cardHolder = cardHolder;
        this.cardNumber = cardNumber;
        this.expireYear = expireYear;
        this.expireMonth = expireMonth;
        this.cvd = cvd;
    }
    
    /**
     * Constructs a new instance of Card without the name of the Card Holder.
     * @param cardNumber Thee card number.
     * @param expireYear The year of expiration fx. 2012
     * @param expireMonth The month of expiration (1=January, 2=February etc.)
     * @param cvd The CVD(security code) from the Card.
     */
    public Card(String cardNumber, int expireYear, int expireMonth, String cvd) {
        this(null, cardNumber, expireYear, expireMonth, cvd);
    }

    /**
     * Retrieves the name of the card holder.
     * @return The name
     */
    public String getCardHolder() {
        return cardHolder;
    }

    /**
     * Retrieves the card number.
     * @return The card number
     */
    public String getCardNumber() {
        return cardNumber;
    }

    /**
     * Retrieves the CVD.
     * @return The cvd.
     */
    public String getCvd() {
        return cvd;
    }

    /**
     * Retrieves the expiration month. It will be the number of month in year for expiration.
     * @return The month.
     */
    public int getExpireMonth() {
        return expireMonth;
    }

    /**
     * Retrieves the expiration year.
     * @return The year.
     */
    public int getExpireYear() {
        return expireYear;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Card other = (Card) obj;
        if ((this.cardHolder == null) ? (other.cardHolder != null) : !this.cardHolder.equals(other.cardHolder)) {
            return false;
        }
        if ((this.cardNumber == null) ? (other.cardNumber != null) : !this.cardNumber.equals(other.cardNumber)) {
            return false;
        }
        if (this.expireYear != other.expireYear) {
            return false;
        }
        if (this.expireMonth != other.expireMonth) {
            return false;
        }
        if (!this.cvd.equals(other.cvd)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = HASH_1;
        hash = HASH_2 * hash + (this.cardHolder != null ? this.cardHolder.hashCode() : 0);
        hash = HASH_2 * hash + (this.cardNumber != null ? this.cardNumber.hashCode() : 0);
        hash = HASH_2 * hash + this.expireYear;
        hash = HASH_2 * hash + this.expireMonth;
        hash = HASH_2 * hash + (this.cvd != null ? this.cvd.hashCode() : 0);
        return hash;
    }
    
    
    
    
}
