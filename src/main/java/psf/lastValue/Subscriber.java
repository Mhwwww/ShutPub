package psf.lastValue;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.api.jms.ActiveMQJMSClient;

import javax.jms.*;

public class Subscriber {
    public  static final String BROKER_URL = "tcp://localhost:61616";
    public static final String DESTINATION = "topic";

    public static void main( String[] args ) throws Exception {
        Connection connection = null;
        Session session = null;
        MessageConsumer messageConsumer = null;

        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(BROKER_URL);
        //System.out.println( "Hello World!" );
        try{

            connection = activeMQConnectionFactory.createConnection();
            session = connection.createSession(Boolean.FALSE, Session.AUTO_ACKNOWLEDGE);

            Queue queue = session.createQueue(DESTINATION);
            MessageConsumer messageConsumer1 = session.createConsumer(queue);

            connection.start();

            System.out.println("*************************************************************");
            System.out.println("GET MESSAGE FROM LAST VALUE QUEUE");
            for (;;) {
                TextMessage messageReceivedA = (TextMessage) messageConsumer1.receive();
                if (messageReceivedA == null) {
                    break;
                }
                System.out.println("*****************"+messageReceivedA.getText()+"*****************");
               // System.out.println("messageConsumer1 received ");
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
