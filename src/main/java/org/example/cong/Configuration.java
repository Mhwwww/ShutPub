package org.example.cong;

public class Configuration {
    //Broker url and Topic for Simple Publisher and Simple Subscriber
    public static final String BROKER_URL = "tcp://localhost:61616";
    public static final String DESTINATION = "targetTopic";


    public static final int CONSUMER_NUM = 450;
    public static final int PRODUCER_NUM = 2000;
    public static final int MESSAGE_NUM = 250;


    // millisecond
    public  static final int MESSAGE_INTERVAL = 1000;
    public  static final int PRODUCER_INTERVAL = 700;
    public  static final int BROKER_INTERVAL = 100;
    public  static final int CONSUMER_INTERVAL = 200;

}
