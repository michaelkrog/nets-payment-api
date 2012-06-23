package dk.apaq.nets.test;

import dk.apaq.nets.payment.Card;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author michaelzachariassenkrog
 */
public class Bank {

    private List<CardEntry> cards = new ArrayList<CardEntry>();
    
    private class CardEntry {
        private final Card card;
        private int amount;

        public CardEntry(Card card, int amount) {
            this.card = card;
            this.amount = amount;
        }

        public Card getCard() {
            return card;
        }

        public int getAmount() {
            return amount;
        }
    }
    
        
    public void addCard(Card card, int amount) {
        cards.add(new CardEntry(card, amount));
    }
}
