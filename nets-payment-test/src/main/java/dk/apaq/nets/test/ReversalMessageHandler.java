package dk.apaq.nets.test;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;
import dk.apaq.nets.payment.ActionCode;
import dk.apaq.nets.payment.MessageFields;
import dk.apaq.nets.payment.MessageTypes;

/**
 *
 * @author michaelzachariassenkrog
 */
public class ReversalMessageHandler implements MessageHandler {

    //private static final String DATE_FORMAT = "yyMMddHHmmss";
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getIntegerInstance();
    private String ode;
    private String acquirerReference;
    /*private String cardNo, processCode, approvalCode, functionCode, reasonCode,
     cardAcceptorBusinessCode, cardAcceptorTerminalId,
     cardAcceptorIdCode, cardAcceptorLocation, currencyCode;*/
    private IsoMessage request;
    private IsoMessage response;
    private long amount;
    //private Date localTime;
    private Bank bank;

    /**
     * @{@inheritDoc}
     */
    @Override
    public IsoMessage handleMessage(Bank handler, IsoMessage message) throws IOException {
        try {
            this.bank = handler;
            this.request = message;

            parse();
            doCancelAndResponse();
            return response;
        } catch (ParseException ex) {
            throw new IOException("Unable to handlemessage.", ex);
        }
    }

    private void doCancelAndResponse() {
        ActionCode actionCode;
        String newOde = null;

        Bank.Transaction t = bank.getTransaction(ode);
        if (t == null || !t.getOrderId().equals(acquirerReference)) {
            //if transaction not found or orderid does not match
            actionCode = ActionCode.No_Card_Record;
        } else if (t.isAuthorized() && t.getAmount() >= amount) {
            newOde = bank.cancel(ode);
            actionCode = newOde == null ? ActionCode.No_Card_Record : ActionCode.Approved;
        } else {
            actionCode = ActionCode.Invalid_Amount;
        }

        if (newOde == null) {
            newOde = "";
        }
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

        response.setValue(MessageFields.ACTION_CODE, actionCode.getCode(), IsoType.NUMERIC, MessageFields.ACTION_CODE_LENGTH);
        response.setValue(MessageFields.AUTH_ODE, newOde, IsoType.LLLVAR, MessageFields.AUTH_ODE_LENGTH);

    }

    private void parse() throws ParseException {
        //cardNo = request.getField(MessageFields.PRIMARY_ACCOUNT_NUMBER).toString();
        //processCode = request.getField(MessageFields.PROCESSING_CODE).toString();
        amount = NUMBER_FORMAT.parse(request.getField(MessageFields.AMOUNT).toString()).intValue();
        //localTime = DATE_FORMAT.parse(request.getField(MessageFields.LOCAL_TIME).toString());
        //functionCode = request.getField(MessageFields.FUNCTION_CODE).toString();
        //reasonCode = request.getField(MessageFields.MESSAGE_REASON_CODE).toString();
        //cardAcceptorBusinessCode = request.getField(MessageFields.CARD_ACCEPTOR_BUSINESS_CODE).toString();
        acquirerReference = request.getField(MessageFields.ACQUIRER_REFERENCE).toString();
        //approvalCode = request.getField(MessageFields.APPROVAL_CODE).toString();
        //cardAcceptorTerminalId = request.getField(MessageFields.CARD_ACCEPTOR_TERMINAL_ID).toString();
        //cardAcceptorIdCode = request.getField(MessageFields.CARD_ACCEPTOR_IDENTIFICATION_CODE).toString();
        //cardAcceptorLocation = request.getField(MessageFields.CARD_ACCEPTOR_NAME_LOCATION).toString();
        //currencyCode = request.getField(MessageFields.CURRENCY_CODE).toString();
        ode = request.getField(MessageFields.AUTH_ODE).toString();
    }
}
