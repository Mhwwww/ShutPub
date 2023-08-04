package psf.lastValue;

import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyAcceptor;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyAcceptorFactory;
import org.apache.activemq.artemis.core.server.ActiveMQServer;
import org.apache.activemq.artemis.core.server.ActiveMQServers;
import org.apache.activemq.artemis.core.settings.impl.AddressSettings;
import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.region.Destination;
import org.apache.activemq.broker.region.Subscription;
import org.apache.activemq.broker.region.policy.ConstantPendingMessageLimitStrategy;
import org.apache.activemq.broker.region.policy.PolicyEntry;
import org.apache.activemq.broker.region.policy.PolicyMap;
import org.apache.activemq.broker.region.policy.StorePendingDurableSubscriberMessageStoragePolicy;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.apache.activemq.command.ConsumerInfo;
import org.example.broker.inferenceEngine.InferenceEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import java.io.IOException;
import java.net.URI;
import java.util.*;


public class LastValueBroker {
    public static final String BROKER_URL = "tcp://localhost:61616";

    public static void main(String[] args) {
//        BrokerService broker = new BrokerService();
//        broker.setPersistent(true);

        InferenceEngine inferenceEngine = new InferenceEngine();

        try {
            String configFilePath = "xbean:file:/Users/minghe/test/src/main/java/org/example/broker/activemq.xml";
            BrokerService broker = BrokerFactory.createBroker(URI.create(configFilePath));



            System.out.println(broker.getDestinationPolicy().getDefaultEntry());
            System.out.println(broker.getDestinationPolicy().isEmpty());





            broker.start();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}

