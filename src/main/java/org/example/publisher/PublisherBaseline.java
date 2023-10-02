package org.example.publisher;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.api.core.QueueConfiguration;
import org.apache.activemq.broker.BrokerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;

import static org.example.cong.Configuration.*;


public class PublisherBaseline
{
    private static final Logger logger = LoggerFactory.getLogger(BrokerService.class);
    public static void baselinePublisher( String brokerUrl, String dest, String name ) {
        Connection connection = null;
        Session session = null;
        MessageProducer messageProducer = null;

        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(brokerUrl);

        try{
            connection = activeMQConnectionFactory.createConnection();
            connection.setClientID("baseline/" + dest + "/" + name);

            session = connection.createSession(Boolean.FALSE, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createTopic(dest);

            messageProducer = session.createProducer(destination);

            Thread.sleep(PUB_WAIT_TIME);

            for (int i = 1; i < MESSAGE_NUM+1; i++) {
                long startTime = System.currentTimeMillis();
                sendTextMsg(session, connection, messageProducer, "someID",i);
                sendTextMsg(session, connection, messageProducer, "noID",i);
                sendTextMsg(session, connection, messageProducer, "randomID",i);
                sendTextMsg(session, connection, messageProducer, "abcdefg",i);
                sendTextMsg(session, connection, messageProducer, "noID",i);
                sendTextMsg(session, connection, messageProducer, "randomID",i);
                sendTextMsg(session, connection, messageProducer, "abcdefg",i);
                sendTextMsg(session, connection, messageProducer, "noID",i);
                sendTextMsg(session, connection, messageProducer, "randomID",i);
                sendTextMsg(session, connection, messageProducer, "abcdefg",i);

                long finishTime = System.currentTimeMillis();

                long sendingTime = finishTime - startTime;

                logger.error("Sending Duration: {}", sendingTime);
                long sleepTime = MESSAGE_INTERVAL;
                if (sendingTime > MESSAGE_INTERVAL){
                    logger.error("We want to set MESSAGE INTERVAL to be: {}, but the sending time {} is **LONGER** than the INTERVAL", MESSAGE_INTERVAL, sendingTime);
                    sleepTime = sendingTime;
                }else {
                    //logger.info("Message Interval Could Be Applied");
                }

                Thread.sleep(sleepTime);
            }

//            for(int i=1; i<MESSAGE_NUM+1; i++){
//                long startTime = System.currentTimeMillis();
//
//                String property = "someID";
//                // using the same impl as PSF publisher will cause reasonable latency for every for loop.
//                TextMessage message = session.createTextMessage(connection.getClientID() + " send: "+ property +" message " + i);
//                message.setStringProperty("messageContent", property);
//                message.setLongProperty("timeSent", System.currentTimeMillis());
//
//                messageProducer.send(message);
//
//                long finishTime = System.currentTimeMillis();
//
//                long sendingTime = finishTime - startTime;
//
//                logger.error("Sending Duration: {}", sendingTime);
//                long sleepTime = MESSAGE_INTERVAL;
//                if (sendingTime > MESSAGE_INTERVAL){
//                    logger.error("We want to set MESSAGE INTERVAL to be: {}, but the sending time {} is **LONGER** than the INTERVAL", MESSAGE_INTERVAL, sendingTime);
//                    sleepTime = sendingTime;
//                }else {
//                    logger.info("Message Interval Could Be Applied");
//                }
//
//                Thread.sleep(sleepTime);
//            }

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

    public static void sendTextMsg(Session session, Connection connection, MessageProducer messageProducer, String property, int i) throws JMSException {

        TextMessage message = session.createTextMessage(connection.getClientID() + " send: "+ property +" message " + i);
        message.setStringProperty("messageContent", property);
        message.setLongProperty("timeSent", System.currentTimeMillis());

        messageProducer.send(message);
        logger.info("{} send message with property {}", connection.getClientID(), property);
        //logger.info("{} send message: {} with property {}", connection.getClientID(), message.getText(), property);
        //mw.subToFilter(messageProducer, session, connection, message2);
        //mw.fiter(messageProducer, message);
    }


}

