package org.example;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.AdvisoryConsumer;
import org.apache.activemq.advisory.AdvisoryBroker;
import org.apache.activemq.advisory.AdvisorySupport;
import org.apache.activemq.broker.*;
import org.apache.activemq.broker.region.Destination;
import org.apache.activemq.broker.region.Subscription;
import org.apache.activemq.command.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Session;
import javax.management.*;
import java.io.IOException;
import java.util.*;


public class Broker {
    public  static final String BROKER_URL = "tcp://localhost:61616";

    private static final Logger LOG = LoggerFactory.getLogger(BrokerService.class);

    public static void main( String[] args ) throws IOException, MalformedObjectNameException, ReflectionException, AttributeNotFoundException, InstanceNotFoundException, MBeanException {
        BrokerService broker = new BrokerService();
        broker.setPersistent(false);

        try {
            broker.addConnector(BROKER_URL);
            broker.setAdvisorySupport(false);
            broker.start();
            Thread.sleep(2000);

            Map<ActiveMQDestination, Destination> destMap = broker.getBroker().getDestinationMap();

            System.out.println("2222222222222222222222"+broker.getBroker().getDestinationMap());


            for (Map.Entry<ActiveMQDestination, Destination> entry : destMap.entrySet()) {
                ActiveMQDestination key = entry.getKey();
                Destination value = entry.getValue();

                List<Subscription> consumer = value.getConsumers();

                if (consumer.size()>0){
                    System.out.println(consumer.get(0).getConsumerInfo());

                }else {
                    System.out.println("this is a publisher");
                }

                //System.out.println("Key: " + key + ", Value: " + value);
            }



            /*
            * modification on 21.07
            * */
            //get the current transport connectors
//            TransportConnector transportConnector =broker.getTransportConnectors().get(0);
//            List<TransportConnection> connections = transportConnector.getConnections();
//
//            Thread.sleep(5000);
//
//            AdvisoryBroker advisoryBroker = new AdvisoryBroker(broker.getBroker());
//            //System.out.println(advisoryBroker.getDestinationMap());
//
//
//
//            Map<ActiveMQDestination, Destination> destMap = advisoryBroker.getDestinationMap();
//            Connection[] consumerIds = advisoryBroker.getClients();
//
//            for (int i = 0; i< consumerIds.length; i++){
//                System.out.println(consumerIds[i]);
//            }
//
//            int producerNum = 0;
//            int consumerNum = 0;
//
//            for (Map.Entry<ActiveMQDestination, Destination> entry : destMap.entrySet()) {
//                ActiveMQDestination key = entry.getKey();
//                Destination value = entry.getValue();
//
//
//                if(key.isTopic() && key.getPhysicalName().contains("ActiveMQ.Advisory.Producer")){
//                    //System.out.println(key);
//                    producerNum++;
//
//                    //System.out.println("there is one Producer, and the current producer number is: "+ producerNum);
//                }
//
//
//                //consumer's subscription != 0
//                String[] result = value.toString().split(", subscriptions=");
//                int currNum = Integer.parseInt(result[1]);
//
//                //it is a client for consumer, but could have multiple subscription
//                if(currNum!=0){
//                    //System.out.println(currNum);//the number of subscription
//                    System.out.println("Key: " + key + ", Value: " + value);
//
//                    //Destination for Consumer
//                    String destinationConsumer = key.getPhysicalName();
//
//                    ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(BROKER_URL);
//
//                    javax.jms.Connection connection = connectionFactory.createConnection();
//
//                    // Create a session
//                    Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
//
//                    ActiveMQTopic advisoryTopic = AdvisorySupport.getConsumerAdvisoryTopic(key);
//                    AdvisoryConsumer advisoryConsumer = (AdvisoryConsumer) session.createConsumer(advisoryTopic);
//
//
//                    System.out.println("Destination for Consumer Clients: "+destinationConsumer);
//                    //TODO: get the metadata of Consumer--> property(filter info)
//                    // using mbeans or AdvisoryConsumer
//                    consumerNum= consumerNum + currNum;//the number of consumer
//                }
//                //System.out.println("Key: " + key + ", Value: " + value);
//                }
//
//            System.out.println("client length: " +advisoryBroker.getClients().length);
//
//            System.out.println("producer number is: "+ producerNum);
//            System.out.println("consumer number is: " + consumerNum);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
}
}


