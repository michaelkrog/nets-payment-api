package dk.apaq.nets.test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import dk.apaq.nets.payment.Card;
import dk.apaq.nets.payment.MessageFields;

/**
 *
 */
public class Bank {

    private List<CardEntry> cards = new ArrayList<CardEntry>();
    private List<Transaction> transactions = new ArrayList<Transaction>();

    /**
     * An entry for a specifc amount of money for a card in the bank.
     */
    private static class CardEntry {

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

    public static class Transaction {

        private boolean authorized;
        private boolean captured;
        private boolean cancelled;
        private boolean refunded;
        private CardEntry card;
        private long amount;
        private long capturedAmount;
        private String orderId;
        //private String id = UUID.randomUUID().toString();
        private String ode;

        private Transaction(CardEntry card, long amount, String orderId) {
            this.card = card;
            this.amount = amount;
            this.orderId = orderId;
            rebuildOde();
        }

        /**
         * Retrieves the amount for the trasnaction
         *
         * @return The amount.
         */
        public long getAmount() {
            return amount;
        }

        /**
         * Retrieves the orderid for the transaction.
         *
         * @return The order id
         */
        public String getOrderId() {
            return orderId;
        }

        /**
         * Marks the transaction authorized.
         */
        public void markAuthorized() {
            this.authorized = true;
        }

        /**
         * Whether the transaction is maked authorized.
         *
         * @return True/False
         */
        public boolean isAuthorized() {
            return authorized;
        }

        /**
         * Marks the transaction as captured.
         *
         * @param amount The amount to capture.
         */
        public void markCaptured(long amount) {
            if (!authorized || refunded || cancelled) {
                throw new IllegalStateException("Transaction shouid be in authorized state.");
            }
            card.setAmount(card.getAmount() - amount);
            capturedAmount = amount;
            captured = true;
            rebuildOde();

        }

        /**
         * Whether this transaction is marked captured.
         *
         * @return True/False
         */
        public boolean isCaptured() {
            return captured;
        }

        /**
         * Marks this transaction as cancelled.
         */
        public void markCancelled() {
            if (!authorized || refunded || captured) {
                throw new IllegalStateException("Transaction should be in authorized state.");
            }
            cancelled = true;
            rebuildOde();

        }

        /**
         * Whether this transaction is marked as cancelled.
         *
         * @return True/False
         */
        public boolean isCancelled() {
            return cancelled;
        }

        /**
         * Marks this
         */
        public void markRefunded() {
            if (!captured) {
                throw new IllegalStateException("Transaction should be in captured state.");
            }
            card.setAmount(card.getAmount() + capturedAmount);
            refunded = true;
            rebuildOde();

        }

        /**
         * Whether this transactions has been refunded.
         *
         * @return True/False
         */
        public boolean isRefunded() {
            return refunded;
        }

        /**
         * Retrieves the Auth Ode for the transaction.
         *
         * @return
         */
        public String getOde() {
            return ode;
        }

        private void rebuildOde() {
            ode = "oDe123" + UUID.randomUUID().toString();
            ode = ode.replace("-", "");
            for (int i = ode.length(); i < MessageFields.AUTH_ODE_LENGTH; i++) {
                ode += " ";
            }
        }
    }

    /**
     * Adds a new card to the bank.
     * @param card The card.
     * @param amount The amount of money available for the card.
     */
    public void addCard(Card card, long amount) {
        cards.add(new CardEntry(card, amount));
    }

    /**
     * Retrieves the amount available for the card.
     * @param card The card
     * @return The amount available.
     */
    public long getAmount(Card card) {
        for (CardEntry entry : cards) {
            if (entry.getCard().equals(card)) {
                return entry.getAmount();
            }
        }
        return 0;
    }

    /**
     * Authorizes a payment.
     * @param card The card.
     * @param amount The amount
     * @param orderId The order id used for reference.
     * @return The Auth Ode or null if unable to authorize.
     */
    public String authorize(Card card, long amount, String orderId) {
        for (CardEntry ce : cards) {
            if (ce.getCard().equals(card) && ce.amount >= amount) {
                Transaction t = new Transaction(ce, amount, orderId);
                t.markAuthorized();
                transactions.add(t);
                return t.getOde();
            }
        }
        return null;
    }

    /**
     * Cancels the authotization.
     * @param ode The ode from authorization.
     * @return The ode if cancelled successfully else null.
     */
    public String cancel(String ode) {
        for (Transaction t : transactions) {
            if (t.getOde().equals(ode)) {
                t.markCancelled();
                return t.getOde();
            }
        }
        return null;
    }

    /**
     * Captures a payment.
     * @param ode The ode from authorization.
     * @param amount The amount to capture.
     * @return The ode if captured successfully else null.
     */
    public String capture(String ode, long amount) {
        for (Transaction t : transactions) {
            if (t.getOde().equals(ode)) {
                t.markCaptured(amount);
                return t.getOde();
            }
        }
        return null;
    }

    /**
     * Refunds a payment.
     * @param ode The ode from authorization.
     * @return The ode if refunded successfully else null.
     */
    public String refund(String ode) {
        for (Transaction t : transactions) {
            if (t.getOde().equals(ode)) {
                t.markRefunded();
                return t.getOde();
            }
        }
        return null;
    }

    /**
     * Retrieves a transaction
     * @param ode The ode from authorization.
     * @return The transaction if found else null.
     */
    public Transaction getTransaction(String ode) {
        for (Transaction t : transactions) {
            if (t.getOde().equals(ode)) {
                return t;
            }
        }
        return null;
    }
}
