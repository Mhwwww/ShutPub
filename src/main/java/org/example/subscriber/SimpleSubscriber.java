package org.example.subscriber;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

import static org.example.cong.Configuration.BROKER_URL;
import static org.example.cong.Configuration.DESTINATION;

public class SimpleSubscriber {

    public static void main( String[] args ) throws Exception {
        Connection connection = null;
        Session session = null;
        MessageConsumer messageConsumer = null;

        //TODO: using initial context for JMS Topic lookup rather than setup connection directly. Why? Differences?
        // 1. we use the ActiveMQConnectionFactory rather than ConnectionFactory in the TopicSelector example
        // 2. create an initial context for looking up JNDI on node 0 (Java Naming and Directory Interface).


        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(BROKER_URL);
        //System.out.println( "Hello World!" );
        try{

            connection = activeMQConnectionFactory.createConnection();
            session = connection.createSession(Boolean.FALSE, Session.AUTO_ACKNOWLEDGE);

            Destination destination = session.createTopic(DESTINATION);

            //messageConsumer = session.createConsumer(destination);
            //destination, property = 'value', noLocal
            MessageConsumer messageConsumer1 = session.createConsumer(destination, "messageContent='someID'", false);

            MessageConsumer messageConsumer2 = session.createConsumer(destination, "messageContent='some'", false);


            connection.start();

            System.out.println("*************************************************************");
            System.out.println("MessageConsumer1 will only receive messages where messageContent='someID'");
            for (;;) {
                TextMessage messageReceivedA = (TextMessage) messageConsumer1.receive();
                long timeRev = System.currentTimeMillis();
                if (messageReceivedA == null) {
                    break;
                }
                long timeSent = messageReceivedA.getLongProperty("timeSent");


                System.out.println("Received Message with selector: "+messageConsumer1.getMessageSelector()+", at "+ System.currentTimeMillis());

                System.out.println( timeRev-timeSent);
            }


            // Step 13. Consume the messages from MessageConsumer2, filtering out someID=2
            System.out.println("*************************************************************");
            System.out.println("MessageConsumer2 will only receive messages where messageContent='some'");
            for (;;) {
                TextMessage messageReceivedB = (TextMessage) messageConsumer2.receive();
                if (messageReceivedB == null) {
                    break;
                }
                messageReceivedB.getLongProperty("timeSent");
                System.out.println("messageConsumer2 received ");
            }

        } finally {
            try{
                if (messageConsumer != null){
                    messageConsumer.close();
                }
                if (session != null){
                    session.close();
                }
                if (connection != null){
                    connection.close();
                }
            }catch (JMSException e){
                e.printStackTrace();
            }

        }









    }

}