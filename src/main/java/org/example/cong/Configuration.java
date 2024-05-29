package org.example.cong;

public class Configuration {
    //BrokerBaseline url and Topic for Simple Publisher and Simple Subscriber
    public static String BROKER_URL = "tcp://localhost:61616";
//    public static final String BROKER_URL = "tcp://broker-container:61616";
    public static String DESTINATION = "targetTopic";

    public  static int BROKER_INTERVAL = 100;
    public static int PRODUCER_NUM = 1000;
    public static int MESSAGE_NUM = 4000;

    public  static int PUB_WAIT_TIME = 20000;
    public  static int MESSAGE_INTERVAL = 2000;
    public  static int PRODUCER_INTERVAL = 0;

    public static int CONSUMER_NUM = 1;
    public  static int CONSUMER_INTERVAL = 1000;
    public static int FILTER_NUM = 1;

    public static String PUB_SELECTORS = "B,C,D,E,F";
    public static String[] selectorArray = PUB_SELECTORS.split(",");


    static {
        System.out.println("Hello from the Configuration init");

        String selectors = System.getenv("PUB_SELECTORS");
        if (selectors != null) {
            PUB_SELECTORS = selectors;
            selectorArray = PUB_SELECTORS.split(",");
            System.out.println("Using non-default PUB_SELECTORS:" + selectors);
        }

        String destination = System.getenv("PSF_DESTINATION");
        if (destination != null) {
            DESTINATION = destination;
            System.out.println("Using non-default DESTINATION:" + destination);
        }

        String broker_url = System.getenv("PSF_BROKER_URL");
        if (broker_url != null) {
            BROKER_URL = broker_url;
            System.out.println("Using non-default BROKER_URL:" + broker_url);
        }

        String producer_num = System.getenv("PSF_PRODUCER_NUM");
        if (producer_num != null) {
            PRODUCER_NUM = Integer.parseInt(producer_num);
            System.out.println("Using non-default PRODUCER_NUM:" + producer_num);
        }

        String message_num = System.getenv("PSF_MESSAGE_NUM");
        if (message_num != null) {
            MESSAGE_NUM = Integer.parseInt(message_num);
            System.out.println("Using non-default MESSAGE_NUM:" + message_num);
        }

        String message_interval = System.getenv("PSF_MESSAGE_INTERVAL");
        if (message_interval != null) {
            MESSAGE_INTERVAL = Integer.parseInt(message_interval);
            System.out.println("Using non-default MESSAGE_INTERVAL:" + message_interval);
        }

        String pub_wait_time = System.getenv("PSF_PUB_WAIT_TIME");
        if (pub_wait_time != null) {
            PUB_WAIT_TIME = Integer.parseInt(pub_wait_time);
            System.out.println("Using non-default PUBLISHER WAIT TIME:" + pub_wait_time);
        }

        String producer_interval = System.getenv("PSF_PRODUCER_INTERVAL");
        if (producer_interval != null) {
            PRODUCER_INTERVAL = Integer.parseInt(producer_interval);
            System.out.println("Using non-default PRODUCER_INTERVAL:" + producer_interval);
        }

        String consumer_num = System.getenv("PSF_CONSUMER_NUM");
        if (consumer_num != null) {
            CONSUMER_NUM = Integer.parseInt(consumer_num);
            System.out.println("Using non-default CONSUMER_NUM:" + consumer_num);
        }

        String consumer_interval = System.getenv("PSF_CONSUMER_INTERVAL");
        if (consumer_interval != null) {
            CONSUMER_INTERVAL = Integer.parseInt(consumer_interval);
            System.out.println("Using non-default CONSUMER_INTERVAL:" + consumer_interval);
        }

        String filter_number = System.getenv("PSF_FILTER_NUM");
        if (filter_number != null) {
            FILTER_NUM = Integer.parseInt(filter_number);
            System.out.println("Using non-default FILTER_NUM:" + filter_number);
        }

    }

}
