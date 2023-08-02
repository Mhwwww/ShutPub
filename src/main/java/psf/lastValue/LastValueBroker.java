package psf.lastValue;

import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyAcceptor;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyAcceptorFactory;
import org.apache.activemq.artemis.core.server.ActiveMQServer;
import org.apache.activemq.artemis.core.server.ActiveMQServers;
import org.apache.activemq.artemis.core.settings.impl.AddressSettings;

import java.util.*;


public class LastValueBroker {
    public static final String BROKER_URL = "tcp://localhost:61616";

    public static void main(String[] args) throws Exception {
//        String host = "localhost";
//        int port = 61616;
//
//        Map<String,Object> urlMap = new HashMap<>();
//        urlMap.put("protocols", "TCP");
//        urlMap.put("host", host);
//        urlMap.put("port", port);

        //TransportConfiguration transportConfiguration = new TransportConfiguration(NettyAcceptorFactory.class.getName());
        //System.out.println(NettyAcceptor.class.getName());
        //System.out.println(NettyAcceptorFactory.class.getName());
//
//        AddressSettings addressSettings = new AddressSettings();
//        addressSettings.setDefaultLastValueQueue(true);
//
//
//        Configuration configuration = new ConfigurationImpl()
//        .addAddressSetting("#", addressSettings)
//                .setPersistenceEnabled(false)
//                .setSecurityEnabled(false);
        //configuration.setMessageCounterEnabled(true);
        //configuration.setResolveProtocols(true);



        ActiveMQServer server = ActiveMQServers.newActiveMQServer("file://:/Users/minghe/test/src/main/java/psf/lastValue/broker.xml", null, null);

        server.start();


        System.out.println("Started Embedded Broker");


    }


}

