package dk.apaq.nets.test;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;
import dk.apaq.nets.payment.ActionCode;
import dk.apaq.nets.payment.Card;
import dk.apaq.nets.payment.FunctionCode;
import dk.apaq.nets.payment.MessageFields;
import dk.apaq.nets.payment.MessageTypes;
import java.io.IOException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author michaelzachariassenkrog
 */
public class AuthorizeMessageHandler  implements MessageHandler {

    private static final DateFormat df = new SimpleDateFormat("yyMMddHHmmss");
    private static final NumberFormat nf = NumberFormat.getIntegerInstance();
    private Card card;
    private FunctionCode functionCode;
    private String processCode, pointOfService, reasonCode,
            cardAcceptorBusinessCode, acquirerReference, cardAcceptorTerminalId,
            cardAcceptorIdCode, cardAcceptorLocation, currencyCode;
    
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
            doAuthorizeAndResponse();
            return response;
        } catch (ParseException ex) {
            throw new IOException("Unable to handlemessage.", ex);
        }
        
        
    }
    
    
    private void doAuthorizeAndResponse() {
        ActionCode actionCode = null;
        String ode = null;
        
        if(functionCode != FunctionCode.Authorize_Original_Accurate_Amount) {
            actionCode = ActionCode.Function_Not_Supported;
        } else {
            ode = bank.authorize(card, amount, acquirerReference);
            actionCode = ode == null ? ActionCode.Insufficient_Funds : ActionCode.Approved;
        }
        
        if(ode==null) ode = "";
        response = new IsoMessage();
        response.setIsoHeader("PSIP100000");
        response.setType(MessageTypes.AUTHORIZATION_RESPONSE);
        response.copyFieldsFrom(request, MessageFields.PRIMARY_ACCOUNT_NUMBER,
                                        MessageFields.PROCESSING_CODE,
                                        MessageFields.AMOUNT,
                                        MessageFields.LOCAL_TIME,
                                        MessageFields.CARD_ACCEPTOR_TERMINAL_ID,
                                        MessageFields.CARD_ACCEPTOR_IDENTIFICATION_CODE,
                                        MessageFields.ADDITIONAL_DATA_NATIONAL,
                                        MessageFields.CURRENCY_CODE);
        
        response.setValue(MessageFields.APPROVAL_CODE, "12345", IsoType.ALPHA, 6);
        response.setValue(MessageFields.ACTION_CODE, actionCode.getCode(), IsoType.NUMERIC, 3);
        response.setValue(MessageFields.AUTH_ODE, ode, IsoType.LLLVAR, 255);
        
    }
    
    private void parse() throws ParseException {
        String cardNo = request.getField(MessageFields.PRIMARY_ACCOUNT_NUMBER).toString();
        processCode = request.getField(MessageFields.PROCESSING_CODE).toString(); 
        amount = nf.parse(request.getField(MessageFields.AMOUNT).toString()).intValue();
        localTime = df.parse(request.getField(MessageFields.LOCAL_TIME).toString());
        String expire = request.getField(MessageFields.EXPIRATION).toString(); 
        pointOfService = request.getField(MessageFields.POINT_OF_SERVICE).toString(); 
        functionCode = FunctionCode.fromCode(request.getField(MessageFields.FUNCTION_CODE).toString()); 
        reasonCode = request.getField(MessageFields.MESSAGE_REASON_CODE).toString(); 
        cardAcceptorBusinessCode = request.getField(MessageFields.CARD_ACCEPTOR_BUSINESS_CODE).toString(); 
        acquirerReference = request.getField(MessageFields.ACQUIRER_REFERENCE).toString(); 
        cardAcceptorTerminalId = request.getField(MessageFields.CARD_ACCEPTOR_TERMINAL_ID).toString(); 
        cardAcceptorIdCode = request.getField(MessageFields.CARD_ACCEPTOR_IDENTIFICATION_CODE).toString(); 
        cardAcceptorLocation = request.getField(MessageFields.CARD_ACCEPTOR_NAME_LOCATION).toString(); 
        currencyCode = request.getField(MessageFields.CURRENCY_CODE).toString(); 
        
        card = new Card(cardNo, nf.parse(expire.substring(0,2)).intValue(), nf.parse(expire.substring(2,4)).intValue(), "123");
        
    }
    
}
