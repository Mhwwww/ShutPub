package psf.lastValue;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.activemq.ActiveMQConnectionFactory;

import java.util.Date;

public class Publisher {
    public static void main(String[] args) {

        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");

        try {
            // Create a connection and start it
            Connection connection = connectionFactory.createConnection();
            connection.start();

            // Create a session
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // Create a destination (queue)
            Destination destination = session.createTopic("topic?last-value=true");

            // Create a message producer
            MessageProducer producer = session.createProducer(destination);

            // Create a text message
            TextMessage message = session.createTextMessage("Hello, ActiveMQ!");

            // Send the message
            producer.send(message);
            producer.send(message);
            producer.send(message);


            System.out.println("Message sent successfully!");

            // Clean up resources
            producer.close();
            session.close();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }}

}
