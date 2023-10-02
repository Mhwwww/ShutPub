package org.example.publisher;
import static org.example.cong.Configuration.*;

public class SimpleBaselinePublisher {

    public static void main(String[] args) throws InterruptedException {

        for (int i = 0; i < PRODUCER_NUM; i++) {

            String name = "baseline_"+ i;
            Thread publisherThread = new Thread(() -> {
                PublisherBaseline.baselinePublisher(BROKER_URL, DESTINATION, name);
            });

            publisherThread.start();

//            Thread.sleep(PRODUCER_INTERVAL);
        }
    }





}






