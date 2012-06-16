package dk.apaq.nets.payment;

import org.joda.money.Money;

/**
 *
 * @author krog
 */
public interface IPaymentSystem {

    public void authorize(Merchant merchant, Card card, Money money, String orderId, 
                        boolean recurring, boolean fraudSuspect, String terminalId);
    
    public void renewAuthorization();
 
    public void refund();
    
    public void capture();
    
    public void cancel();
    
}
