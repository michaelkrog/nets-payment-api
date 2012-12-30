package dk.apaq.nets.test;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;
import dk.apaq.framework.common.beans.finance.Card;
import dk.apaq.nets.payment.ActionCode;
import dk.apaq.nets.payment.FunctionCode;
import dk.apaq.nets.payment.MessageFields;
import dk.apaq.nets.payment.MessageTypes;

/**
 *
 * @author michaelzachariassenkrog
 */
public class AuthorizeMessageHandler implements MessageHandler {

    //private static final String DATE_FORMAT = "yyMMddHHmmss";
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getIntegerInstance();
    private Card card;
    private FunctionCode functionCode;
    private String acquirerReference;
    /*private String processCode, pointOfService, reasonCode,
            cardAcceptorBusinessCode, cardAcceptorTerminalId,
            cardAcceptorIdCode, cardAcceptorLocation, currencyCode;*/
    private IsoMessage request;
    private IsoMessage response;
    private int amount;
    //private Date localTime;
    private Bank bank;

    /**
     * @{@inheritDoc} 
     */
    @Override
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
        ActionCode actionCode;
        String ode = null;

        if (functionCode != FunctionCode.Authorize_Original_Accurate_Amount) {
            actionCode = ActionCode.Function_Not_Supported;
        } else {
            ode = bank.authorize(card, amount, acquirerReference);
            actionCode = ode == null ? ActionCode.Insufficient_Funds : ActionCode.Approved;
        }

        if (ode == null) {
            ode = "";
        }
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

        response.setValue(MessageFields.APPROVAL_CODE, "12345", IsoType.ALPHA, MessageFields.APPROVAL_CODE_LENGTH);
        response.setValue(MessageFields.ACTION_CODE, actionCode.getCode(), IsoType.NUMERIC, MessageFields.ACTION_CODE_LENGTH);
        response.setValue(MessageFields.AUTH_ODE, ode, IsoType.LLLVAR, MessageFields.AUTH_ODE_LENGTH);

    }

    private void parse() throws ParseException {
        //DateFormat df = new SimpleDateFormat(DATE_FORMAT);
        String cardNo = request.getField(MessageFields.PRIMARY_ACCOUNT_NUMBER).toString();
        //processCode = request.getField(MessageFields.PROCESSING_CODE).toString();
        amount = NUMBER_FORMAT.parse(request.getField(MessageFields.AMOUNT).toString()).intValue();
        //localTime = df.parse(request.getField(MessageFields.LOCAL_TIME).toString());
        String expire = request.getField(MessageFields.EXPIRATION).toString();
        //pointOfService = request.getField(MessageFields.POINT_OF_SERVICE).toString();
        functionCode = FunctionCode.fromCode(request.getField(MessageFields.FUNCTION_CODE).toString());
        //reasonCode = request.getField(MessageFields.MESSAGE_REASON_CODE).toString();
        //cardAcceptorBusinessCode = request.getField(MessageFields.CARD_ACCEPTOR_BUSINESS_CODE).toString();
        acquirerReference = request.getField(MessageFields.ACQUIRER_REFERENCE).toString();
        //cardAcceptorTerminalId = request.getField(MessageFields.CARD_ACCEPTOR_TERMINAL_ID).toString();
        //cardAcceptorIdCode = request.getField(MessageFields.CARD_ACCEPTOR_IDENTIFICATION_CODE).toString();
        //cardAcceptorLocation = request.getField(MessageFields.CARD_ACCEPTOR_NAME_LOCATION).toString();
        //currencyCode = request.getField(MessageFields.CURRENCY_CODE).toString();

        card = new Card(cardNo, NUMBER_FORMAT.parse(expire.substring(0, 2)).intValue(), NUMBER_FORMAT.parse(expire.substring(2, 4)).intValue(), "123");

    }
}
