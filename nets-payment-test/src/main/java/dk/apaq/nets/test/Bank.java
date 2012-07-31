package dk.apaq.nets.test;

import dk.apaq.nets.payment.Card;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 
 */
public class Bank {

    private List<CardEntry> cards = new ArrayList<CardEntry>();
    private List<Transaction> transactions = new ArrayList<Transaction>();
    
    private class CardEntry {
        private final Card card;
        private long amount;

        public CardEntry(Card card, long amount) {
            this.card = card;
            this.amount = amount;
        }

        public Card getCard() {
            return card;
        }

        public long getAmount() {
            return amount;
        }

        public void setAmount(long amount) {
            this.amount = amount;
        }
        
        
    }
    
    public class Transaction {
        private boolean authorized;
        private boolean captured;
        private boolean cancelled;
        private boolean refunded;
        private CardEntry card;
        private long amount;
        private long capturedAmount;
        private String id = UUID.randomUUID().toString();
        private String ode;

        private Transaction(CardEntry card, long amount) {
            this.card = card;
            this.amount = amount;
            rebuildOde();
        }

        public long getAmount() {
            return amount;
        }
        
        public void markAuthorized() {
            this.authorized = true;
        }

        public boolean isAuthorized() {
            return authorized;
        }
        
        public void markCaptured(long amount) {
            if(!authorized || refunded || cancelled) {
                throw new IllegalStateException("Transaction shouid be in authorized state.");
            }
            card.setAmount(card.getAmount() - amount);
            capturedAmount = amount;
            captured = true;
            rebuildOde();
            
        }

        public boolean isCaptured() {
            return captured;
        }
        
        public void markCancelled() {
            if(!authorized || refunded || captured) {
                throw new IllegalStateException("Transaction should be in authorized state.");
            }
            cancelled = true;
            rebuildOde();
            
        }

        public boolean isCancelled() {
            return cancelled;
        }
        
        public void markRefunded() {
            if(!captured) {
                throw new IllegalStateException("Transaction should be in captured state.");
            }
            card.setAmount(card.getAmount() + capturedAmount);
            refunded = true;
            rebuildOde();
            
        }

        public boolean isRefunded() {
            return refunded;
        }

        public String getOde() {
            return ode;
        }
        
        private void rebuildOde() {
            ode = "oDe123" + UUID.randomUUID().toString();
            ode = ode.replace("-", "");
            for(int i = ode.length();i<255;i++) {
                ode +=" ";
            }
        }
        
        
    }
    
        
    public void addCard(Card card, long amount) {
        cards.add(new CardEntry(card, amount));
    }
    
    public long getAmount(Card card) {
        for(CardEntry entry : cards) {
            if(entry.getCard().equals(card)) {
                return entry.getAmount();
            }
        }
        return 0;
    }
    
    public String authorize(Card card, long amount) {
        for(CardEntry ce : cards) {
            if(ce.getCard().equals(card) && ce.amount >= amount) {
                Transaction t = new Transaction(ce, amount);
                t.markAuthorized();
                transactions.add(t);
                return t.getOde();
            }
        }
        return null;
    }
    
    public String cancel(String ode) {
        for(Transaction t : transactions) {
            if(t.getOde().equals(ode)) {
                t.markCancelled();
                return t.getOde();
            }
        }
        return null;
    }
    
    public String capture(String ode, long amount) {
        for(Transaction t : transactions) {
            if(t.getOde().equals(ode)) {
                t.markCaptured(amount);
                return t.getOde();
            }
        }
        return null;
    }
    
    public String refund(String ode) {
        for(Transaction t : transactions) {
            if(t.getOde().equals(ode)) {
                t.markRefunded();
                return t.getOde();
            }
        }
        return null;
    }
    
    public Transaction getTransaction(String ode) {
        for(Transaction t : transactions) {
            if(t.getOde().equals(ode)) {
                return t;
            }
        }
        return null;
    }
    
    
}
