package org.example.broker;

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

import org.example.broker.inferenceEngine.*;


public class Broker {
    public  static final String BROKER_URL = "tcp://localhost:61616";

    private static final Logger LOG = LoggerFactory.getLogger(BrokerService.class);
    private int currentConsumerNum;

    public static void main( String[] args ) throws IOException, MalformedObjectNameException, ReflectionException, AttributeNotFoundException, InstanceNotFoundException, MBeanException {
        BrokerService broker = new BrokerService();
        broker.setPersistent(false);
        int consumerNum=0;
        InferenceEngine inferenceEngine = new InferenceEngine();

        try {
            broker.addConnector(BROKER_URL);
            broker.setAdvisorySupport(false);
            broker.start();

            //TODO: Is there new client connection?
            // yes--> is this client consumer?--> yes--> call inference engine
            // every consumer(except for the "filter" consumer) should have selector, if not, the publisher will need send all messages.


            Thread.sleep(5000);

            Map<ActiveMQDestination, Destination> destMap = broker.getBroker().getDestinationMap();

            System.out.println("Destination Map for the Broker <ActiveMQDestination, Destination> "+broker.getBroker().getDestinationMap());


            for (Map.Entry<ActiveMQDestination, Destination> entry : destMap.entrySet()) {
                ActiveMQDestination key = entry.getKey();
                Destination value = entry.getValue();

                List<Subscription> consumer = value.getConsumers();
                consumerNum += consumer.size();

                if (consumer.size()>0){
                    for (int i = 0; i<consumer.size(); i++ ){
                        ConsumerInfo consumerInfo;
                        consumerInfo = consumer.get(i).getConsumerInfo();
                        //topic--ActiveMQDestination
                        ActiveMQDestination destination = consumerInfo.getDestination();
                        //filter--String
                        String selector =  consumerInfo.getSelector();
                        //consumer id--ConsumerID
                        //consumerInfo.getConsumerId();

                        System.out.println("Consumer Destination: "+ consumerInfo.getDestination() +" ,Consumer Selector: "+consumerInfo.getSelector());

                        //1. get "selector & topic"
                        //2. update threshold for filter topic in "inference engine".

                        if (selector != null){
                            System.out.println("********* This consumer has a selector, send metadata to Inference Engine *********");

                            inferenceEngine.inferenceEngine(destination, selector);

                        }
                    }
                }else {
                    System.out.println("this is a publisher");
                }

                //System.out.println("Key: " + key + ", Value: " + value);
            }



        } catch (Exception e) {
            throw new RuntimeException(e);
        }
}


}


