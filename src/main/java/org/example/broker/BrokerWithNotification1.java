package org.example.broker;

import org.apache.activemq.advisory.AdvisorySupport;
import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.core.settings.impl.AddressSettings;
import org.apache.activemq.broker.*;
import org.apache.activemq.broker.region.*;
import org.apache.activemq.broker.region.cursors.PendingMessageCursor;
import org.apache.activemq.broker.region.policy.*;
import org.apache.activemq.camel.component.broker.BrokerComponent;
import org.apache.activemq.camel.component.broker.BrokerConfiguration;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQTopic;
import org.apache.activemq.command.ConsumerInfo;
import org.apache.activemq.leveldb.DurableSubscription;
import org.apache.activemq.xbean.XBeanBrokerService;
import org.apache.commons.collections.map.HashedMap;
import org.example.broker.connectionManager.ConnectionManager;
import org.example.broker.inferenceEngine.InferenceEngine;

import java.net.URI;
import java.sql.Struct;
import java.util.*;


public class BrokerWithNotification1 {


    public static final String BROKER_URL = "tcp://localhost:61616";
    //private static final Logger LOG = LoggerFactory.getLogger(BrokerService.class);


    private static int nonFilterConsumer = 0;
    private static int subscriptionNum = 0;
    private static int currentSubNum = 0;
    private static int currentConnection = 0;
    static InferenceEngine inferenceEngine = new InferenceEngine();
    static ConnectionManager connectionManager = new ConnectionManager();

    public static void main(String[] args) {
//        BrokerService broker = new BrokerService();
//        broker.setPersistent(true);

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



