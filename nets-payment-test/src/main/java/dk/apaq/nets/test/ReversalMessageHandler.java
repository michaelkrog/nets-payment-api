package dk.apaq.nets.test;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;
import dk.apaq.nets.payment.ActionCode;
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
public class ReversalMessageHandler implements MessageHandler {

    private static final DateFormat df = new SimpleDateFormat("yyMMddHHmmss");
    private static final NumberFormat nf = NumberFormat.getIntegerInstance();
    
    private String cardNo, processCode, approvalCode, functionCode, reasonCode,
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
            doCancelAndResponse();
            return response;
        } catch (ParseException ex) {
            throw new IOException("Unable to handlemessage.", ex);
        }
    }

    private void doCancelAndResponse() {
        String result = bank.cancel(ode);
        
        ActionCode actionCode = result == null ? ActionCode.No_Card_Record : ActionCode.Approved;
        ode = "";
        
        response = new IsoMessage();
        response.setIsoHeader("PSIP100000");
        response.setType(MessageTypes.REVERSAL_ADVICE_RESPONSE);
        response.copyFieldsFrom(request, MessageFields.PRIMARY_ACCOUNT_NUMBER,
                MessageFields.PROCESSING_CODE,
                MessageFields.AMOUNT,
                MessageFields.LOCAL_TIME,
                MessageFields.ACQUIRER_REFERENCE,
                MessageFields.CARD_ACCEPTOR_TERMINAL_ID,
                MessageFields.CARD_ACCEPTOR_IDENTIFICATION_CODE,
                MessageFields.CURRENCY_CODE,
                MessageFields.AUTHORIZATION_LIFE_CYCLE);

        response.setValue(MessageFields.ACTION_CODE, actionCode.getCode(), IsoType.NUMERIC, 3);
        response.setValue(MessageFields.AUTH_ODE, ode, IsoType.LLLVAR, 255);

    }

    private void parse() throws ParseException {
        cardNo = request.getField(MessageFields.PRIMARY_ACCOUNT_NUMBER).toString();
        processCode = request.getField(MessageFields.PROCESSING_CODE).toString();
        amount = nf.parse(request.getField(MessageFields.AMOUNT).toString()).intValue();
        localTime = df.parse(request.getField(MessageFields.LOCAL_TIME).toString());
        functionCode = request.getField(MessageFields.FUNCTION_CODE).toString();
        reasonCode = request.getField(MessageFields.MESSAGE_REASON_CODE).toString();
        cardAcceptorBusinessCode = request.getField(MessageFields.CARD_ACCEPTOR_BUSINESS_CODE).toString();
        acquirerReference = request.getField(MessageFields.ACQUIRER_REFERENCE).toString();
        approvalCode = request.getField(MessageFields.APPROVAL_CODE).toString();
        cardAcceptorTerminalId = request.getField(MessageFields.CARD_ACCEPTOR_TERMINAL_ID).toString();
        cardAcceptorIdCode = request.getField(MessageFields.CARD_ACCEPTOR_IDENTIFICATION_CODE).toString();
        cardAcceptorLocation = request.getField(MessageFields.CARD_ACCEPTOR_NAME_LOCATION).toString();
        currencyCode = request.getField(MessageFields.CURRENCY_CODE).toString();
        ode = request.getField(MessageFields.AUTH_ODE).toString();
    }
}
