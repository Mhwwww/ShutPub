package org.example.broker;

import org.apache.activemq.broker.*;
import org.example.broker.connectionManager.ConnectionManager;
import java.net.URI;

public class BrokerWithNotification1 {
    public static final String BROKER_URL = "tcp://localhost:61616";
    static ConnectionManager connectionManager = new ConnectionManager();
    public static void main(String[] args) {

        try {
            String configFilePath = "xbean:file:/Users/minghe/test/src/main/java/org/example/broker/activemq.xml";
            BrokerService broker = BrokerFactory.createBroker(URI.create(configFilePath));

            broker.start();

            for (;;){
                connectionManager.callInferenceEngine(broker);
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}



