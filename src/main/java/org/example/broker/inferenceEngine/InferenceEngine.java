package org.example.broker.inferenceEngine;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQTopic;
import org.example.MetricsCollector;
import org.example.broker.connectionManager.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.LogManager;

import static org.example.cong.Configuration.BROKER_URL;

//  1. get multiple subscriptions that :
//          1.1 subscribed to the same Broker;
//          1.2 with same topic;
//          1.3 but have different constraints.
//  2. Testing
//          2.1 test case1: constraints1 = someID, and constraints2 = someIDE==> different constraints have intersection.
//                  return threshold = someID ==> the larger scopt
//          2.2 test case2: constraints1 = alice, and constraints = bob ==> No intersection
//                  return threshold = alice || bob


public class InferenceEngine {
    private static final Logger logger = LoggerFactory.getLogger(BrokerService.class);
    private static MetricsCollector metricsCollector = new MetricsCollector();

    private Map<ActiveMQDestination, Map<String, String>> filterMap = new HashMap<>();



    public String inferenceEngine(ActiveMQDestination destination, String selector, long invocationTime) throws IOException {

        //LogManager.getLogManager().readConfiguration(new FileInputStream("src/main/resources/logging.properties"));

        logger.debug("---------------- Got Consumer Selector Input ----------------");
        logger.debug(destination.getPhysicalName());
        logger.debug(selector);

        destination.getPhysicalName();
        //There is no publisher-sub-threshold topic, because the sub-topics do not have selector.
        //generate adequate threshold
        // split the selector, if the property is the same, then check if the constaints contains each other.
        // for string matching, we set the operator to be '='

        String[] selectorResult = selector.split("=");
        String property = selectorResult[0];//messageContent
        String constraints = selectorResult[1];//'someID'
        constraints = constraints.substring(1, constraints.length() - 1);

        // convert string selector to map, containing property & constraints
        Map<String, String> selectorMap = new HashMap<>();
        selectorMap.put(property, constraints);

        if (filterMap.containsKey(destination)) {// there is already selector on this topic
            //compare current threshold with incoming selector
            logger.debug("current selector: " + filterMap.get(destination));//current selector

            if (filterMap.get(destination).containsKey(property)) {// there is already constraints for the given property
                logger.debug("current constraints: " + filterMap.get(destination).get(property));

                if (filterMap.get(destination).get(property).contains(constraints) && !filterMap.get(destination).get(property).equals(constraints)) {//someID contains some
                    filterMap.replace(destination, selectorMap);

                    //if the threshold change, then publish!
                    logger.debug("--------> Going to pass threshold " + filterMap.get(destination) + " to Threshold Publisher");

                    //Update Threshold to Meta Topic/
                    //TODO: use wildcard, update current threshold to all related topics
                    //publishThreshold(destination, filterMap.get(destination).toString());

                    long updateFilterTime = System.nanoTime();
                    logger.info("Updated Filter to Meta Topic at: {}",updateFilterTime);
                    metricsCollector.logTimestamp("Updated Filter to Meta Topic at", updateFilterTime);

                    logger.info("Latency of Updating Filter is: {}", updateFilterTime-invocationTime);
                    metricsCollector.logTimestamp("Latency of Updating Filter is", updateFilterTime-invocationTime);

                    logger.debug("the updated filter Map is " + filterMap);

                    return filterMap.get(destination).toString();


                }//TODO: else{} if two thresholds are "Alice" and "Bob"

            }//TODO: else need add new property
        } else {
            // if new item added! then publish
            filterMap.put(destination, selectorMap);
            logger.debug("**************** ADD new Pair to the Fiter Map ***********" + filterMap.size());
            logger.debug("--------> Going to pass threshold " + filterMap.get(destination) + " to Threshold Publisher");

            //Publish Threshold to Meta Topic//TODO: only when there is publisher, we will then publish the filer value
            //TODO: use wildcard--wildcard only possible for subscriber...
            //todo: publish this threshold to existing matching publisher map.
            //publishThreshold(destination, filterMap.get(destination).toString());


            long addFilterTime = System.nanoTime();

            logger.info("Publish Filter to a NEW Meta Topic at: {}",addFilterTime);
            metricsCollector.logTimestamp("Publish Filter to a New Meta Topic at", addFilterTime);

            logger.info("Latency to Publish the NEW Threshold is: {}", System.nanoTime()-invocationTime);
            metricsCollector.logTimestamp("Latency to Publish the NEW Threshold is", System.nanoTime()-invocationTime);
            return filterMap.get(destination).toString();
        }
        return null;
    }

    public void publishThreshold(ActiveMQDestination originalDestination, String selector) {
        // 1. get the filter topic that need to publish the threshold
        String filterTopic = "";
//        filterTopic = "filter/" + originalDestination.getPhysicalName();
        filterTopic = originalDestination.getPhysicalName();

        // 2. init a publisher that sends 'threshold' to 'filterTopic'
        Connection connection = null;
        Session session = null;
        MessageProducer messageProducer = null;

        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(BROKER_URL);

        try {
            connection = activeMQConnectionFactory.createConnection();
            connection.setClientID("tempConsumer");
            session = connection.createSession(Boolean.FALSE, Session.CLIENT_ACKNOWLEDGE);

            Destination destination = session.createTopic(filterTopic);

            messageProducer = session.createProducer(destination);
            //messageProducer.setDeliveryMode(DeliveryMode.PERSISTENT);

            TextMessage msg = session.createTextMessage(selector);
            msg.setLongProperty("thresholdTimeSent", System.nanoTime());

            //The Time that Published the threshold to Meta Topic
            messageProducer.send(msg);
            //messageProducer.send(msg,2,0, Long.MAX_VALUE);//message, persistent, priority, ttl

            logger.debug("Sent Threshold: " + msg.getText() + " to Filter Topic: " + filterTopic);

        } catch (JMSException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (JMSException e) {
                throw new RuntimeException(e);
            }

        }


    }

    public Boolean isThresholdExist(String checkName, String realName){

        ActiveMQDestination subDestination = new ActiveMQTopic(checkName);
        ActiveMQDestination pubDestination = new ActiveMQTopic(realName);

        System.out.println(filterMap);

        if (filterMap.containsKey(subDestination)){
            System.out.println("!!!!!Publish to"+pubDestination+"Get Threshold from"+subDestination);
            // publish the threshold again
            publishThreshold(pubDestination, filterMap.get(subDestination).toString());
            logger.debug("!!!!!!!!!!Publish Threshold for the NEW Publisher!!!!!!!!!!!!!!!");

            return true;
        }else {
            return false;
        }

    }


}
