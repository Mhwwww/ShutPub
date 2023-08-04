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
    public static void main(String[] args) {
//        BrokerService broker = new BrokerService();
//        broker.setPersistent(true);

        try {
            String configFilePath = "xbean:file:/Users/minghe/test/src/main/java/org/example/broker/activemq.xml";
            BrokerService broker = BrokerFactory.createBroker(URI.create(configFilePath));

            broker.start();

            for (; ; ) {
                //TODO: if there is a publisher just connect to broker
                // check if there already threshold
                // if exist, send again
                // if not, do nothing,  if a subscriber connected, it will receive the threshold.


                if (broker.getBroker().getDestinationMap().size() != 0) {

                    Map<ActiveMQDestination, Destination> destMap = broker.getBroker().getDestinationMap();
                    Map<ActiveMQDestination, Destination> subMap = nonFilterConsumerMap(destMap);

                    //System.out.println(destMap);
                    //System.out.println(subMap);
                    //System.out.println(broker.getCurrentConnections());

                    //TODO: if the currentConnections increased,
                    // but the consumerSubMap.size() does not change, and the total subscription number does not change.--> no incoming consumer
                    // there is a new publisher connected
                    System.out.println(broker.getCurrentConnections());
                    System.out.println(currentConnection);


                    if (broker.getCurrentConnections() > currentConnection){// either a consumer or a producer
                        currentConnection = broker.getCurrentConnections();

                        //--------------- unmodified-------------------//
                        currentSubNum = getSubscriptionNum(subMap);
                        // if there is a consumer, and more subscriptions --> a new consumer
                        System.out.println(currentSubNum);
                        System.out.println(subscriptionNum);

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
                        }else {
                            System.out.println("The new client is a Producer");
                            publisherMap(destMap);

                            //System.out.println(destMap);
                            //System.out.println(subMap);

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
        Map<ActiveMQDestination, Destination> producerMap = new HashMap<>();


        for (Map.Entry<ActiveMQDestination, Destination> entry : destMap.entrySet()) {
            ActiveMQDestination key = entry.getKey();
            Destination value = entry.getValue();

            // consumer counter
            if (!key.getPhysicalName().contains(AdvisorySupport.ADVISORY_TOPIC_PREFIX) && !key.getPhysicalName().contains("filter/")) {
                //System.out.println("Not Advisory Topic: "+ key.getPhysicalName());//System.out.println(key);//System.out.println(value);
                int currSubscriptionNum = value.getConsumers().size();
                //TODO: later, only process the new connected consumer
                if (currSubscriptionNum > 0) {//filter out the publisher
                    consumerMap.put(key, value);
                }
            }
        }

        return consumerMap;

    }
    public static void publisherMap(Map<ActiveMQDestination, Destination> destMap) {

        Map<ActiveMQDestination, Destination> producerMap = new HashMap<>();

        for (Map.Entry<ActiveMQDestination, Destination> entry : destMap.entrySet()) {
            ActiveMQDestination key = entry.getKey();
            Destination value = entry.getValue();

            //producer map
            if(key.getPhysicalName().contains(AdvisorySupport.PRODUCER_ADVISORY_TOPIC_PREFIX) && ! key.getPhysicalName().contains("filter/")){
                producerMap.put(key, value);
                System.out.println("producerMap : "+ producerMap);
            }
        }

        newConnectedPublisher(producerMap);
    }

    public static int getSubscriptionNum(Map<ActiveMQDestination, Destination> subMap) {
        int subscriptionNum = 0;

        for (Map.Entry<ActiveMQDestination, Destination> entry : subMap.entrySet()) {
            subscriptionNum += entry.getValue().getConsumers().size();
        }

        return subscriptionNum;
    }

    public static void newConnectedPublisher(Map<ActiveMQDestination, Destination> producerMap){

        for (Map.Entry<ActiveMQDestination, Destination> entry : producerMap.entrySet()) {
            ActiveMQDestination key = entry.getKey();
            Destination value = entry.getValue();

            String realName = key.getPhysicalName().replace("ActiveMQ.Advisory.Producer.Topic.", "");
            System.out.println(realName);

            Boolean ifExist = inferenceEngine.isThresholdExist(realName);

            if (ifExist){
                System.out.println("there is a threshold for this publisher");
                System.out.println("should publish this threshold again");

            }else {
                System.out.println("there is no threshold for this publisher yet");
            }
        }

    }





}



