package org.example.broker.connectionManager;

import org.apache.activemq.advisory.AdvisorySupport;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.region.Destination;
import org.apache.activemq.broker.region.Subscription;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ConsumerInfo;
import org.example.broker.inferenceEngine.InferenceEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConnectionManager {
    //private static int nonFilterConsumer = 0;
    private static final Logger logger = LoggerFactory.getLogger(BrokerService.class);

    private static int subscriptionNum = 0;
    private static int currentSubNum = 0;
    private static int currentConnection = 0;
    private static InferenceEngine inferenceEngine = new InferenceEngine();

    public void connectionManager(BrokerService broker) {
        try {
            if (broker.getBroker().getDestinationMap().size() != 0) {

                Map<ActiveMQDestination, Destination> destMap = broker.getBroker().getDestinationMap();
                Map<ActiveMQDestination, Destination> subMap = nonFilterConsumerMap(destMap);

                if (broker.getCurrentConnections() < currentConnection) {
                    System.out.println("------------------ A Client Disconnected ------------------");
                    currentConnection = broker.getCurrentConnections();
                }

                if (broker.getCurrentConnections() > currentConnection) {
                    // There is a new client connected to the broker
                    long newClientTime = System.currentTimeMillis();
                    logger.info("--------------New Client Arrived at: {}--------------", newClientTime);

                    currentConnection = broker.getCurrentConnections();
                    currentSubNum = getSubscriptionNum(subMap);

                    // if there is a consumer, and more subscriptions --> a new consumer
                    if (subMap.size() != 0 && currentSubNum > subscriptionNum) {
                        //This New Client is a SimpleSubscriber
                        long subFoundTime = System.currentTimeMillis();

                        logger.info("Subscriber Identification Latency is: {}", subFoundTime - newClientTime);
                        logger.debug("New SimpleSubscriber found at: {}", System.currentTimeMillis());

                        subscriptionNum = currentSubNum;

                        logger.debug("-------------------------------------------------------");
                        for (Map.Entry<ActiveMQDestination, Destination> entry : subMap.entrySet()) {
                            ActiveMQDestination key = entry.getKey();
                            Destination value = entry.getValue();

                            if (value.getConsumers().size() != 0) {
                                //call the inference engine
                                logger.debug("this not-meta consumer " + key.getPhysicalName() + " has " + value.getConsumers().size() + " subscription");

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
                                        logger.debug("********* This consumer has a selector, Send metadata to Inference Engine *********");

                                        //Time To Call the Inference Engine
                                        logger.debug("Time To Start Generating Threshold" + System.currentTimeMillis());

                                        inferenceEngine.inferenceEngine(destination, selector, System.currentTimeMillis());
                                    } else {
                                        logger.debug("This subscription does NOT have a selector ");
                                    }
                                }
                            }
                        }
                        //Finish Operations on the New Connected SimpleSubscriber
                        long subFinishTime = System.currentTimeMillis();
                        logger.info("Incoming Subscriber related Latency is: {}", subFinishTime - subFoundTime);

                    } else {
                        long pubFoundTime = System.currentTimeMillis();
                        logger.info("Publisher Identification Latency is: {}", pubFoundTime - newClientTime);
                        //logger.info("New Publisher found at: {}", System.currentTimeMillis());

                        publisherMap(destMap);

                        //Finish Operations on the New Connected Publisher
                        long pubFinishTime = System.currentTimeMillis();
                        logger.info("Incoming Publisher Related Operations Latency is: {}", pubFinishTime - pubFoundTime);
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

            if (key.getPhysicalName().contains(AdvisorySupport.PRODUCER_ADVISORY_TOPIC_PREFIX) && !key.getPhysicalName().contains("filter/")) {
                producerMap.put(key, value);
                logger.debug("producerMap : " + producerMap);
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
            logger.debug(realName);

            Boolean ifExist = inferenceEngine.isThresholdExist(realName);

            if (ifExist) {
                logger.debug("there is a threshold for this publisher, publish this threshold again.");
            } else {
                logger.debug("there is no threshold for this publisher.");
            }
        }

    }


}
