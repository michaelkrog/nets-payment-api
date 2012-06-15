package dk.apaq.nets.payment;

import org.joda.money.Money;

/**
 *
 * @author krog
 */
public interface IPaymentSystem {

    public enum EstimationFlag {

        Estimated, Accurate, AccurateSupplementary
    }

    public enum ReasonFlag {

        Normal, FraudSuspicion
    }

    public void authorize(Merchant merchant, Card card, Money money, String orderId, boolean recurring, EstimationFlag amountFlag, 
                        ReasonFlag reasonFlag, String terminalId);
    
    
}
