package org.example.broker.inferenceEngine;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.core.filter.Filter;
import org.apache.activemq.artemis.core.filter.impl.FilterImpl;
import org.apache.activemq.artemis.selector.filter.BooleanExpression;
import org.apache.activemq.artemis.selector.filter.FilterException;
import org.apache.activemq.artemis.selector.filter.UnaryExpression;
import org.apache.activemq.artemis.selector.impl.SelectorParser;
import org.apache.activemq.broker.region.Topic;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQTopic;
import org.apache.activemq.command.ConsumerInfo;
import org.w3c.dom.Text;

import javax.jms.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.example.broker.Broker.BROKER_URL;

public class InferenceEngine {

    private String threshold;
    private Map<ActiveMQDestination, Map<String, String>> filterMap = new HashMap<>();

    // TODO: 1. get multiple subscriptions that :
    //          1.1 subscribed to the same Broker;
    //          1.2 with same topic;
    //          1.3 but have different constraints.
    // TODO: 2. Testing
    //          2.1 test case1: constraints1 = someID, and constraints2 = someIDE==> different constraints have intersection.
    //                  return threshold = someID ==> the larger scopt
    //          2.2 test case2: constraints1 = alice, and constraints = bob ==> No intersection
    //                  return threshold = alice || bob
    public void inferenceEngine(ActiveMQDestination destination, String selector ) {

        System.out.println("---------------- Got Consumer Selector Input ----------------");
        System.out.println(destination.getPhysicalName());
        System.out.println(selector);

        destination.getPhysicalName();
        //There is no publisher-sub-threshold topic, because the sub-topics do not have selector.
        //generate adequate threshold
        //ToDo: split the selector, if the property is the same, then check if the constaints contains each other.
        // for string matching, we set the operator to be '='

        String[] selectorResult = selector.split("=");
        String property = selectorResult[0];//messageContent
        String constraints = selectorResult[1];//'someID'
        constraints = constraints.substring(1,constraints.length()-1);


        // convert string selector to map, containing property & constraints
        Map<String, String> selectorMap = new HashMap<>();
        selectorMap.put(property, constraints);



        if (filterMap.containsKey(destination)){// there is already selector on this topic
            //compare current threshold with incoming selector
            // current selector
            System.out.println("current selector: "+ filterMap.get(destination));

            if (filterMap.get(destination).containsKey(property)){// there is already constraints for the given property
                System.out.println("current constraints: " + filterMap.get(destination).get(property) );
                if (filterMap.get(destination).get(property).contains(constraints)){//someID contains some
                    filterMap.replace(destination, selectorMap);
                    System.out.println("the updated filter Map is " + filterMap);
                }

            }
        }else {
            filterMap.put(destination, selectorMap);
            System.out.println("**************** ADD new Pair to the Fiter Map ***********" +filterMap.size());
        }


        System.out.println("--------> Going to pass threshold "+ filterMap.get(destination) + " to Threshold Publisher");
        publishThreshold(destination, filterMap.get(destination).toString());

    }

    private void publishThreshold(ActiveMQDestination originalDestination, String selector){
        //TODO: 1. get the filter topic that need to publish the threshold
        String filterTopic = "";
        filterTopic = "filter/" + originalDestination.getPhysicalName();

        //TODO: 2. init a publisher that sends 'threshold' to 'filterTopic'
        Connection connection = null;
        Session session = null;
        MessageProducer messageProducer = null;

        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(BROKER_URL);

        try {
            connection = activeMQConnectionFactory.createConnection();
            session = connection.createSession(Boolean.FALSE, Session.AUTO_ACKNOWLEDGE);

            Destination destination = session.createTopic(filterTopic);
            messageProducer = session.createProducer(destination);


            TextMessage msg = session.createTextMessage(selector);
            messageProducer.send(msg);

            System.out.println("Sent Threshold: " + msg.getText()+ " to Filter Topic: "+ filterTopic);

        } catch (JMSException e) {
            throw new RuntimeException(e);
        }


    }
}
