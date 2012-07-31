package dk.apaq.nets.test;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;
import dk.apaq.nets.payment.*;
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
    private String processCode, pointOfService, functionCode,
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
        
        String newOde = null;
        ActionCode actionCode = null;
        
        Bank.Transaction t = bank.getTransaction(ode);
        
        if(t == null || !t.getOrderId().equals(acquirerReference)) {
            //if transaction not found or orderid does not match
            actionCode = ActionCode.No_Card_Record;
        } else {
            if(ProcessingCode.GoodsAndServiceCredit.getCode().equals(processCode)) {
                newOde = bank.refund(ode);
            } else {
                newOde = bank.capture(ode, amount);
            }
            
            actionCode = newOde == null ? ActionCode.Invalid_Amount : ActionCode.Approved;
        }
        
        if(newOde==null) newOde = "";
        response = new IsoMessage();
        response.setIsoHeader("PSIP100000");
        response.setType(MessageTypes.CAPTURE_RESPONSE);
        response.copyFieldsFrom(request, MessageFields.PRIMARY_ACCOUNT_NUMBER,
                                        MessageFields.PROCESSING_CODE,
                                        MessageFields.AMOUNT,
                                        MessageFields.LOCAL_TIME,
                                        MessageFields.ACQUIRER_REFERENCE,
                                        MessageFields.CARD_ACCEPTOR_TERMINAL_ID,
                                        MessageFields.CARD_ACCEPTOR_IDENTIFICATION_CODE,
                                        MessageFields.ADDITIONAL_DATA_NATIONAL,
                                        MessageFields.CURRENCY_CODE);
        
        response.setValue(MessageFields.ACTION_CODE, actionCode.getCode(), IsoType.NUMERIC, 3);
        response.setValue(MessageFields.AUTH_ODE, newOde, IsoType.LLLVAR, 255);
        
    }
    
    private void parse() throws ParseException {
        String cardNo = request.getField(MessageFields.PRIMARY_ACCOUNT_NUMBER).toString();
        processCode = request.getField(MessageFields.PROCESSING_CODE).toString(); 
        amount = nf.parse(request.getField(MessageFields.AMOUNT).toString()).intValue();
        localTime = df.parse(request.getField(MessageFields.LOCAL_TIME).toString());
        String expire = request.getField(MessageFields.EXPIRATION).toString(); 
        pointOfService = request.getField(MessageFields.POINT_OF_SERVICE).toString(); 
        functionCode = request.getField(MessageFields.FUNCTION_CODE).toString(); 
        cardAcceptorBusinessCode = request.getField(MessageFields.CARD_ACCEPTOR_BUSINESS_CODE).toString(); 
        acquirerReference = request.getField(MessageFields.ACQUIRER_REFERENCE).toString(); 
        cardAcceptorTerminalId = request.getField(MessageFields.CARD_ACCEPTOR_TERMINAL_ID).toString(); 
        cardAcceptorIdCode = request.getField(MessageFields.CARD_ACCEPTOR_IDENTIFICATION_CODE).toString(); 
        cardAcceptorLocation = request.getField(MessageFields.CARD_ACCEPTOR_NAME_LOCATION).toString(); 
        currencyCode = request.getField(MessageFields.CURRENCY_CODE).toString(); 
        ode = request.getField(MessageFields.AUTH_ODE).toString(); 
        
        card = new Card(cardNo, nf.parse(expire.substring(0,2)).intValue(), nf.parse(expire.substring(2,4)).intValue(), "123");
        
    }
    
}
