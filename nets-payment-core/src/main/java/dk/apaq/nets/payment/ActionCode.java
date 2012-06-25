package dk.apaq.nets.payment;

/**
 *
 * @author michael
 */
public enum ActionCode {
    Approved("000","Approved","Approved",MerchantAction.Approved),
    Honour_WithId("001","Honour with identification","Decline",MerchantAction.ContactCustomer),
    Approved_Partial_Amount("002","Approved for partitial amount","Partly approved",MerchantAction.Try_Again),
    Approved2("060","Approved","Approved",MerchantAction.Approved),
    Approved3("061","Approved","Approved",MerchantAction.Approved),
    Approved4("063","Approved","Approved",MerchantAction.Approved),
    Do_Not_Honour("100","Do not honour","Decline",MerchantAction.Try_Again),
    Expired_Card("101","Expired card","Decline/Expired card",MerchantAction.ContactCustomer),
    Suspected_Fraud("102","Suspected fraud","Decline",MerchantAction.ContactCustomer),
    Card_Acceptor_Contact_Acquirer("103","Card acceptor contact acquirer","Decline",MerchantAction.ContactCustomer),
    Restricted_Card("104","Restricted card","Decline",MerchantAction.ContactCustomer),
    Call_Security("105","Card Acceptor call Acquirers security department","Decline",MerchantAction.ContactCustomer),
    Pin_Tries_Exceeded("106","Allowable PIN tries exceeded","Decline",MerchantAction.ContactCustomer),
    Refer_Issuer("107","Refer to card issuer","Decline",MerchantAction.ContactCustomer),
    Refer_Issuer_Special("108","Refer to card issuer - special conditions","Decline",MerchantAction.ContactCustomer),
    Invalid_Merchant("109","Invalid merchant","Decline",MerchantAction.Recurring_Not_Allowed),
    Invalid_Amount("110","Invalid account","Decline/Ammount error",MerchantAction.Data_Error),
    Invalid_Card_Number("111","Invalid card number","Decline",MerchantAction.ContactCustomer),
    Pin_Required("112","Pin data required","Decline",MerchantAction.Data_Error),
    Unacceptable_Fee("113","Unacceptable fee","Decline",MerchantAction.Data_Error),
    No_Account("114","No account of type request","Decline",MerchantAction.Data_Error),
    Function_Not_Supported("115","Requested function not supported","Decline",MerchantAction.Data_Error),
    Insufficient_Funds("116","Insufficient funds","Decline",MerchantAction.Try_Again),
    Incorrect_Pin("117","Incorrect pin","Decline",null),
    No_Card_Record("118","No card record","Decline",MerchantAction.ContactCustomer),
    Not_Permitted_Cardholder("119","Transaction not permitted to cardholder","Decline/Invalid transaction",MerchantAction.ContactCustomer),
    Not_Permitted_Terminal("120","Transaction not permitted to terminal","Decline/Invalid transaction",MerchantAction.Recurring_Not_Allowed),
    
    
    ;
/**
     * ￼￼121 ￼ ￼ ￼ ￼ Exceeds withdrawal amount limit ￼ ￼ ￼ Decline ￼ ￼ ￼ DE
122 ￼ ￼ ￼ ￼ Security violation ￼ ￼ ￼ Decline ￼ ￼ ￼ DE
123 ￼ ￼ ￼ ￼ Exceeds withdrawal frequency limit ￼ ￼ ￼ Decline ￼ ￼ ￼ TN
124 ￼ ￼ ￼ ￼ Violation of law ￼ ￼ ￼ Decline ￼ ￼ ￼ DE
125 ￼ ￼ ￼ ￼ Card not effective ￼ ￼ ￼ Decline ￼ ￼ ￼ CC
126 ￼ ￼ ￼ ￼ Invalid pin block ￼ ￼ ￼ Decline
127 ￼ ￼ ￼ ￼ Pin length error ￼ ￼ ￼ Decline
128 ￼ ￼ ￼ ￼ Pin key synch error ￼ ￼ ￼ Decline
129 ￼ ￼ ￼ ￼ Suspected counterfeit card ￼ ￼ ￼ Decline ￼ ￼ ￼ CC
160 ￼ ￼ ￼ ￼ Invalid date ￼ ￼ ￼ Decline ￼ ￼ ￼ DE
161 ￼ ￼ ￼ ￼ Allowable number of PIN tries exceeded ￼ ￼ ￼ Decline ￼ ￼ ￼ CC
162 ￼ ￼ ￼ ￼ Unable to locate previous message ￼ ￼ ￼ Decline ￼ ￼ ￼ DE
164 ￼ ￼ ￼ ￼ Card entry found, below low range ￼ ￼ ￼ Decline ￼ ￼ ￼ CC
165 ￼ ￼ ￼ ￼ Pan length not according to table ￼ ￼ ￼ Decline ￼ ￼ ￼ CC
167 ￼ ￼ ￼ ￼ ￼ Match on previous transaction not ￼ ￼ ￼ Decline ￼ ￼ ￼ ￼ DE

     */

    private String code;
    private String merchantText;
    private String customerText;
    private MerchantAction merchantAction;

    private ActionCode(String code, String merchantText, String customerText, MerchantAction merchantAction) {
        this.code = code;
        this.merchantText = merchantText;
        this.customerText = customerText;
        this.merchantAction = merchantAction;
    }

    public String getCode() {
        return code;
    }

    public MerchantAction getMerchantAction() {
        return merchantAction;
    }

    public String getMerchantText() {
        return merchantText;
    }

    public String getCustomerText() {
        return customerText;
    }
    
    public static ActionCode fromCode(String code) {
        for(ActionCode current : ActionCode.values()) {
            if(code.equals(current.getCode())) {
                return current;
            }
        }
        return null;
    }
    
    
}
