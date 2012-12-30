/*
 * Copyright by Apaq 2011-2013
 */
package dk.apaq.nets.payment;

import dk.apaq.framework.common.beans.finance.Card;
import org.joda.money.Money;

/**
 * Javadoc
 */
public interface ITransactionData {

    ActionCode getActionCode();

    String getApprovalCode();

    Money getApprovedAmount();

    Card getCard();

    String getId();

    String getOde();
    
    String getProcessingCode();

    void setActionCode(ActionCode actionCode);

    void setApprovalCode(String approvalCode);

    void setApprovedAmount(Money approvedAmount);

    void setCard(Card card);

    void setId(String id);

    void setOde(String ode);

    void setProcessingCode(String processingCode);
    
    
}
