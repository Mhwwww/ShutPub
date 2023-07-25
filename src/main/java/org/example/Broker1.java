package org.example;

import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyAcceptorFactory;
import org.apache.activemq.artemis.core.server.ActiveMQServer;
import org.apache.activemq.artemis.core.server.ActiveMQServers;

import java.util.HashMap;

public class Broker1 {
    public static void main(String[] args) throws Exception {
        Configuration configuration = new ConfigurationImpl();

        // Configuration for the acceptor (listening for incoming connections)
        HashMap<String, Object> params = new HashMap<>();
        params.put("host", "localhost");
        params.put("port", 61617); // Change this to the port number where you want to run the remote broker
        configuration.getAcceptorConfigurations().add(new TransportConfiguration(NettyAcceptorFactory.class.getName(), params));

        ActiveMQServer server = ActiveMQServers.newActiveMQServer(configuration);
        server.start();

        System.out.println("Remote Broker started at: tcp://localhost:61617");

        // Keep the remote broker running until the application is terminated
        while (true) {
            Thread.sleep(1000);
        }
    }
}
