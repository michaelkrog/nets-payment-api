package dk.apaq.nets.test;

import com.solab.iso8583.IsoMessage;
import dk.apaq.nets.payment.MessageFields;

/**
 *
 * @author michaelzachariassenkrog
 */
public class AuthorizeMessageHandler  implements MessageHandler {

    public IsoMessage handleMessage(Bank bank, IsoMessage message) {
        String card = message.getField(MessageFields.FIELD_INDEX_PRIMARY_ACCOUNT_NUMBER).toString();
        String processCode = message.getField(MessageFields.FIELD_INDEX_PROCESSING_CODE).toString(); 
        String expire = message.getField(MessageFields.FIELD_INDEX_EXPIRATION).toString(); 
        
        return null;
    }
    
}
