package org.example.publisher;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.example.MetricsCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;

import static org.example.cong.Configuration.*;
//forward messages to middlerware, with its destinationTopic and messages

public class PublisherWithPSF {
    //      2.1 the mw will receive all messages that this publisher send
    //      2.2 the mw will subscriber to the according filter topic
    //      2.3 when there are subscribed 'threshold', mw will fiter the incoming messages then send results to Destination.

    private static final Logger logger = LoggerFactory.getLogger(BrokerService.class);
    private static MetricsCollector metricsCollector = new MetricsCollector();

    public static void startProducer(String brokerUrl, String dest, String name) {
        Connection connection = null;
        Session session = null;
        MessageProducer messageProducer = null;

        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(brokerUrl);
        try {
            connection = activeMQConnectionFactory.createConnection();
            connection.setClientID("filter/" + dest + "/" + name);

            session = connection.createSession(Boolean.FALSE, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createTopic(dest);
            //TODO: 1. create a normal publisher, and 2. creat a publisher-side middleware
            messageProducer = session.createProducer(destination);

            PsfMW mw = new PsfMW();
            mw.subToFilter(session, connection);
//            mw.subToFilter(messageProducer, session, connection);
            //TODO: make sure that every publisher could receive filter.
            Thread.sleep(PUB_WAIT_TIME);

            for (int i = 1; i < MESSAGE_NUM+1; i++) {
                long startTime = System.currentTimeMillis();
                sendTextMsg(session, connection, mw, messageProducer, "someID",i);
                sendTextMsg(session, connection, mw, messageProducer, "noID",i);
                sendTextMsg(session, connection, mw, messageProducer, "randomID",i);
                sendTextMsg(session, connection, mw, messageProducer, "abcdefg",i);
                sendTextMsg(session, connection, mw, messageProducer, "noID",i);
                sendTextMsg(session, connection, mw, messageProducer, "randomID",i);
                sendTextMsg(session, connection, mw, messageProducer, "abcdefg",i);
                sendTextMsg(session, connection, mw, messageProducer, "noID",i);
                sendTextMsg(session, connection, mw, messageProducer, "randomID",i);
                sendTextMsg(session, connection, mw, messageProducer, "abcdefg",i);
                long finishTime = System.currentTimeMillis();

                long sendingTime = finishTime - startTime;

                logger.error("Sending Duration: {}", sendingTime);
                long sleepTime = MESSAGE_INTERVAL;
                if (sendingTime > MESSAGE_INTERVAL){
                    logger.error("We want to set MESSAGE INTERVAL to be: {}, but the sending time {} is **LONGER** than the INTERVAL", MESSAGE_INTERVAL, sendingTime);
                    sleepTime = sendingTime;
                }else {
                    logger.info("Message Interval Could Be Applied");
                }

                Thread.sleep(sleepTime);
            }

        } catch (JMSException e) {
            e.printStackTrace();
            throw new RuntimeException(e);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (messageProducer != null) {
                    messageProducer.close();
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

    public static void sendTextMsg(Session session, Connection connection, PsfMW mw, MessageProducer messageProducer, String property, int i) throws JMSException {

        TextMessage message = session.createTextMessage(connection.getClientID() + " send: "+ property +" message " + i);
        message.setStringProperty("messageContent", property);
        message.setLongProperty("timeSent", System.currentTimeMillis());
        metricsCollector.logPlanMsg("Plan to sent Msg at ", System.currentTimeMillis(),"Msg Content is ", message.getText());

        //mw.subToFilter(messageProducer, session, connection, message2);
        mw.fiter(messageProducer, message);
    }


}




