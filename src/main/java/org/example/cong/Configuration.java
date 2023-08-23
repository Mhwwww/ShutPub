package org.example.cong;

public class Configuration {
    //Broker url and Topic for Simple Publisher and Simple Subscriber
    public static String BROKER_URL = "tcp://localhost:61616";
//    public static final String BROKER_URL = "tcp://broker-container:61616";
    public static String DESTINATION = "targetTopic";

    public  static int BROKER_INTERVAL = 90;
    public static int PRODUCER_NUM = 1000;
    public static int MESSAGE_NUM = 375;

    public  static int MESSAGE_INTERVAL = 300;
    public  static int PRODUCER_INTERVAL = 500;

    public static int CONSUMER_NUM = 400;
    public  static int CONSUMER_INTERVAL = 100;

    static {
        System.out.println("Hello from the Configuration init");

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
    }

}
