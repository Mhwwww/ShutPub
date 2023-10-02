package org.example.broker.inferenceEngine;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQTopic;
import org.example.MetricsCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.example.cong.Configuration.BROKER_URL;
public class InferenceEngine {
    private static final Logger logger = LoggerFactory.getLogger(BrokerService.class);
    private static final MetricsCollector metricsCollector = new MetricsCollector();
    private final Map<String , String> filterMap = new HashMap<>();
    public String inferenceEngine(ActiveMQDestination destination, String selector, long invocationTime) throws IOException {

        destination.getPhysicalName();

        String[] selectorResult = selector.split("=");
        String constraints = selectorResult[1];//'someID'
        constraints = constraints.substring(1, constraints.length() - 1); //someID

        if (filterMap.containsKey(destination.getPhysicalName())) {// there is already selector on this topic
            //compare current threshold with incoming selector
            //logger.info("current selector: " + filterMap.get(destination.getPhysicalName()));//current selector

                if (filterMap.get(destination.getPhysicalName()).contains(constraints) && !filterMap.get(destination.getPhysicalName()).equals(constraints)) {//someID contains some
                    filterMap.replace(destination.getPhysicalName(), constraints);

                    //if the threshold change, then publish!
                    //logger.info("--------> Going to pass threshold " + filterMap.get(destination.getPhysicalName()) + " to Threshold Publisher");
                    //Update Threshold to Meta Topic///publishThreshold(destination, filterMap.get(destination).toString());

                    long updateFilterTime = System.currentTimeMillis();
                    //logger.info("Updated Filter to Meta Topic at: {}", updateFilterTime);
                    metricsCollector.logTimestamp("Updated Filter to Meta Topic at", updateFilterTime);

                    //logger.info("Latency of Updating Filter is: {}", updateFilterTime - invocationTime);
                    metricsCollector.logTimestamp("Latency of Updating Filter is", updateFilterTime - invocationTime);

                    logger.info("the updated filter Map is " + filterMap);

                    return filterMap.get(destination.getPhysicalName());
                //}//TODO: else{} if two thresholds are "Alice" and "Bob"
            }//TODO: else need add new property
        } else {
            // if new item added! then publish
            filterMap.put(destination.getPhysicalName(), constraints);
           // logger.info("**************** ADD new Pair to the Fiter Map ***********" + filterMap.size());
            //logger.info("--------> Going to pass threshold " + filterMap.get(destination.getPhysicalName()) + " to Threshold Publisher");

            //Publish Threshold to Meta Topic//TODO: only when there is publisher, we will then publish the filer value
            // use wildcard--wildcard only possible for subscriber...
            //todo: publish this threshold to existing matching publisher map.
            //publishThreshold(destination, filterMap.get(destination).toString());

            long addFilterTime = System.currentTimeMillis();

            //logger.info("Publish Filter to a NEW Meta Topic at: {}", addFilterTime);
            metricsCollector.logTimestamp("Publish Filter to a New Meta Topic at", addFilterTime);
           // logger.info("Latency to Publish the NEW Threshold is: {}", System.currentTimeMillis() - invocationTime);
            metricsCollector.logTimestamp("Latency to Publish the NEW Threshold is", System.currentTimeMillis() - invocationTime);

            return filterMap.get(destination.getPhysicalName());
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
            //connection.setClientID("tempConsumer");
            session = connection.createSession(Boolean.FALSE, Session.CLIENT_ACKNOWLEDGE);

            Destination destination = session.createTopic(filterTopic);

            messageProducer = session.createProducer(destination);
            //messageProducer.setDeliveryMode(DeliveryMode.PERSISTENT);

            TextMessage msg = session.createTextMessage(selector);
            msg.setLongProperty("thresholdTimeSent", System.currentTimeMillis());

            //The Time that Published the threshold to Meta Topic
            messageProducer.send(msg);
            //messageProducer.send(msg,2,0, Long.MAX_VALUE);//message, persistent, priority, ttl
            //logger.info("Sent Threshold: " + msg.getText() + " to Filter Topic: " + filterTopic);
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

    public Boolean isThresholdExist(String checkName, String realName) {

        ActiveMQDestination subDestination = new ActiveMQTopic(checkName);
        ActiveMQDestination pubDestination = new ActiveMQTopic(realName);

        System.out.println(filterMap);

        if (filterMap.containsKey(subDestination.getPhysicalName())) {
            //System.out.println("!!!!!Publish to" + pubDestination + "Get Threshold from" + subDestination);
            //logger.info("Publish to: {}, and Get Threshold from: {}" , pubDestination,subDestination);
            // publish the threshold again
            publishThreshold(pubDestination, filterMap.get(subDestination.getPhysicalName()));
            //logger.info("!!!!!!!!!!Publish Threshold for the NEW Publisher!!!!!!!!!!!!!!!");

            return true;
        } else {
            return false;
        }

    }
}
