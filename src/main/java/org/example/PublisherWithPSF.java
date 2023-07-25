package org.example;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
//forward messages to middlerware, with its destinationTopic and messages
public class PublisherWithPSF {
    //      2.1 the mw will receive all messages that this publisher send
    //      2.2 the mw will subscriber to the according filter topic
    //      2.3 when there are subscribed 'threshold', mw will fiter the incoming messages then send results to Destination.
        public  static final String BROKER_URL = "tcp://localhost:61616";
        public static final String DESTINATION = "myTopic";

        public static void main( String[] args ) {
            Connection connection = null;
            Session session = null;
            MessageProducer messageProducer = null;

            ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(BROKER_URL);
            try{
                connection = activeMQConnectionFactory.createConnection();
                session = connection.createSession(Boolean.FALSE, Session.AUTO_ACKNOWLEDGE);
                Destination destination = session.createTopic(DESTINATION);
                //Message msg = session.createTextMessage("TESTING");

                //TODO: 1. create a normal publisher, and 2. creat a publisher-side middleware
                messageProducer = session.createProducer(destination);

                PsfMW mw = new PsfMW();
                mw.subToFilter(messageProducer);



                for (int i = 1; i < 10; i++) {
                    for (int someID = 1; someID <= 2; someID++) {
                        // Step 10.1 Create a text message
                        TextMessage message1 = session.createTextMessage("This is a text message " + i +
                                " sent for someID=" +
                                someID);

                        // Step 10.1 Set a property, message contains head, body, and property
                        message1.setIntProperty("someID", someID);

                        // Step 10.2 Send the message
                        //messageProducer.send(message1);
                        // TODO: let the middleware send messages, not the producer itself
                        //System.out.println("will send messages in the middleware");
                        mw.fiter(message1);
                        Thread.sleep(2000);

                        //System.out.println("Sent message: " + message1.getText());
                    }
                }


            }catch (JMSException e){
                e.printStackTrace();
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


