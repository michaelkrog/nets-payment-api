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
    private int cvd;

    public Card(String cardHolder, String cardNumber, int expireYear, int expireMonth, int cvd) {
        this.cardHolder = cardHolder;
        this.cardNumber = cardNumber;
        this.expireYear = expireYear;
        this.expireMonth = expireMonth;
        this.cvd = cvd;
    }
    
    public Card(String cardNumber, int expireYear, int expireMonth, int cvd) {
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

    public int getCvd() {
        return cvd;
    }

    public void setCvd(int cvd) {
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
    
    
    
    
}
