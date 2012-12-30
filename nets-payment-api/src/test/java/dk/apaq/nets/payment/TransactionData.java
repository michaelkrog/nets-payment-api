package dk.apaq.nets.payment;

import dk.apaq.framework.common.beans.finance.Card;
import dk.apaq.nets.payment.ActionCode;
import dk.apaq.nets.payment.ITransactionData;
import org.joda.money.Money;

/**
 *
 * @author krog
 */
public class TransactionData implements ITransactionData {
    //CHECKSTYLE:OFF
    private String _id;
    //CHECKSTYLE:ON
    private String ode;
    private String approvalCode;
    private String processingCode;
    private Money approvedAmount;
    private ActionCode actionCode;
    private Card card;

    @Override
    public String getId() {
        return _id;
    }

    @Override
    public void setId(String id) {
        this._id = id;
    }

    @Override
    public ActionCode getActionCode() {
        return actionCode;
    }

    @Override
    public void setActionCode(ActionCode actionCode) {
        this.actionCode = actionCode;
    }

    @Override
    public String getApprovalCode() {
        return approvalCode;
    }

    @Override
    public void setApprovalCode(String approvalCode) {
        this.approvalCode = approvalCode;
    }

    @Override
    public String getProcessingCode() {
        return processingCode;
    }

    @Override
    public void setProcessingCode(String processingCode) {
        this.processingCode = processingCode;
    }
    
    @Override
    public Money getApprovedAmount() {
        return approvedAmount;
    }

    @Override
    public void setApprovedAmount(Money approvedAmount) {
        this.approvedAmount = approvedAmount;
    }

    @Override
    public String getOde() {
        return ode;
    }

    @Override
    public void setOde(String ode) {
        this.ode = ode;
    }

    @Override
    public Card getCard() {
        return card;
    }

    @Override
    public void setCard(Card card) {
        this.card = card;
    }
    
    
}
