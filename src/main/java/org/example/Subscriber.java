package org.example;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.camel.component.jms.reply.CorrelationTimeoutMap;
import org.apache.camel.component.jms.reply.MessageSelectorCreator;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class Subscriber {
    public  static final String BROKER_URL = "tcp://localhost:61616";
    public static final String DESTINATION = "myTopic";

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

            Destination destination1 = session.createTopic("filter/myTopic");

            messageConsumer = session.createConsumer(destination);
            //destination, property = 'value', noLocal
            MessageConsumer messageConsumer1 = session.createConsumer(destination1, "someID=1", false);
            MessageConsumer messageConsumer2 = session.createConsumer(destination, "someID=2", false);




            connection.start();

           while (true){
               Message msg = messageConsumer.receive();
               Message msg1 = messageConsumer1.receive();
               Message msg2 = messageConsumer2.receive();
               //System.out.println(msg.getJMSDestination());//topic://myTopic

               if (msg instanceof TextMessage){
                   String message = ((TextMessage)msg).getText();
                   System.out.println("The Received messages: " + message);
               }
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
