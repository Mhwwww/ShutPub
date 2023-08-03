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
import org.example.broker.inferenceEngine.InferenceEngine;

import java.util.*;


public class BrokerWithNotification1 {


    public static final String BROKER_URL = "tcp://localhost:61616";
    //private static final Logger LOG = LoggerFactory.getLogger(BrokerService.class);


    private static int nonFilterConsumer = 0;
    private static int subscriptionNum = 0;
    private static int currentSubNum = 0;

    public static void main(String[] args) {
        BrokerService broker = new BrokerService();
        broker.setPersistent(true);

        InferenceEngine inferenceEngine = new InferenceEngine();

        try {
            broker.addConnector(BROKER_URL);
            broker.setAdvisorySupport(true);
            broker.setAdjustUsageLimits(true);
            broker.setBrokerId("testBroker");

            System.out.println(broker.getBrokerContext());



            //TODO: Set Last-Value-Queue || Set Message limit to 1
//            PolicyEntry policyEntry = new PolicyEntry();
//
//            ConstantPendingMessageLimitStrategy strategy = new ConstantPendingMessageLimitStrategy();
//            strategy.setLimit(1);
//            PolicyMap policyMap = new PolicyMap();
//            policyMap.setDefaultEntry(policyEntry);
//
//            AddressSettings addressSettings = new AddressSettings();
//            addressSettings.setDefaultLastValueQueue(true);
//
//            broker.setDestinationPolicy(policyMap);


            //policyEntry.setDestination(new ActiveMQTopic("filter/targetTopic"));
            //policyEntry.setTopic("filter/targetTopic");
            //policyEntry.setMaxPageSize(1);

            ConstantPendingMessageLimitStrategy constantPendingMessageLimitStrategy = new ConstantPendingMessageLimitStrategy();
            constantPendingMessageLimitStrategy.setLimit(1);

            //System.out.println(constantPendingMessageLimitStrategy.getLimit());//1

            PolicyEntry policyEntry = new PolicyEntry();

            ConnectionContext context = new ConnectionContext();
            context.setBroker(broker.getBroker());
            context.setClientId("testBroker");

            ConsumerInfo consumerInformation = new ConsumerInfo();
            //consumerInformation.setSubscriptionName("filter/targetTopic");
            consumerInformation.setDestination(new ActiveMQTopic("filter/targetTopic"));
            consumerInformation.setClientId("filterTargetTopic");

            DurableTopicSubscription filterTopic =  new DurableTopicSubscription(
                    broker.getBroker(),
                    null,
                    context,
                    consumerInformation,
                    false
            );

            policyEntry.configure(broker.getBroker(),null,filterTopic);


            //TODO: need to specify the topic
            //System.out.println(policyEntry.getPendingMessageLimitStrategy().getMaximumPendingMessageLimit());

            PolicyMap policyMap = new PolicyMap();
            policyMap.setDefaultEntry(policyEntry);

            System.out.println(policyMap.getDefaultEntry());

            //policyMap.setPolicyEntries();
            broker.setDestinationPolicy(policyMap);

            broker.start();

//
//            PolicyEntry policyEntry = new PolicyEntry();
//            policyEntry.setPendingMessageLimitStrategy(constantPendingMessageLimitStrategy);
//            policyEntry.setMemoryLimit(1);
//
//            System.out.println(policyEntry);


            //policyMap.setDefaultEntry(policyEntry);
            //policyMap.put(new ActiveMQTopic("filter/targetTopic"), policyEntry);


            //Thread.sleep(5000);
            //System.out.println(broker.getBroker().getDestinationMap());
            //AdvisoryBroker advisoryBroker = new AdvisoryBroker(broker.getBroker());


            for (; ; ) {
                if (broker.getBroker().getDestinationMap().size() != 0) {
                    Map<ActiveMQDestination, Destination> destMap = broker.getBroker().getDestinationMap();
                    //System.out.println(destMap);
                    Map<ActiveMQDestination, Destination> subMap = nonFilterConsumerMap(destMap);
                    //System.out.println(subMap);
                    //System.out.println(broker.getCurrentConnections());

                    currentSubNum = getSubscriptionNum(subMap);
                    if (subMap.size() != 0 && currentSubNum > subscriptionNum) {
                        subscriptionNum = currentSubNum;

                        System.out.println("-------------------------------------------------------");
                        for (Map.Entry<ActiveMQDestination, Destination> entry : subMap.entrySet()) {
                            //for (Map.Entry<Map<ActiveMQDestination, Destination>, Integer> entry : subMap.entrySet()) {
                            ActiveMQDestination key = entry.getKey();
                            Destination value = entry.getValue();

                            if (value.getConsumers().size() != 0) {
                                //call the inference engine
                                System.out.println("this non filter consumer " + key.getPhysicalName() + " has " + value.getConsumers().size() + " subscription");

                                List<Subscription> consumer = value.getConsumers();
                                //subscriptionNum += consumer.size();

                                for (int i = 0; i < consumer.size(); i++) {
                                    ConsumerInfo consumerInfo = consumer.get(i).getConsumerInfo();
                                    //topic--ActiveMQDestination
                                    ActiveMQDestination destination = consumerInfo.getDestination();
                                    //filter--String
                                    String selector = consumerInfo.getSelector();

                                    //consumer id--ConsumerID; consumerInfo.getConsumerId();
                                    //System.out.println("Consumer Destination: " + consumerInfo.getDestination() + " ,Consumer Selector: " + consumerInfo.getSelector());

                                    //1. get "selector & topic"; 2. update threshold for filter topic in "inference engine".
                                    if (selector != null) {
                                        System.out.println("********* This consumer has a selector *********");
                                        System.out.println("********* Send metadata to Inference Engine *********");
                                        inferenceEngine.inferenceEngine(destination, selector);
                                    } else {
                                        System.out.println("This subscription does NOT have a selector ");
                                    }
                                }
                            }
                        }
                    }
                }
                //TODO: should get notification when there is new incoming connection, not just check every one second.
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<ActiveMQDestination, Destination> nonFilterConsumerMap(Map<ActiveMQDestination, Destination> destMap) {

        Map<ActiveMQDestination, Destination> consumerMap = new HashMap<>();

        for (Map.Entry<ActiveMQDestination, Destination> entry : destMap.entrySet()) {
            ActiveMQDestination key = entry.getKey();
            Destination value = entry.getValue();

            // Get the connected consumer and producer for this broker
//            if (key.getPhysicalName().contains(AdvisorySupport.CONSUMER_ADVISORY_TOPIC_PREFIX) && !key.getPhysicalName().contains(AdvisorySupport.CONSUMER_ADVISORY_TOPIC_PREFIX + "Topic.filter/")) {
//                //This an Advisory Producer
//                //System.out.println(AdvisorySupport.CONSUMER_ADVISORY_TOPIC_PREFIX);//ActiveMQ.Advisory.Consumer.
//                System.out.println(key.getPhysicalName());
//                advisoryConsumerMap.put(key, value);
//            }

            // consumer counter
            if (!key.getPhysicalName().contains(AdvisorySupport.ADVISORY_TOPIC_PREFIX) && !key.getPhysicalName().contains("filter/")) {
                //System.out.println("Not Advisory Topic: "+ key.getPhysicalName());
                //System.out.println(key);
                //System.out.println(value);
                int currSubscriptionNum = value.getConsumers().size();

                if (currSubscriptionNum > 0) {//filter out the publisher
                    consumerMap.put(key, value);
                }
            }
        }


        return consumerMap;

    }

    public static int getSubscriptionNum(Map<ActiveMQDestination, Destination> subMap) {
        int subscriptionNum = 0;

        for (Map.Entry<ActiveMQDestination, Destination> entry : subMap.entrySet()) {
            subscriptionNum += entry.getValue().getConsumers().size();
        }

        return subscriptionNum;
    }
}



