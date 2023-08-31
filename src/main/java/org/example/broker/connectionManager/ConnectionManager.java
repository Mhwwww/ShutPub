package org.example.broker.connectionManager;

import org.apache.activemq.advisory.AdvisorySupport;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.region.Destination;
import org.apache.activemq.broker.region.Subscription;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQTopic;
import org.apache.activemq.command.ConsumerInfo;
import org.example.MetricsCollector;
import org.example.broker.inferenceEngine.InferenceEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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
    private static MetricsCollector metricsCollector = new MetricsCollector();

    public void connectionManager(BrokerService broker) {
        try {
            if (broker.getCurrentConnections() > 0) {
                //if (broker.getBroker().getDestinationMap().size() != 0) {

                Map<ActiveMQDestination, Destination> destMap = broker.getBroker().getDestinationMap();
                Map<ActiveMQDestination, Destination> subMap = nonFilterConsumerMap(destMap);

                if (broker.getCurrentConnections() < currentConnection) {
                    System.out.println("------------------ A Client Disconnected ------------------");
                    logger.debug("Client Disconnected Time: {}", System.nanoTime());
                    currentConnection = broker.getCurrentConnections();
                }

                if (broker.getCurrentConnections() > currentConnection) {
                    // There is a new client connected to the broker
                    long newClientTime = System.nanoTime();
                    logger.info("--------------New Client Arrived at: {}--------------", newClientTime);
                    metricsCollector.logTimestamp("New Client Arrived at", newClientTime);

                    currentConnection = broker.getCurrentConnections();
                    currentSubNum = getSubscriptionNum(subMap);

//                    logger.error(String.valueOf(subMap.size()));
//                    logger.error(String.valueOf(currentSubNum));
//                    logger.error(String.valueOf(subscriptionNum));


                    // if there is a consumer, and more subscriptions --> a new consumer
                    if (subMap.size() != 0 && currentSubNum > subscriptionNum) {
                        //This New Client is a SimpleSubscriber
                        long subFoundTime = System.nanoTime();
                        logger.info("A New Subscriber is Identified at: {}", subFoundTime);
                        metricsCollector.logTimestamp("A New Subscriber is Identified at", subFoundTime);

                        logger.info("Subscriber Identification Latency is: {}", subFoundTime - newClientTime);
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
                                        logger.info("Start Generating Threshold at: {}", System.nanoTime());
                                        metricsCollector.logTimestamp("Start Generating Threshold at", System.nanoTime());

                                        String thresholdUpdated = inferenceEngine.inferenceEngine(destination, selector, System.nanoTime());

                                        if(thresholdUpdated!=null){
                                            Map<ActiveMQDestination, Destination> metaPubMap = publisherMap(destMap);

                                            for (Map.Entry<ActiveMQDestination, Destination> entry1 : metaPubMap.entrySet()) {
                                                ActiveMQDestination metaPubKey = entry1.getKey();
                                                Destination metaPubValue = entry1.getValue();

                                                inferenceEngine.publishThreshold(metaPubKey, thresholdUpdated);

                                            }
                                        }


                                    } else {
                                        logger.debug("This subscription does NOT have a selector ");
                                    }
                                }


                            }
                        }
                        //Finish Operations on the New Connected SimpleSubscriber
                        long subFinishTime = System.nanoTime();
                        logger.info("Finish New Subscriber Related Operations at: {}", subFinishTime);
                        metricsCollector.logTimestamp("Finish New Subscriber Related Operations at", subFinishTime);

                        logger.info("Incoming Subscriber related Latency is: {}", subFinishTime - subFoundTime);

                    } else {//this is a publisher
                        long pubFoundTime = System.nanoTime();
                        logger.info("Publisher Identification Latency is: {}", pubFoundTime - newClientTime);
                        logger.info("New Publisher found at: {}", System.nanoTime());
                        publisherMap(destMap);

                        //Finish Operations on the New Connected Publisher
                        long pubFinishTime = System.nanoTime();
                        logger.info("Finish New Publisher Related Operations at: {}", pubFinishTime);
                        metricsCollector.logTimestamp("Finish New Publisher Related Operations at", pubFinishTime);

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

    public static Map<ActiveMQDestination, Destination> publisherMap(Map<ActiveMQDestination, Destination> destMap) {
        Map<ActiveMQDestination, Destination> metaMap = new HashMap<>();
        Map<ActiveMQDestination, Destination> metaPubMap = new HashMap<>();

        String checkName = null;
        String realName = null;

        //System.out.println("FOUND NEW PUBLISHER, AND CURRENT DEST MAP IS:" + destMap);

        for (Map.Entry<ActiveMQDestination, Destination> entry : destMap.entrySet()) {
            ActiveMQDestination key = entry.getKey();
            Destination value = entry.getValue();

            if (key.getPhysicalName().contains(AdvisorySupport.TOPIC_CONSUMER_ADVISORY_TOPIC_PREFIX + "filter/")) {
                String matchingName = key.getPhysicalName().replace(AdvisorySupport.TOPIC_CONSUMER_ADVISORY_TOPIC_PREFIX, AdvisorySupport.TOPIC_PRODUCER_ADVISORY_TOPIC_PREFIX);
                ActiveMQDestination matchingDest = new ActiveMQTopic(matchingName);

                if (!destMap.containsKey(matchingDest)) {
                    metaMap.put(key, value);
                    //check if this key exist threshold, if exists, publish
                    realName = key.getPhysicalName().replace(AdvisorySupport.TOPIC_CONSUMER_ADVISORY_TOPIC_PREFIX, "");
                    System.out.println("------------real name------------" + realName);
                    checkName = realName.split("/")[1];
                    System.out.println("------------check name------------" + checkName);
                    Boolean ifExist = inferenceEngine.isThresholdExist(checkName, realName);

                    if (ifExist) {
                        System.out.println("there is a threshold for this publisher, publish this threshold again.");
                    } else {
                        System.out.println("there is no threshold for this publisher.");
                    }

                } else {
                    metaPubMap.put(key, value);
                    //todo: if updated?
                }
            }
        }

        System.out.println("META MAP" + metaMap);
        System.out.println("META PUB MAP" + metaPubMap);

        return metaPubMap;
    }


    public static int getSubscriptionNum(Map<ActiveMQDestination, Destination> subMap) {
        int subscriptionNum = 0;

        for (Map.Entry<ActiveMQDestination, Destination> entry : subMap.entrySet()) {
            subscriptionNum += entry.getValue().getConsumers().size();
        }

        return subscriptionNum;
    }

}

//    public static void newConnectedPublisher(Map<ActiveMQDestination, Destination> metaTopicMap) {
//        for (Map.Entry<ActiveMQDestination, Destination> entry : metaTopicMap.entrySet()) {
//            ActiveMQDestination key = entry.getKey();
//            Destination value = entry.getValue();
//
//            //String realName = key.getPhysicalName().replace("ActiveMQ.Advisory.Producer.Topic.", "");
//            String realName = key.getPhysicalName().replace(AdvisorySupport.TOPIC_CONSUMER_ADVISORY_TOPIC_PREFIX, "");
//            System.out.println("------------real name------------"+realName);
//            logger.debug(realName);
//
//            String checkName = realName.split("/")[1];
//            System.out.println("------------check name------------"+checkName);
//
//            long startCheckTime = System.nanoTime();
//            logger.info("Start to Check Filter Existence for the Late Publisher at: {}", startCheckTime);
//            metricsCollector.logTimestamp("Start to Check Filter Existence for the Late Publisher at", startCheckTime);
//
//
//            Boolean ifExist = inferenceEngine.isThresholdExist(checkName,realName);
//
//            long finishCheckTime = System.nanoTime();
//            logger.info("Finish Checking Filter Existence at: {}", finishCheckTime);
//            metricsCollector.logTimestamp("Finish Checking Filter Existence at: ", finishCheckTime);
//
//
//            metricsCollector.logTimestamp("Checking Latency is", finishCheckTime-startCheckTime);
//            logger.info("Checking Latency is: {}", finishCheckTime-startCheckTime);
//
//
//            if (ifExist) {
//                logger.debug("there is a threshold for this publisher, publish this threshold again.");
//            } else {
//                logger.debug("there is no threshold for this publisher.");
//            }
//        }
//
//    }