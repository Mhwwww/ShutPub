package org.example.broker;

import org.apache.activemq.advisory.AdvisorySupport;
import org.apache.activemq.artemis.core.settings.impl.AddressSettings;
import org.apache.activemq.broker.*;
import org.apache.activemq.broker.region.Destination;
import org.apache.activemq.broker.region.Subscription;
import org.apache.activemq.broker.region.policy.PolicyEntry;
import org.apache.activemq.broker.region.policy.PolicyMap;
import org.apache.activemq.command.*;

import org.apache.activemq.filter.DestinationMapEntry;
import org.apache.activemq.security.MessageAuthorizationPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import java.io.IOException;
import java.util.*;

import org.example.broker.inferenceEngine.*;


public class BrokerWithNotification {


    public static final String BROKER_URL = "tcp://localhost:61616";
    //private static final Logger LOG = LoggerFactory.getLogger(BrokerService.class);


    private static int nonFilterConsumer = 0;
    //private static int subscriptionNum = 0;

    public static void main(String[] args) {
        BrokerService broker = new BrokerService();
        broker.setPersistent(false);


        InferenceEngine inferenceEngine = new InferenceEngine();

        try {
            broker.addConnector(BROKER_URL);
            broker.setAdvisorySupport(true);


            broker.start();
            System.out.println("CONSUMER SYSTEM USAGE "+ broker.getConsumerSystemUsage().getMemoryUsage());
            System.out.println("PRODUCER SYSTEM USAGE"+broker.getProducerSystemUsage().getStoreUsage());
            System.out.println("BROKER SYSTEM USAGE"+broker.getSystemUsage().getStoreUsage());




            //Thread.sleep(5000);
            //System.out.println(broker.getBroker().getDestinationMap());
            //AdvisoryBroker advisoryBroker = new AdvisoryBroker(broker.getBroker());

            for(;;) {
                if(broker.getBroker().getDestinationMap().size() != 0) {
                    Map<ActiveMQDestination, Destination> destMap = broker.getBroker().getDestinationMap();
                    //System.out.println(destMap);
                    Map<ActiveMQDestination, Destination> subMap = nonFilterConsumerMap(destMap);
                   // System.out.println(subMap);

                    System.out.println(broker.getCurrentConnections());

                    if (subMap.size()!=0 && nonFilterConsumer < subMap.size()) {// there is a new consumer
                            nonFilterConsumer = subMap.size();

                            System.out.println("-------------------------------------------------------");
                            for (Map.Entry<ActiveMQDestination, Destination> entry : subMap.entrySet()) {
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
                                        //consumer id--ConsumerID
                                        //consumerInfo.getConsumerId();

                                        //System.out.println("Consumer Destination: " + consumerInfo.getDestination() + " ,Consumer Selector: " + consumerInfo.getSelector());

                                        //1. get "selector & topic"
                                        //2. update threshold for filter topic in "inference engine".

                                        if (selector != null) {
                                            System.out.println("********* This consumer has a selector *********");
                                            System.out.println("********* Send metadata to Inference Engine *********");
                                            inferenceEngine.inferenceEngine(destination, selector,System.currentTimeMillis());
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
        Map<ActiveMQDestination, Destination> advisoryConsumerMap = new HashMap<>();
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

            if (!key.getPhysicalName().contains(AdvisorySupport.ADVISORY_TOPIC_PREFIX) && !key.getPhysicalName().contains("filter/")){
                // System.out.println("Not Advisory Topic: "+ key.getPhysicalName());
                //System.out.println(key);
                //System.out.println(value);
                if (value.getConsumers().size() > 0){//filter out the publisher
                    consumerMap.put(key,value);
                }

            }
        }

        return consumerMap;
    }


}



