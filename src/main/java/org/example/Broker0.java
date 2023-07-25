package org.example;

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

import javax.jms.*;
import java.util.Collections;

public class Broker0 {
    public static final String LOCAL_BROKER_URL = "tcp://localhost:61616";

    public static void main(String[] args) {
        try {

            BridgeConfiguration brigeConf = new BridgeConfiguration()
                    .setName("sausage-factory")
                    .setQueueName("sausage-factory")
                    .setForwardingAddress("mincing-machine")
                    .setFilterString("name='abc'")
                    .setStaticConnectors(Collections.singletonList("tcp://localhost:61617"));

            // 创建并配置本地 Broker
            Configuration configuration = new ConfigurationImpl()
                    .setPersistenceEnabled(false)
                    .setSecurityEnabled(false)
                    .addAcceptorConfiguration(new TransportConfiguration(NettyAcceptorFactory.class.getName()))
                    .addBridgeConfiguration(brigeConf)
                    .addConnectorConfiguration("remote-connector", new TransportConfiguration(NettyConnectorFactory.class.getName()));

            ActiveMQServer localBroker = ActiveMQServers.newActiveMQServer(configuration);
            localBroker.start();


            System.out.println("Local Broker started at: " + localBroker.getAddressInfo(SimpleString.toSimpleString(LOCAL_BROKER_URL)));

            Thread.sleep(2000);

            while (true){
                Bridge bridge = localBroker.getClusterManager().getBridges().get("sausage-factory");

                if (bridge != null && bridge.isConnected()){
                    System.out.println("Success!!!!!!!!!!!!!!");
                    break;
                }

            }


            // 等待一段时间，确保消息被处理和转发




        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
