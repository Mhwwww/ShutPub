package org.example;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.core.filter.Filter;
import org.apache.activemq.artemis.core.filter.impl.FilterImpl;
import org.apache.activemq.artemis.logs.BundleFactory;
import org.apache.activemq.artemis.selector.filter.BooleanExpression;
import org.apache.activemq.artemis.selector.filter.FilterException;
import org.apache.activemq.artemis.selector.filter.UnaryExpression;
import org.apache.activemq.artemis.selector.impl.SelectorParser;
import org.apache.artemis.client.cdi.logger.ActiveMQCDILogger;
import org.apache.artemis.client.cdi.logger.ActiveMQCDILogger_impl;
import org.slf4j.Logger;
import org.slf4j.event.EventRecodingLogger;

import static org.example.Broker.BROKER_URL;

public class InferenceEngine {
    // TODO: 1. get multiple subscriptions that :
    //          1.1 subscribed to the same Broker;
    //          1.2 with same topic;
    //          1.3 but have different constraints.
    // TODO: 2. Testing
    //          2.1 test case1: constraints1 = someID, and constraints2 = someIDE==> different constraints have intersection.
    //                  return threshold = someID ==> the larger scopt
    //          2.2 test case2: constraints1 = alice, and constraints = bob ==> No intersection
    //                  return threshold = alice || bob
    public String inferenceEngine() {
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(BROKER_URL);
        System.out.println(activeMQConnectionFactory.getClientID());
//TODO: try removePrefix(queueName);==>       final SimpleString unPrefixedQueueName = removePrefix(queueName);

//TODO: get filter expression from consumer(initialized when create the consumer)
        SimpleString filterString = new SimpleString("filterExpression");
        try {
            Filter filter = FilterImpl.createFilter(filterString);
        } catch (ActiveMQException e) {
            throw new RuntimeException(e);
        }


        try {
            BooleanExpression expre1 =  UnaryExpression.createNOT(SelectorParser.parse("x = 1"));
        } catch (FilterException e) {
            throw new RuntimeException(e);
        }


        return "threshold";

    }
}
