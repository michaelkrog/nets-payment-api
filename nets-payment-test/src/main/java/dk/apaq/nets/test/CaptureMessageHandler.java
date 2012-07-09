package dk.apaq.nets.test;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;
import dk.apaq.nets.payment.Card;
import dk.apaq.nets.payment.MessageFields;
import dk.apaq.nets.payment.MessageTypes;
import java.io.IOException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author michaelzachariassenkrog
 */
public class CaptureMessageHandler  implements MessageHandler {
    
   private static final DateFormat df = new SimpleDateFormat("yyMMddHHmmss");
    private static final NumberFormat nf = NumberFormat.getIntegerInstance();
    private Card card;
    private String processCode, pointOfService, functionCode, reasonCode,
            cardAcceptorBusinessCode, acquirerReference, cardAcceptorTerminalId,
            cardAcceptorIdCode, cardAcceptorLocation, currencyCode, ode;
    
    private IsoMessage request;
    private IsoMessage response;
    private int amount;
    private Date localTime;
    private Bank bank;
    
    public IsoMessage handleMessage(Bank bank, IsoMessage message) throws IOException {
        try {
            this.bank = bank;
            this.request = message;
            
            parse();
            doCaptureAndResponse();
            return response;
        } catch (ParseException ex) {
            throw new IOException("Unable to handlemessage.", ex);
        }
        
        
    }
    
    
    private void doCaptureAndResponse() {
        String result = bank.capture(ode, amount);
        
        //TODO if result is null
        response = new IsoMessage();
        response.setIsoHeader("PSIP100000");
        response.setType(MessageTypes.AUTHORIZATION_RESPONSE);
        response.copyFieldsFrom(request, MessageFields.FIELD_INDEX_PRIMARY_ACCOUNT_NUMBER,
                                        MessageFields.FIELD_INDEX_PROCESSING_CODE,
                                        MessageFields.FIELD_INDEX_AMOUNT,
                                        MessageFields.FIELD_INDEX_LOCAL_TIME,
                                        MessageFields.FIELD_INDEX_ACQUIRER_REFERENCE,
                                        MessageFields.FIELD_INDEX_CARD_ACCEPTOR_TERMINAL_ID,
                                        MessageFields.FIELD_INDEX_CARD_ACCEPTOR_IDENTIFICATION_CODE,
                                        MessageFields.FIELD_INDEX_ADDITIONAL_DATA_NATIONAL,
                                        MessageFields.FIELD_INDEX_CURRENCY_CODE);
        
        response.setValue(MessageFields.FIELD_INDEX_ACTION_CODE, "000", IsoType.NUMERIC, 3);
        response.setValue(MessageFields.FIELD_INDEX_AUTH_ODE, result, IsoType.LLLVAR, 255);
        
    }
    
    private void parse() throws ParseException {
        String cardNo = request.getField(MessageFields.FIELD_INDEX_PRIMARY_ACCOUNT_NUMBER).toString();
        processCode = request.getField(MessageFields.FIELD_INDEX_PROCESSING_CODE).toString(); 
        amount = nf.parse(request.getField(MessageFields.FIELD_INDEX_AMOUNT).toString()).intValue();
        localTime = df.parse(request.getField(MessageFields.FIELD_INDEX_LOCAL_TIME).toString());
        String expire = request.getField(MessageFields.FIELD_INDEX_EXPIRATION).toString(); 
        pointOfService = request.getField(MessageFields.FIELD_INDEX_POINT_OF_SERVICE).toString(); 
        functionCode = request.getField(MessageFields.FIELD_INDEX_FUNCTION_CODE).toString(); 
        reasonCode = request.getField(MessageFields.FIELD_INDEX_MESSAGE_REASON_CODE).toString(); 
        cardAcceptorBusinessCode = request.getField(MessageFields.FIELD_INDEX_CARD_ACCEPTOR_BUSINESS_CODE).toString(); 
        acquirerReference = request.getField(MessageFields.FIELD_INDEX_ACQUIRER_REFERENCE).toString(); 
        cardAcceptorTerminalId = request.getField(MessageFields.FIELD_INDEX_CARD_ACCEPTOR_TERMINAL_ID).toString(); 
        cardAcceptorIdCode = request.getField(MessageFields.FIELD_INDEX_CARD_ACCEPTOR_IDENTIFICATION_CODE).toString(); 
        cardAcceptorLocation = request.getField(MessageFields.FIELD_INDEX_CARD_ACCEPTOR_NAME_LOCATION).toString(); 
        currencyCode = request.getField(MessageFields.FIELD_INDEX_CURRENCY_CODE).toString(); 
        ode = request.getField(MessageFields.FIELD_INDEX_AUTH_ODE).toString(); 
        
        card = new Card(cardNo, nf.parse(expire.substring(0,2)).intValue(), nf.parse(expire.substring(2,4)).intValue(), "123");
        
    }
    
}
