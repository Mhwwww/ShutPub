package org.example.publisher;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQDestination;

import javax.jms.*;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static org.example.publisher.PublisherWithPSF.BROKER_URL;
import static org.example.publisher.PublisherWithPSF.DESTINATION;


public class PsfMW {
    /*
    1. subscribe to filter topic
    2. return threshold
    3. send messages to broker
     */
    // Map to store AtomicReferences for each producer's threshold
    //private Map<MessageProducer, AtomicReference<Message>> producerThresholdMap = new HashMap<>();
    private AtomicReference<String> threshold = new AtomicReference<>(null);
    private String currentSelector = null;
    public void subToFilter(MessageProducer producer) {
        // 2. send messages when there is no threshold from filter topic
        // 3. filter messages when there is a threshold
        try {
            ActiveMQDestination pubDestination = ActiveMQDestination.transform(producer.getDestination());

            String fiterDestination = "filter/" + pubDestination.getPhysicalName();
            System.out.println(fiterDestination);

            // create filter subscriber
            ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(BROKER_URL);
            Connection filterConn = activeMQConnectionFactory.createConnection();
            Session filterSession = filterConn.createSession(Boolean.FALSE, Session.AUTO_ACKNOWLEDGE);
            Destination filterTopic = filterSession.createTopic(fiterDestination);
            MessageConsumer filterSubscriber = filterSession.createConsumer(filterTopic);
            //MessageListener listener = new ActiveMQRAMessageListener();
            filterConn.start();

            //TODO: map filterTopic with currentThreshold
            filterSubscriber.setMessageListener(message1 -> {
                // Handle incoming messages here
                if ( message1 != null && message1 instanceof TextMessage) {
                    //System.out.println("we got the threshold");
                    try {
                        String result = ((TextMessage) message1).getText();
                        System.out.println("we got the threshold" +result);

                        if (!Objects.equals(currentSelector, result)){
                            System.out.println("Threshold changed, New threshold is: " + result);
                            currentSelector = result;
                        }else {
                            System.out.println("No changes in threshold");
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
    public void fiter(TextMessage msg){
        // using currentThreshold for the incoming messages




        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(BROKER_URL);
        try {
            Connection publishConn = activeMQConnectionFactory.createConnection();
            Session pubSession = publishConn.createSession(Boolean.FALSE, Session.AUTO_ACKNOWLEDGE);

            MessageProducer mwProducer = pubSession.createProducer(pubSession.createTopic(DESTINATION));


            //TODO: 1. if the threshold does not change, then use the currentThreshold
            if( currentSelector == null){
                mwProducer.send(msg);
                System.out.println("no threshold, and just send messages to broker");

            }else {
                /* currentSelector = {messageContent=some}*/
                String[] result = currentSelector.substring(1, currentSelector.length()-1).split("=");
                String property = result[0];
                String constraints = result[1];

                if( msg.propertyExists(property) && msg.getStringProperty(property).contains(constraints)){
                    mwProducer.send(msg);

                    System.out.println("Actual Sent Message is: " + msg.getText()+ " , the message property is: "+ msg.getStringProperty(property)+ ", it passed the threshold, and the currentThreshold is:"+ currentSelector);

                }

            }

        } catch (JMSException e) {
            throw new RuntimeException(e);
        }

    }

}
