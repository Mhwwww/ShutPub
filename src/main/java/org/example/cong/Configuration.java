package org.example.cong;

public class Configuration {
    //Broker url and Topic for Simple Publisher and Simple Subscriber
    public static final String BROKER_URL = "tcp://localhost:61616";
    public static final String DESTINATION = "targetTopic";

    public static final int CONSUMER_NUM = 2;
    public static final int PRODUCER_NUM = 20;

}
