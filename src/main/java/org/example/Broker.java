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

            System.out.println("Destination Map for the Broker <ActiveMQDestination, Destination> "+broker.getBroker().getDestinationMap());


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



        } catch (Exception e) {
            throw new RuntimeException(e);
        }
}
}


