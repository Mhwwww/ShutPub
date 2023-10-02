package org.example.broker;

import org.apache.activemq.advisory.AdvisorySupport;
import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.broker.DefaultBrokerFactory;
import org.apache.activemq.broker.region.Destination;
import org.apache.activemq.broker.region.DurableTopicSubscription;
import org.apache.activemq.broker.region.Subscription;
import org.apache.activemq.broker.region.policy.ConstantPendingMessageLimitStrategy;
import org.apache.activemq.broker.region.policy.PolicyEntry;
import org.apache.activemq.broker.region.policy.PolicyMap;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQTopic;
import org.apache.activemq.command.ConsumerInfo;
import org.apache.activemq.store.kahadb.KahaDBPersistenceAdapter;
import org.apache.activemq.xbean.BrokerFactoryBean;
import org.apache.camel.util.FilePathResolver;
import org.example.broker.inferenceEngine.InferenceEngine;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BrokerWithNotification2 {


    public static final String BROKER_URL = "tcp://localhost:61616";

    public static void main(String[] args) {

        try {

            String configFilePath = "xbean:file:/Users/minghe/test/src/main/java/org/example/broker/activemq.xml";

            BrokerService broker = BrokerFactory.createBroker(URI.create(configFilePath));

            broker.start();



        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

