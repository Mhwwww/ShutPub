package org.example.broker;

import org.apache.activemq.broker.*;
import org.example.broker.connectionManager.ConnectionManager;

import java.net.URI;

import static org.example.cong.Configuration.BROKER_INTERVAL;
import static org.example.cong.Configuration.BROKER_URL;

public class BrokerBaseline {
    public static void main(String[] args) {
        BrokerService broker = new BrokerService();
        broker.setPersistent(false);
        broker.setAdvisorySupport(true);

        try {
            broker.addConnector(BROKER_URL);
            broker.start();

            for (;;){
                //connectionManager.connectionManager(broker);
                Thread.sleep(BROKER_INTERVAL);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}



