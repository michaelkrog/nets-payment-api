package dk.apaq.nets.payment;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author krog
 */
public class MemPersistence implements Persistence {

    private final Map<String, TransactionData> map = new HashMap<String, TransactionData>();
    
    private String buildId(Merchant merchant, String orderId) {
        return merchant.getMerchantId() + "_" + orderId;
    }
    
    public void save(Merchant merchant, String orderId, TransactionData data) {
        map.put(buildId(merchant, orderId), data);
    }

    public TransactionData read(Merchant merchant, String orderId) {
        return map.get(buildId(merchant, orderId));
    }

    public void remove(Merchant merchant, String orderId) {
        map.remove(buildId(merchant, orderId));
    }
    
}
