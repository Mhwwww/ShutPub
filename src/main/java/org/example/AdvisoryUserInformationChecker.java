package org.example;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.advisory.AdvisorySupport;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.ActiveMQTopic;

import javax.jms.*;
import javax.jms.MessageListener;

public class AdvisoryUserInformationChecker {
    public static final String BROKER_URL = "tcp://localhost:61616";

    public static void main(String[] args) {
        Connection connection = null;
        Session session = null;
        MessageConsumer consumer = null;

        try {
            // Create a connection to the ActiveMQ broker
            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(BROKER_URL);
            connection = connectionFactory.createConnection();
            connection.start();

            // Create a session
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // Create the AdvisoryTopic for Connection events
            ActiveMQTopic advisoryTopic = AdvisorySupport.getConnectionAdvisoryTopic();

            // Create a consumer for the AdvisoryTopic
            consumer = session.createConsumer(advisoryTopic);

            // Set a message listener to receive Advisory messages
            consumer.setMessageListener(new MessageListener() {
                public void onMessage(Message message) {
                    if (message instanceof ActiveMQMessage) {
                        ActiveMQMessage activeMQMessage = (ActiveMQMessage) message;
                        String event = null;
                        try {
                            event = activeMQMessage.getStringProperty("EVENT");
                        } catch (JMSException e) {
                            throw new RuntimeException(e);
                        }

                        if ("CONNECTION_ADDED".equals(event)) {
                            String connectionId = null;
                            try {
                                connectionId = activeMQMessage.getStringProperty("CONNECTION_ID");
                                String clientId = activeMQMessage.getStringProperty("CLIENT_ID");
                                String userName = activeMQMessage.getStringProperty("USER_NAME");
                                String remoteAddress = activeMQMessage.getStringProperty("REMOTE_ADDRESS");
                                System.out.println("Connection added - Connection ID: " + connectionId +
                                        ", Client ID: " + clientId +
                                        ", User Name: " + userName +
                                        ", Remote Address: " + remoteAddress);
                            } catch (JMSException e) {
                                throw new RuntimeException(e);
                            }



                        }
                    }
                }
            });

            // Keep the program running to continue receiving messages
            System.out.println("AdvisoryUserInformationChecker is now running. Press Ctrl+C to exit.");
            while (true) {
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // Clean up resources
                if (consumer != null) consumer.close();
                if (session != null) session.close();
                if (connection != null) connection.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }
}
