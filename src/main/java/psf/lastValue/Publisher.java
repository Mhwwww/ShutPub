package psf.lastValue;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.activemq.ActiveMQConnectionFactory;

import java.util.Date;

public class Publisher {
            public static void main(String[] args) {
                // Set the JNDI initial context factory class
                System.setProperty("java.naming.factory.initial", "org.apache.activemq.jndi.ActiveMQInitialContextFactory");

                try {
                    // Create the initial context
                    InitialContext initialContext = new InitialContext();
                    // Look-up the JMS queue
                    Queue queue = (Queue) initialContext.lookup("dynamicQueues/exampleQueue");

                    // Look-up the JMS connection factory
                    ConnectionFactory cf = (ConnectionFactory) initialContext.lookup("ConnectionFactory");

                    Connection connection = cf.createConnection();
                    Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

                    MessageProducer producer = session.createProducer(queue);
                    TextMessage message = session.createTextMessage("Hello sent at " + new Date());

                    System.out.println("Sending message: " + message.getText());
                    producer.send(message);

                    // Clean up resources
                    producer.close();
                    session.close();
                    connection.close();
                } catch (NamingException e) {
                    throw new RuntimeException(e);
                } catch (JMSException e) {
                    throw new RuntimeException(e);
                }
            }
        }

// Connection factory setup
//        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
//
//        try {
//            // Create a connection and start it
//            Connection connection = connectionFactory.createConnection();
//            connection.start();
//
//            // Create a session
//            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
//
//            // Create a destination (queue)
//            Destination destination = session.createTopic("topic?last-value=true");
//
//            // Create a message producer
//            MessageProducer producer = session.createProducer(destination);
//
//            // Create a text message
//            TextMessage message = session.createTextMessage("Hello, ActiveMQ!");
//
//            // Send the message
//            producer.send(message);
//            producer.send(message);
//            producer.send(message);
//
//
//            System.out.println("Message sent successfully!");
//
//            // Clean up resources
//            producer.close();
//            session.close();
//            connection.close();
//        } catch (JMSException e) {
//            e.printStackTrace();
//        }