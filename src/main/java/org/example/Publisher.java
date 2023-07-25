package org.example;

import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;


public class Publisher
{
    public  static final String BROKER_URL = "tcp://localhost:61616";
    public static final String DESTINATION = "myTopic";

    public static void main( String[] args ) {
        Connection connection = null;
        Session session = null;
        MessageProducer messageProducer = null;
        MessageProducer messageProducer1 = null;


        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(BROKER_URL);
        try{
            connection = activeMQConnectionFactory.createConnection();
            session = connection.createSession(Boolean.FALSE, Session.AUTO_ACKNOWLEDGE);
            //Message msg = session.createTextMessage("TESTING");


            Destination destination = session.createTopic(DESTINATION);
            Destination destination1 = session.createTopic("filter/myTopic");

            messageProducer = session.createProducer(destination);
            messageProducer1 = session.createProducer(destination1);

            TextMessage msg = session.createTextMessage("someI");

            for(int i=0; i<10; i++){
                messageProducer.send(msg);
                messageProducer1.send(msg);
                System.out.println("Sent message: " + msg.getText());

                Thread.sleep(1000);
            }




            /*for (int i = 1; i < 10; i++) {
                for (int someID = 1; someID <= 2; someID++) {
                    // Step 10.1 Create a text message
                    TextMessage message1 = session.createTextMessage("This is a text message " + i +
                            " sent for someID=" +
                            someID);

                    // Step 10.1 Set a property
                    message1.setIntProperty("someID", someID);

                    // Step 10.2 Send the message
                    messageProducer.send(message1);

                    System.out.println("Sent message: " + message1.getText());
                }
            }*/


        }catch (JMSException e){
            e.printStackTrace();;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            try{
                if (messageProducer != null){
                    messageProducer.close();
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
