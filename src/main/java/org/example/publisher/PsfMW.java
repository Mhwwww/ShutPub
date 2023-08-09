package org.example.publisher;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.command.ActiveMQDestination;
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


    public void subToFilter(MessageProducer producer, Session session, Connection connection) {
        try {
            ActiveMQDestination pubDestination = ActiveMQDestination.transform(producer.getDestination());

            String fiterDestination = "filter/" + pubDestination.getPhysicalName();
            logger.debug(fiterDestination);

            Topic filterTopic = session.createTopic(fiterDestination);
            //do not need to be the durable
            //TopicSubscriber filterSubscriber = session.createDurableSubscriber(filterTopic, fiterDestination);
            MessageConsumer filterSubscriber = session.createConsumer(filterTopic);

            connection.start();

            //map filterTopic with currentThreshold
            filterSubscriber.setMessageListener(message1 -> {
                // Handle incoming messages here
                if (message1 instanceof TextMessage) {
                    //System.out.println("we got the threshold");
                    try {
                        String result = ((TextMessage) message1).getText();
                        //TODO: the time that publisher middleware receives a threshold
                        long gotThresholdTime =System.currentTimeMillis();
                        logger.debug("we got the threshold" + result);
                        logger.info("{}: Got Threshold at: {}, Threshold arriving latency is: {}", connection.getClientID(), gotThresholdTime, gotThresholdTime- message1.getLongProperty("thresholdTimeSent") );

                        //TODO: not sure whether still need this
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

    public void fiter(MessageProducer producer, TextMessage msg) {
        // using currentThreshold for the incoming messages
        try {
            // 1. if the threshold does not change, then use the currentThreshold
            if (currentSelector == null) {
                producer.send(msg);
                logger.info("!!NO FILTER!! Sent Message: {} to broker with latency: {}" ,msg.getText(), System.currentTimeMillis()-msg.getLongProperty("timeSent"));


            } else {
                String[] result = currentSelector.substring(1, currentSelector.length() - 1).split("=");
                String property = result[0];
                String constraints = result[1];

                if (msg.propertyExists(property) && msg.getStringProperty(property).contains(constraints)) {// filter unmatched msgs

                    producer.send(msg);
                    //logger.info("Filtered Msg latency: {} ", System.currentTimeMillis()-msg.getLongProperty("timeSent"));

                    logger.info("Actual Sent Message is: {}, the latency is: {}, the message property is: {}, the currentThreshold is: {}" , msg.getText() , System.currentTimeMillis()-msg.getLongProperty("timeSent"), msg.getStringProperty(property) , currentSelector);
                }

            }

        } catch (JMSException e) {
            throw new RuntimeException(e);
        }

    }

}
