package org.example.broker;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.api.config.ActiveMQDefaultConfiguration;
import org.apache.activemq.artemis.api.core.RoutingType;
import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.api.core.management.ActiveMQServerControl;
import org.apache.activemq.artemis.core.config.BridgeConfiguration;
import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyAcceptorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyConnectorFactory;
import org.apache.activemq.artemis.core.server.ActiveMQServer;
import org.apache.activemq.artemis.core.server.ActiveMQServers;
import org.apache.activemq.artemis.core.server.cluster.Bridge;
import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.Connection;
import org.apache.activemq.command.ActiveMQDestination;
import org.example.broker.connectionManager.ConnectionManager;

import javax.jms.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;

public class Broker0 {
    public static final String BROKER_URL = "tcp://localhost:61616";
    static ConnectionManager connectionManager = new ConnectionManager();

    public static void main(String[] args) {
//        BrokerService broker = new BrokerService();
//        broker.setPersistent(false);

        try {
            String configFilePath = "xbean:file:/Users/minghe/test/src/main/java/org/example/broker/activemq.xml";
            BrokerService broker = BrokerFactory.createBroker(URI.create(configFilePath));

            broker.start();

            Connection[] brokerClients = broker.getBroker().getClients();
            ActiveMQDestination[] clientDest = broker.getBroker().getDestinations();


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
