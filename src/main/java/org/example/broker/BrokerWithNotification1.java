package org.example.broker;

import org.apache.activemq.broker.*;
import org.example.broker.connectionManager.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



import java.net.URI;

public class BrokerWithNotification1 {
    public static final String BROKER_URL = "tcp://localhost:61616";

    private static final Logger logger = LoggerFactory.getLogger(BrokerService.class);

    static ConnectionManager connectionManager = new ConnectionManager();
    public static void main(String[] args) {

        try {
            String configFilePath = "xbean:file:/Users/minghe/test/src/main/java/org/example/broker/activemq.xml";
            BrokerService broker = BrokerFactory.createBroker(URI.create(configFilePath));

            broker.start();

            for (;;){
                connectionManager.callInferenceEngine(broker);
                Thread.sleep(900);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}



