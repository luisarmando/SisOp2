/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package messaging;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author luisarmando
 */
public class SubscriberSender extends Thread {
    PSServer server;
    
    public SubscriberSender(PSServer server) {
        this.server = server;
    }

    @Override
    public void run() {
        try {
            while (true) {
                SubscriberSender.sleep(2000);
                this.server.dispatchToSubscribers();
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(SubscriberSender.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}