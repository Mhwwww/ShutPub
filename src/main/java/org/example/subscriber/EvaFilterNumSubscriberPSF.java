package org.example.subscriber;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.example.MetricsCollector;

import javax.jms.*;

public class EvaFilterNumSubscriberPSF {
    private static MetricsCollector metricsCollector = new MetricsCollector();
    public static void subPSF(String broker_url, String dest, String name, String selector) throws JMSException {
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(broker_url);
        Connection connection = null;
        Session session = null;
        MessageConsumer messageConsumer = null;

        try {
            connection = activeMQConnectionFactory.createConnection();
            connection.setClientID(name);
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            connection.start();

            Destination destination = session.createTopic(dest);
            messageConsumer = session.createConsumer(destination, selector, false);

            System.out.println(name + " will only receive messages that match the selector: " + messageConsumer.getMessageSelector() + "***************");

            while (true) {
                TextMessage messageReceived = (TextMessage) messageConsumer.receive();
                if (messageReceived == null) {
                    break;
                }
                long timeRev = System.nanoTime();
                long timeSent = messageReceived.getLongProperty("timeSent");

                System.out.println(name + " Received Message with selector: " + messageConsumer.getMessageSelector() + ", at " + System.nanoTime());
                System.out.println(timeRev - timeSent);

                //metricsCollector.logTimestamp("msgLatency", timeRev - timeSent);

            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            if (messageConsumer != null) {
                messageConsumer.close();
            }
            if (session != null) {
                session.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
    }
}


