package org.example.publisher;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
//forward messages to middlerware, with its destinationTopic and messages

public class PublisherWithPSF {
    //      2.1 the mw will receive all messages that this publisher send
    //      2.2 the mw will subscriber to the according filter topic
    //      2.3 when there are subscribed 'threshold', mw will fiter the incoming messages then send results to Destination.

    private static final Logger logger = LoggerFactory.getLogger(BrokerService.class);

    public static void startProducer(String brokerUrl, String dest, String name) {
        Connection connection = null;
        Session session = null;
        MessageProducer messageProducer = null;

        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(brokerUrl);
        try {
            connection = activeMQConnectionFactory.createConnection();
            connection.setClientID("filter_" + dest + "_" + name);
            session = connection.createSession(Boolean.FALSE, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createTopic(dest);
            //Message msg = session.createTextMessage("TESTING");

            //TODO: 1. create a normal publisher, and 2. creat a publisher-side middleware
            messageProducer = session.createProducer(destination);

            PsfMW mw = new PsfMW();
            mw.subToFilter(messageProducer, session, connection);

            for (int i = 1; i < 51; i++) {
                TextMessage message1 = session.createTextMessage(connection.getClientID() + " send: SomeID text message" + i);
                message1.setStringProperty("messageContent", "someID");
                message1.setLongProperty("timeSent", System.currentTimeMillis());
                mw.fiter(messageProducer, message1);

                TextMessage message2 = session.createTextMessage(connection.getClientID() + " send: NoID text message" + i);
                message2.setStringProperty("messageContent", "noID");
                message2.setLongProperty("timeSent", System.currentTimeMillis());
                mw.fiter(messageProducer, message2);

                Thread.sleep(300);
            }

        } catch (JMSException e) {
            e.printStackTrace();
            throw new RuntimeException(e);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (messageProducer != null) {
                    messageProducer.close();
                }
                if (session != null) {
                    session.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (JMSException e) {
                e.printStackTrace();
            }

        }

    }


}




