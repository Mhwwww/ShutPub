package org.example;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.selector.filter.*;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static org.example.PublisherWithPSF.BROKER_URL;
import static org.example.PublisherWithPSF.DESTINATION;


public class PsfMW {
    /*
    1. subscribe to filter topic
    2. return threshold
    3. send messages to broker
     */
    // Map to store AtomicReferences for each producer's threshold
    //private Map<MessageProducer, AtomicReference<Message>> producerThresholdMap = new HashMap<>();
    private AtomicReference<String> threshold = new AtomicReference<>(null);
    private String currentThreshold = null;
    public void subToFilter(MessageProducer producer) {
        // 2. send messages when there is no threshold from filter topic
        // 3. filter messages when there is a threshold
        try {
            Destination pubDestination = producer.getDestination();
            //System.out.println(pubDestination.toString().replace("topic://", ""));
            String fiterDestination = "filter/" + pubDestination.toString().replace("topic://", "");
            //.out.println("the filter destination is: " + fiterDestination);

            ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(BROKER_URL);
            Connection filterConn = activeMQConnectionFactory.createConnection();
            Session filterSession = filterConn.createSession(Boolean.FALSE, Session.AUTO_ACKNOWLEDGE);
            Destination filterTopic = filterSession.createTopic(fiterDestination);

            MessageConsumer filterSubscriber = filterSession.createConsumer(filterTopic);
            //MessageListener listener = new ActiveMQRAMessageListener();
            filterConn.start();

            filterSubscriber.setMessageListener(message1 -> {
                // Handle incoming messages here
                if ( message1 != null && message1 instanceof TextMessage) {
                    System.out.println("we got the threshold");
                    try {
                        String result = ((TextMessage) message1).getText();
                        if (!Objects.equals(currentThreshold, result)){
                            System.out.println("Threshold changed. New threshold: " + result);
                            currentThreshold = result;
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
    public void fiter(Message msg){
        // using currentThreshold for the incoming messages
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(BROKER_URL);
        try {
            Connection publishConn = activeMQConnectionFactory.createConnection();
            Session pubSession = publishConn.createSession(Boolean.FALSE, Session.AUTO_ACKNOWLEDGE);

            MessageProducer mwProducer = pubSession.createProducer(pubSession.createTopic(DESTINATION));
            //TODO: 1. if the threshold does not change, then use the currentThreshold
            if( currentThreshold == null){
                mwProducer.send(msg);

                System.out.println("no threshold, and just send messages to broker");
            }else {


                if (msg.toString().contains(currentThreshold)) {
                    //TODO: 2. if msg passed the threshold, then send.
                    mwProducer.send(msg);
                    System.out.println("The sent message is: " + msg.toString()+ ", it passed the threshold, and the currentThreshold is:"+currentThreshold);
                }
            }

        } catch (JMSException e) {
            throw new RuntimeException(e);
        }

    }

}
