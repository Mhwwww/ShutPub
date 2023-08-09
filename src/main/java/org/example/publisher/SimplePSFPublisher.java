package org.example.publisher;

import org.apache.activemq.broker.BrokerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.example.cong.Configuration.*;

public class SimplePSFPublisher {

    private static final Logger logger = LoggerFactory.getLogger(BrokerService.class);

    public static void main(String[] args) throws InterruptedException {

        for (int i = 0; i < PRODUCER_NUM; i++) {

            String name = "psf_"+ i;
            Thread publisherThread = new Thread(() -> {
                PublisherWithPSF psf = new PublisherWithPSF();
                psf.startProducer(BROKER_URL, DESTINATION, name);

            });
            publisherThread.start();

            Thread.sleep(1000);


        }
    }





}






