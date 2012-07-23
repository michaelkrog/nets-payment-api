package dk.apaq.nets.payment;

import java.util.Date;

/**
 *
 * @author krog
 */
public abstract class Message {

    protected String cardNumber;
    protected int expireYear;
    protected int expireMonth;
    protected ProcessingCode processingCode = ProcessingCode.GoodsAndServices;
    protected long amount;
    protected Date localTime;
    protected PointOfService pointOfService = PointOfService.InternetMerchant;
    protected FunctionCode functionCode = FunctionCode.Authorize_Original_Accurate_Amount;
    protected MessageReason messageReason = MessageReason.NormalTransaction;
    protected String acquirerReference;
    protected String cardAcceptorTerminalId;
    protected Merchant merchant;
    protected ActionCode actionCode = ActionCode.Approved;
    protected String currencyCode;
    protected String ode;
    protected String lifeCycle;
    protected String cvd;
    protected String approvalCode;
    
    
}
