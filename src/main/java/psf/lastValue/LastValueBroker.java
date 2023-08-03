package psf.lastValue;

import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyAcceptor;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyAcceptorFactory;
import org.apache.activemq.artemis.core.server.ActiveMQServer;
import org.apache.activemq.artemis.core.server.ActiveMQServers;
import org.apache.activemq.artemis.core.settings.impl.AddressSettings;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.region.policy.ConstantPendingMessageLimitStrategy;
import org.apache.activemq.broker.region.policy.PolicyEntry;
import org.apache.activemq.broker.region.policy.PolicyMap;
import org.apache.activemq.broker.region.policy.StorePendingDurableSubscriberMessageStoragePolicy;
import org.apache.activemq.command.ActiveMQTopic;
import org.example.broker.inferenceEngine.InferenceEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import java.io.IOException;
import java.util.*;


public class LastValueBroker {
    public static final String BROKER_URL = "tcp://localhost:61616";

    public static void main(String[] args) {

        BrokerService broker = new BrokerService();
        broker.setPersistent(false);
        broker.setAdvisorySupport(false);


        try {
            broker.addConnector(BROKER_URL);
//TODO: try to using AddressSettings to enable Last Value Queue


//            AddressSettings addressSettings = new AddressSettings();
//            addressSettings.setDefaultLastValueQueue(true);

            PolicyEntry policyEntry = new PolicyEntry();
            //policyEntry.getDurableTopicPrefetch();
            //policyEntry.setPendingDurableSubscriberPolicy(new StorePendingDurableSubscriberMessageStoragePolicy().set);
            ConstantPendingMessageLimitStrategy strategy = new ConstantPendingMessageLimitStrategy();
            strategy.setLimit(1);
            policyEntry.setPendingMessageLimitStrategy(strategy);






            PolicyMap policyMap = new PolicyMap();
            //policyMap.put(new ActiveMQTopic("topic"), policyEntry);
            policyMap.setDefaultEntry(policyEntry);

            broker.setDestinationPolicy(policyMap);


            broker.start();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }


//        String host = "localhost";
//        int port = 61616;
//
//        Map<String,Object> urlMap = new HashMap<>();
//        urlMap.put("protocols", "TCP");
//        urlMap.put("host", host);
//        urlMap.put("port", port);

        //TransportConfiguration transportConfiguration = new TransportConfiguration(NettyAcceptorFactory.class.getName());
        //System.out.println(NettyAcceptor.class.getName());
        //System.out.println(NettyAcceptorFactory.class.getName());
//
//        AddressSettings addressSettings = new AddressSettings();
//        addressSettings.setDefaultLastValueQueue(true);
//
//
//        Configuration configuration = new ConfigurationImpl()
//        .addAddressSetting("#", addressSettings)
//                .setPersistenceEnabled(false)
//                .setSecurityEnabled(false);
        //configuration.setMessageCounterEnabled(true);
        //configuration.setResolveProtocols(true);


//
//        ActiveMQServer server = ActiveMQServers.newActiveMQServer("file://:/Users/minghe/test/src/main/java/psf/lastValue/broker.xml", null, null);
//
//        server.start();


//        System.out.println("Started Embedded Broker");


    }


}

