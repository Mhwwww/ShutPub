package org.example.publisher;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
//forward messages to middlerware, with its destinationTopic and messages

public class PublisherWithPSF {
    //      2.1 the mw will receive all messages that this publisher send
    //      2.2 the mw will subscriber to the according filter topic
    //      2.3 when there are subscribed 'threshold', mw will fiter the incoming messages then send results to Destination.
        //public  static final String BROKER_URL = "tcp://localhost:61616";
        //public static final String DESTINATION = "targetTopic";
        private static final Logger logger = LoggerFactory.getLogger(BrokerService.class);

        public static void startProducer(String brokerUrl, String dest, String name) {
            Connection connection = null;
            Session session = null;
            MessageProducer messageProducer = null;

            ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(brokerUrl);
            try{
                connection = activeMQConnectionFactory.createConnection();
                connection.setClientID("filter_"+dest+"_"+name);
                session = connection.createSession(Boolean.FALSE, Session.AUTO_ACKNOWLEDGE);
                Destination destination = session.createTopic(dest);
                //Message msg = session.createTextMessage("TESTING");

                //TODO: 1. create a normal publisher, and 2. creat a publisher-side middleware
                messageProducer = session.createProducer(destination);


//                PsfMW mw = new PsfMW();
//                mw.subToFilter(messageProducer, session, connection);

                PsfMW mw = new PsfMW();
                //mw.subToFilter(messageProducer);

                mw.subToFilter(messageProducer,session,connection);

                for (int i = 1; i < 21; i++) {
                        // Step 10.1 Create a text message
                        //TextMessage message1 = session.createTextMessage("This is a text message " + i + " sent for someID=" + someID);
                        TextMessage message1 = session.createTextMessage(connection.getClientID() + " send: SomeID text message" + i );
                        TextMessage message2 = session.createTextMessage(connection.getClientID() + " send: NoID text message" + i );
                        // Step 10.1 Set a property, message contains head, body, and property
                        //message1.setIntProperty("someID", someID);
                        message1.setStringProperty("messageContent", "someID");
                        message1.setLongProperty("timeSent", System.currentTimeMillis());
                        mw.fiter(messageProducer, message1);

                        message2.setStringProperty("messageContent", "noID");
                        message2.setLongProperty("timeSent", System.currentTimeMillis());
                        // Step 10.2 Send the message
                        //messageProducer.send(message1);

                        // TODO: let the middleware send messages, not the producer itself
                        //System.out.println("will send messages in the middleware");
                        mw.fiter(messageProducer, message2);


                        Thread.sleep(1000);

                        logger.debug("Publisher Plan to Sent message: " + message1.getText()+ message1.getStringProperty("messageContent"));
                        logger.debug("Publisher Plan to Sent message: " + message2.getText()+ message2.getStringProperty("messageContent"));

                }

            }catch (JMSException e){
                e.printStackTrace();
                throw new RuntimeException(e);

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




