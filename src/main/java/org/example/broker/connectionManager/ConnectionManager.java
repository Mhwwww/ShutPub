package org.example.broker.connectionManager;

import org.apache.activemq.advisory.AdvisorySupport;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.region.Destination;
import org.apache.activemq.broker.region.Subscription;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ConsumerInfo;
import org.example.broker.inferenceEngine.InferenceEngine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConnectionManager {
    //private static int nonFilterConsumer = 0;
    private static int subscriptionNum = 0;
    private static int currentSubNum = 0;
    private static int currentConnection = 0;
    private static InferenceEngine inferenceEngine = new InferenceEngine();

    public void callInferenceEngine(BrokerService broker) {
        try {
            if (broker.getBroker().getDestinationMap().size() != 0) {

                Map<ActiveMQDestination, Destination> destMap = broker.getBroker().getDestinationMap();
                Map<ActiveMQDestination, Destination> subMap = nonFilterConsumerMap(destMap);

                // either a new consumer or a new producer
                if (broker.getCurrentConnections() > currentConnection) {
                    currentConnection = broker.getCurrentConnections();

                    currentSubNum = getSubscriptionNum(subMap);
                    // if there is a consumer, and more subscriptions --> a new consumer
                    System.out.println(currentSubNum);
                    System.out.println(subscriptionNum);

                    if (subMap.size() != 0 && currentSubNum > subscriptionNum) {
                        subscriptionNum = currentSubNum;

                        System.out.println("-------------------------------------------------------");
                        for (Map.Entry<ActiveMQDestination, Destination> entry : subMap.entrySet()) {
                            ActiveMQDestination key = entry.getKey();
                            Destination value = entry.getValue();

                            if (value.getConsumers().size() != 0) {
                                //call the inference engine
                                System.out.println("this non filter consumer " + key.getPhysicalName() + " has " + value.getConsumers().size() + " subscription");

                                List<Subscription> consumer = value.getConsumers();

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
                    } else {
                        System.out.println("The new client is a Producer");
                        publisherMap(destMap);
                    }
                }
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
            if (key.getPhysicalName().contains(AdvisorySupport.PRODUCER_ADVISORY_TOPIC_PREFIX) && !key.getPhysicalName().contains("filter/")) {
                producerMap.put(key, value);
                System.out.println("producerMap : " + producerMap);
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

    public static void newConnectedPublisher(Map<ActiveMQDestination, Destination> producerMap) {

        for (Map.Entry<ActiveMQDestination, Destination> entry : producerMap.entrySet()) {
            ActiveMQDestination key = entry.getKey();

            String realName = key.getPhysicalName().replace("ActiveMQ.Advisory.Producer.Topic.", "");
            System.out.println(realName);

            Boolean ifExist = inferenceEngine.isThresholdExist(realName);

            if (ifExist) {
                System.out.println("there is a threshold for this publisher, publish this threshold again.");
            } else {
                System.out.println("there is no threshold for this publisher.");
            }
        }

    }


}
