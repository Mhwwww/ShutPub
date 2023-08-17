package org.example.publisher;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.command.ActiveMQDestination;
import org.example.MetricsCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;


public class PsfMW {
    /*
    1. subscribe to filter topic
    2. return threshold
    3. send messages to broker
     */
    // Map to store AtomicReferences for each producer's threshold
    //private Map<MessageProducer, AtomicReference<Message>> producerThresholdMap = new HashMap<>();
    private AtomicReference<String> threshold = new AtomicReference<>(null);
    private static final Logger logger = LoggerFactory.getLogger(BrokerService.class);
    private String currentSelector = null;

    private static MetricsCollector metricsCollector = new MetricsCollector();

    public void subToFilter(MessageProducer producer, Session session, Connection connection) {
        try {
            ActiveMQDestination pubDestination = ActiveMQDestination.transform(producer.getDestination());
            String fiterDestination = "filter/" + pubDestination.getPhysicalName();
            logger.debug(fiterDestination);
            Topic filterTopic = session.createTopic(fiterDestination);
            MessageConsumer filterSubscriber = session.createConsumer(filterTopic);
            //TopicSubscriber filterSubscriber = session.createDurableSubscriber(filterTopic, fiterDestination);
            connection.start();

            //map filterTopic with currentThreshold
            filterSubscriber.setMessageListener(message1 -> {
                // Handle incoming messages here
                if (message1 instanceof TextMessage) {
                    try {
                        String result = ((TextMessage) message1).getText();
                        long gotThresholdTime = System.nanoTime();
                        logger.info("{}: Got Threshold {} at: {}, Threshold arriving latency is: {}", connection.getClientID(), result, gotThresholdTime, gotThresholdTime - message1.getLongProperty("thresholdTimeSent"));
                        metricsCollector.logMiddlewareFilterTimestamp(connection.getClientID(), "Middleware Receives the Filter at", gotThresholdTime, "filter sent time is: ", message1.getLongProperty("thresholdTimeSent") );

                        if (!Objects.equals(currentSelector, result)) {
                            logger.debug("Threshold changed, New threshold is: " + result);
                            currentSelector = result;
                        } else {
                            logger.debug("No changes in threshold");
                        }
                    } catch (JMSException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * middleware publisher
     * */

    public void fiter(MessageProducer producer, TextMessage msg) throws JMSException {
        if (currentSelector == null) {
            producer.send(msg);
            logger.info("!!NO FILTER!! Sent Message: {} to broker with latency: {}", msg.getText(), System.nanoTime() - msg.getLongProperty("timeSent"));
            metricsCollector.logNoFilterMsgTimestamp("NO FILTER, msg sent at", System.nanoTime(), "the msg content is", msg.getText(), "the msg was generated at", msg.getLongProperty("timeSent"));
        } else {
            String[] result = currentSelector.substring(1, currentSelector.length() - 1).split("=");
            String property = result[0];
            String constraints = result[1];

            if (msg.propertyExists(property) && msg.getStringProperty(property).contains(constraints)) {// filter unmatched msgs
                producer.send(msg);
                //logger.info("Filtered Msg latency: {} ", System.nanoTime()-msg.getLongProperty("timeSent"));

                logger.info("Actual Sent Message is: {}, the latency is: {}, the message property is: {}, the currentThreshold is: {}", msg.getText(), System.nanoTime() - msg.getLongProperty("timeSent"), msg.getStringProperty(property), currentSelector);
                metricsCollector.logFilterMsgTimestamp("Actural Sent Msg at ",System.nanoTime(),"Msg content is ", msg.getText(), "this msg is generated at ", msg.getLongProperty("timeSent"), "the msg property and current filter are ", msg.getStringProperty(property), currentSelector);
            }
        }
    }
}

