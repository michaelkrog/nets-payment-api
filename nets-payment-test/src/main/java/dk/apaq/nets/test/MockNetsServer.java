package dk.apaq.nets.test;

import dk.apaq.nets.payment.Card;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author krog
 */
public class MockNetsServer {
    
    private class CardEntry {
        private Card card;
        private int amount;
    }
    
    private List<CardEntry> cards = new ArrayList<CardEntry>();
    
    public void start() {
        
    }
    
    public void stop() {
        
    }
    
    public boolean isStarted() {
        return false;
    }
}
