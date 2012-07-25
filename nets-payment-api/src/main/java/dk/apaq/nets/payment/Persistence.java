package dk.apaq.nets.payment;

/**
 *
 * @author krog
 */
public interface Persistence {
    
    void save(Merchant merchant, String orderId, TransactionData data);
    TransactionData read(Merchant merchant, String orderId);
    void remove(Merchant merchant, String orderId);
}
