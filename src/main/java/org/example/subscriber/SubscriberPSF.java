package org.example.subscriber;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.example.MetricsCollector;

import javax.jms.*;

import java.util.ArrayList;
import java.util.List;

import static org.example.cong.Configuration.*;

public class SubscriberPSF {
    private static MetricsCollector metricsCollector = new MetricsCollector();
    public static void subPSF(String broker_url, String dest, String name) throws JMSException, InterruptedException {

        List<String> selectors = generateSelectors();
        //System.out.println(selectors);
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(broker_url);
        Connection connection = null;
        Session session = null;

        for (int j = 0; j < FILTER_NUM; j++){
        try {
            connection = activeMQConnectionFactory.createConnection();
            String sub_name = name + "_sub_"+ j;
            connection.setClientID(sub_name);

            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            Destination destination = session.createTopic(dest);

            connection.start();

            String selector = selectors.get(j);
             MessageConsumer messageConsumer = session.createConsumer(destination, selector, false);
             msgThread(messageConsumer, sub_name, session, connection);

            } catch (InterruptedException e) {
            throw new RuntimeException(e);
            }
            Thread.sleep(CONSUMER_INTERVAL);
        }
    }

    private static List<String> generateSelectors() {
        List<String> selectors = new ArrayList<>();
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

        //random string that length is FILTER_NUM
        StringBuilder baseSelector = new StringBuilder();
        for (int j = 0; j < FILTER_NUM; j++) {
            int randomIndex = (int) (Math.random() * characters.length());
            char randomChar = characters.charAt(randomIndex);
            baseSelector.append(randomChar);
        }

        for (int i = FILTER_NUM; i > 0; i--) {
            selectors.add("messageContent='" + baseSelector + "'");
            // decrease the length of filter
            if (baseSelector.length() > 1) {
                baseSelector.setLength(baseSelector.length() - 1);
            }
        }
        return selectors;
    }

    private static Thread createMessageConsumerThread(MessageConsumer messageConsumer, String name, Session session, Connection connection) {
        return new Thread(() -> {
            try {
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

                    metricsCollector.logTimestamp("msgLatency", timeRev - timeSent);
                }
            } catch (JMSException e) {
                e.printStackTrace();

            } finally {
                close(messageConsumer, session, connection);
            }
        });

    }

    private static void msgThread(MessageConsumer messageConsumer, String name, Session session, Connection connection) throws InterruptedException {
        Thread msgConsumerThread = createMessageConsumerThread(messageConsumer, name, session, connection);
        msgConsumerThread.start();
    }

    private static void close(MessageConsumer messageConsumer, Session session, Connection connection) {
        try {
            if (messageConsumer != null) {
                messageConsumer.close();
            }
            if (session != null) {
                session.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (JMSException e) {
            e.printStackTrace();
            throw new RuntimeException(e);

        }
    }
}


