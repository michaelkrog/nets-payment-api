/*
 * Copyright by Apaq 2011-2013
 */

package dk.apaq.nets.payment;

import java.util.HashMap;
import java.util.Map;
import com.solab.iso8583.MessageFactory;
import com.solab.iso8583.parse.AlphaParseInfo;
import com.solab.iso8583.parse.FieldParseInfo;
import com.solab.iso8583.parse.LllvarParseInfo;
import com.solab.iso8583.parse.LlvarParseInfo;
import com.solab.iso8583.parse.NumericParseInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static dk.apaq.nets.payment.MessageFields.*;
/**
 * Javadoc
 */
public class NetsMessageFactoryCreator {
    
    private static final Logger LOG = LoggerFactory.getLogger(NetsMessageFactoryCreator.class);
    
    private NetsMessageFactoryCreator() { /* EMPTY */ }
    
    public static MessageFactory createFactory() {
        LOG.debug("Creating new Nets MessageFactory");
        MessageFactory factory = new MessageFactory();
        Map<Integer, FieldParseInfo> authRespFields = new HashMap<Integer, FieldParseInfo>();
        authRespFields.put(PRIMARY_ACCOUNT_NUMBER, new LlvarParseInfo());
        authRespFields.put(PROCESSING_CODE, new NumericParseInfo(PROCESSING_CODE_LENGTH));
        authRespFields.put(AMOUNT, new NumericParseInfo(AMOUNT_LENGTH));
        authRespFields.put(LOCAL_TIME, new NumericParseInfo(LOCAL_TIME_LENGTH));
        authRespFields.put(ACQUIRER_REFERENCE, new LlvarParseInfo());
        authRespFields.put(APPROVAL_CODE, new AlphaParseInfo(APPROVAL_CODE_LENGTH));
        authRespFields.put(ACTION_CODE, new NumericParseInfo(ACTION_CODE_LENGTH));
        authRespFields.put(CARD_ACCEPTOR_TERMINAL_ID, new AlphaParseInfo(CARD_ACCEPTOR_TERMINAL_ID_LENGTH));
        authRespFields.put(CARD_ACCEPTOR_IDENTIFICATION_CODE, new AlphaParseInfo(CARD_ACCEPTOR_IDENTIFICATION_CODE_LENGTH));
        authRespFields.put(ADDITIONAL_RESPONSE_DATA, new LlvarParseInfo());
        authRespFields.put(ADDITIONAL_DATA_NATIONAL, new LllvarParseInfo());
        authRespFields.put(CURRENCY_CODE, new AlphaParseInfo(CURRENCY_CODE_LENGTH));
        authRespFields.put(AUTH_ODE, new LllvarParseInfo());
        factory.setParseMap(MessageTypes.AUTHORIZATION_RESPONSE, authRespFields);

        Map<Integer, FieldParseInfo> reverseRespFields = new HashMap<Integer, FieldParseInfo>();
        reverseRespFields.put(PRIMARY_ACCOUNT_NUMBER, new LlvarParseInfo());
        reverseRespFields.put(PROCESSING_CODE, new NumericParseInfo(PROCESSING_CODE_LENGTH));
        reverseRespFields.put(AMOUNT, new NumericParseInfo(AMOUNT_LENGTH));
        reverseRespFields.put(LOCAL_TIME, new NumericParseInfo(LOCAL_TIME_LENGTH));
        reverseRespFields.put(ACQUIRER_REFERENCE, new LlvarParseInfo());
        reverseRespFields.put(ACTION_CODE, new NumericParseInfo(ACTION_CODE_LENGTH));
        reverseRespFields.put(CARD_ACCEPTOR_TERMINAL_ID, new AlphaParseInfo(CARD_ACCEPTOR_TERMINAL_ID_LENGTH));
        reverseRespFields.put(CARD_ACCEPTOR_IDENTIFICATION_CODE, new AlphaParseInfo(CARD_ACCEPTOR_IDENTIFICATION_CODE_LENGTH));
        reverseRespFields.put(CURRENCY_CODE, new AlphaParseInfo(CURRENCY_CODE_LENGTH));
        reverseRespFields.put(AUTH_ODE, new LllvarParseInfo());
        factory.setParseMap(MessageTypes.REVERSAL_ADVICE_RESPONSE, reverseRespFields);

        Map<Integer, FieldParseInfo> captureRespFields = new HashMap<Integer, FieldParseInfo>();
        captureRespFields.put(PRIMARY_ACCOUNT_NUMBER, new LlvarParseInfo());
        captureRespFields.put(PROCESSING_CODE, new NumericParseInfo(PROCESSING_CODE_LENGTH));
        captureRespFields.put(AMOUNT, new NumericParseInfo(AMOUNT_LENGTH));
        captureRespFields.put(LOCAL_TIME, new NumericParseInfo(LOCAL_TIME_LENGTH));
        captureRespFields.put(ACQUIRER_REFERENCE, new LlvarParseInfo());
        captureRespFields.put(ACTION_CODE, new NumericParseInfo(ACTION_CODE_LENGTH));
        captureRespFields.put(CARD_ACCEPTOR_TERMINAL_ID, new AlphaParseInfo(CARD_ACCEPTOR_TERMINAL_ID_LENGTH));
        captureRespFields.put(CARD_ACCEPTOR_IDENTIFICATION_CODE, new AlphaParseInfo(CARD_ACCEPTOR_IDENTIFICATION_CODE_LENGTH));
        captureRespFields.put(ADDITIONAL_RESPONSE_DATA, new LlvarParseInfo());
        captureRespFields.put(ADDITIONAL_DATA_NATIONAL, new LllvarParseInfo());
        captureRespFields.put(CURRENCY_CODE, new AlphaParseInfo(CURRENCY_CODE_LENGTH));
        captureRespFields.put(AUTH_ODE, new LllvarParseInfo());
        factory.setParseMap(MessageTypes.CAPTURE_RESPONSE, captureRespFields);
        
        Map<Integer, FieldParseInfo> authReqFields = new HashMap<Integer, FieldParseInfo>();
        authReqFields.put(PRIMARY_ACCOUNT_NUMBER, new LlvarParseInfo());
        authReqFields.put(PROCESSING_CODE, new NumericParseInfo(PROCESSING_CODE_LENGTH));
        authReqFields.put(AMOUNT, new NumericParseInfo(AMOUNT_LENGTH));
        authReqFields.put(LOCAL_TIME, new NumericParseInfo(LOCAL_TIME_LENGTH));
        authReqFields.put(EXPIRATION, new NumericParseInfo(EXPIRATION_LENGTH));
        authReqFields.put(POINT_OF_SERVICE, new AlphaParseInfo(POINT_OF_SERVICE_LENGTH));
        authReqFields.put(FUNCTION_CODE, new NumericParseInfo(FUNCTION_CODE_LENGTH));
        authReqFields.put(MESSAGE_REASON_CODE, new NumericParseInfo(MESSAGE_REASON_CODE_LENGTH));
        authReqFields.put(CARD_ACCEPTOR_BUSINESS_CODE, new NumericParseInfo(CARD_ACCEPTOR_BUSINESS_CODE_LENGTH));
        authReqFields.put(ACQUIRER_REFERENCE, new LlvarParseInfo());
        authReqFields.put(CARD_ACCEPTOR_TERMINAL_ID, new AlphaParseInfo(CARD_ACCEPTOR_TERMINAL_ID_LENGTH));
        authReqFields.put(CARD_ACCEPTOR_IDENTIFICATION_CODE, new AlphaParseInfo(CARD_ACCEPTOR_IDENTIFICATION_CODE_LENGTH));
        authReqFields.put(CARD_ACCEPTOR_NAME_LOCATION, new LlvarParseInfo());
        authReqFields.put(ADDITIONAL_DATA_NATIONAL, new LllvarParseInfo());
        authReqFields.put(CURRENCY_CODE, new AlphaParseInfo(CURRENCY_CODE_LENGTH));
        authReqFields.put(AUTH_ODE, new LllvarParseInfo());
        factory.setParseMap(MessageTypes.AUTHORIZATION_REQUEST, authReqFields);

        Map<Integer, FieldParseInfo> reverseReqFields = new HashMap<Integer, FieldParseInfo>();
        reverseReqFields.put(PRIMARY_ACCOUNT_NUMBER, new LlvarParseInfo());
        reverseReqFields.put(PROCESSING_CODE, new NumericParseInfo(PROCESSING_CODE_LENGTH));
        reverseReqFields.put(AMOUNT, new NumericParseInfo(AMOUNT_LENGTH));
        reverseReqFields.put(LOCAL_TIME, new NumericParseInfo(LOCAL_TIME_LENGTH));
        reverseReqFields.put(FUNCTION_CODE, new NumericParseInfo(FUNCTION_CODE_LENGTH));
        reverseReqFields.put(MESSAGE_REASON_CODE, new NumericParseInfo(MESSAGE_REASON_CODE_LENGTH));
        reverseReqFields.put(CARD_ACCEPTOR_BUSINESS_CODE, new NumericParseInfo(CARD_ACCEPTOR_BUSINESS_CODE_LENGTH));
        reverseReqFields.put(ACQUIRER_REFERENCE, new LlvarParseInfo());
        reverseReqFields.put(APPROVAL_CODE, new AlphaParseInfo(APPROVAL_CODE_LENGTH));
        reverseReqFields.put(CARD_ACCEPTOR_TERMINAL_ID, new AlphaParseInfo(CARD_ACCEPTOR_TERMINAL_ID_LENGTH));
        reverseReqFields.put(CARD_ACCEPTOR_IDENTIFICATION_CODE, new AlphaParseInfo(CARD_ACCEPTOR_IDENTIFICATION_CODE_LENGTH));
        reverseReqFields.put(CARD_ACCEPTOR_NAME_LOCATION, new LlvarParseInfo());
        reverseReqFields.put(CURRENCY_CODE, new AlphaParseInfo(CURRENCY_CODE_LENGTH));
        reverseReqFields.put(AUTH_ODE, new LllvarParseInfo());
        factory.setParseMap(MessageTypes.REVERSAL_ADVICE_REQUEST, reverseReqFields);

        Map<Integer, FieldParseInfo> captureReqFields = new HashMap<Integer, FieldParseInfo>();
        captureReqFields.put(PRIMARY_ACCOUNT_NUMBER, new LlvarParseInfo());
        captureReqFields.put(PROCESSING_CODE, new NumericParseInfo(PROCESSING_CODE_LENGTH));
        captureReqFields.put(AMOUNT, new NumericParseInfo(AMOUNT_LENGTH));
        captureReqFields.put(LOCAL_TIME, new NumericParseInfo(LOCAL_TIME_LENGTH));
        captureReqFields.put(EXPIRATION, new NumericParseInfo(EXPIRATION_LENGTH));
        captureReqFields.put(POINT_OF_SERVICE, new AlphaParseInfo(POINT_OF_SERVICE_LENGTH));
        captureReqFields.put(FUNCTION_CODE, new NumericParseInfo(FUNCTION_CODE_LENGTH));
        captureReqFields.put(CARD_ACCEPTOR_BUSINESS_CODE, new NumericParseInfo(CARD_ACCEPTOR_BUSINESS_CODE_LENGTH));
        captureReqFields.put(ACQUIRER_REFERENCE, new LlvarParseInfo());
        captureReqFields.put(APPROVAL_CODE, new AlphaParseInfo(APPROVAL_CODE_LENGTH));
        captureReqFields.put(ACTION_CODE, new NumericParseInfo(ACTION_CODE_LENGTH));
        captureReqFields.put(CARD_ACCEPTOR_TERMINAL_ID, new AlphaParseInfo(CARD_ACCEPTOR_TERMINAL_ID_LENGTH));
        captureReqFields.put(CARD_ACCEPTOR_IDENTIFICATION_CODE, new AlphaParseInfo(CARD_ACCEPTOR_IDENTIFICATION_CODE_LENGTH));
        captureReqFields.put(CARD_ACCEPTOR_NAME_LOCATION, new LlvarParseInfo());
        captureReqFields.put(ADDITIONAL_DATA_NATIONAL, new LllvarParseInfo());
        captureReqFields.put(CURRENCY_CODE, new AlphaParseInfo(CURRENCY_CODE_LENGTH));
        captureReqFields.put(AUTH_ODE, new LllvarParseInfo());
        factory.setParseMap(MessageTypes.CAPTURE_REQUEST, captureReqFields);
    
        return factory;
    }
}
