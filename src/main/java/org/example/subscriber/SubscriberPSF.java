package org.example.subscriber;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.example.MetricsCollector;

import javax.jms.*;

import static org.example.cong.Configuration.BROKER_URL;
import static org.example.cong.Configuration.DESTINATION;

public class SubscriberPSF {

        private static MetricsCollector metricsCollector = new MetricsCollector();

        public static void subPSF(String broker_url, String dest, String name) throws Exception {
            ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(broker_url);
            Connection connection = null;

            Session session = null;
            MessageConsumer messageConsumer1 = null;
            MessageConsumer messageConsumer2 = null;
            MessageConsumer messageConsumer3 = null;
            MessageConsumer messageConsumer4 = null;

            try {
                connection = activeMQConnectionFactory.createConnection();
                connection.setClientID(name);

                session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

                Destination destination = session.createTopic(dest);

                messageConsumer1 = session.createConsumer(destination, "messageContent='someID'", false);
                messageConsumer2 = session.createConsumer(destination, "messageContent='some'", false);
                messageConsumer3 = session.createConsumer(destination, "messageContent='som'", false);
                messageConsumer4 = session.createConsumer(destination, "messageContent='so'", false);

                connection.start();

                Thread msgConsumerThread1 = createMessageConsumerThread(messageConsumer1);
                Thread msgConsumerThread2 = createMessageConsumerThread(messageConsumer2);
                Thread msgConsumerThread3 = createMessageConsumerThread(messageConsumer3);
                Thread msgConsumerThread4 = createMessageConsumerThread(messageConsumer4);

                msgConsumerThread1.start();
                msgConsumerThread2.start();
                msgConsumerThread3.start();
                msgConsumerThread4.start();

                msgConsumerThread1.join();
                msgConsumerThread2.join();
                msgConsumerThread3.join();
                msgConsumerThread4.join();

            } finally {
                close(messageConsumer1,session, connection);
                close(messageConsumer2,session, connection);
            }
        }

        private static Thread createMessageConsumerThread(MessageConsumer messageConsumer) {
            return new Thread(() -> {
                try {
                    System.out.println("***************Message Consumer will only receive messages where messageContent='" + messageConsumer.getMessageSelector() + "***************");
                    while (true) {
                        TextMessage messageReceived = (TextMessage) messageConsumer.receive();
                        if (messageReceived == null) {
                            break;
                        }

                        long timeRev = System.nanoTime();
                        long timeSent = messageReceived.getLongProperty("timeSent");

                        System.out.println("Received Message with selector: " + messageConsumer.getMessageSelector() + ", at " + System.nanoTime());
                        System.out.println(timeRev - timeSent);

                        metricsCollector.logTimestamp("msgLatency", timeRev-timeSent);

                    }
                } catch (JMSException e) {
                    throw new RuntimeException(e);
                }
            });

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
            }
        }
    }



