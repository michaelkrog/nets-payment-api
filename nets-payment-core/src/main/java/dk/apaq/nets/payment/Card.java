package dk.apaq.nets.payment;

/**
 *
 * @author krog
 */
public class Card {
    
    private String cardHolder;
    private String cardNumber;
    private int expireYear;
    private int expireMonth;
    private String cvd;

    public Card(String cardHolder, String cardNumber, int expireYear, int expireMonth, String cvd) {
        this.cardHolder = cardHolder;
        this.cardNumber = cardNumber;
        this.expireYear = expireYear;
        this.expireMonth = expireMonth;
        this.cvd = cvd;
    }
    
    public Card(String cardNumber, int expireYear, int expireMonth, String cvd) {
        this.cardNumber = cardNumber;
        this.expireYear = expireYear;
        this.expireMonth = expireMonth;
        this.cvd = cvd;
    }

    public String getCardHolder() {
        return cardHolder;
    }

    public void setCardHolder(String cardHolder) {
        this.cardHolder = cardHolder;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCvd() {
        return cvd;
    }

    public void setCvd(String cvd) {
        this.cvd = cvd;
    }

    public int getExpireMonth() {
        return expireMonth;
    }

    public void setExpireMonth(int expireMonth) {
        this.expireMonth = expireMonth;
    }

    public int getExpireYear() {
        return expireYear;
    }

    public void setExpireYear(int expireYear) {
        this.expireYear = expireYear;
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
        if (this.cvd != other.cvd) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + (this.cardHolder != null ? this.cardHolder.hashCode() : 0);
        hash = 23 * hash + (this.cardNumber != null ? this.cardNumber.hashCode() : 0);
        hash = 23 * hash + this.expireYear;
        hash = 23 * hash + this.expireMonth;
        hash = 23 * hash + (this.cvd != null ? this.cvd.hashCode() : 0);
        return hash;
    }
    
    
    
    
}
