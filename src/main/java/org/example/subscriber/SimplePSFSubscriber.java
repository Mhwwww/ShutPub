package org.example.subscriber;

import org.apache.activemq.broker.BrokerService;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.text.RandomStringGenerator;
import org.example.publisher.PublisherWithPSF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

import static org.example.cong.Configuration.*;

public class SimplePSFSubscriber {

    public static void main(String[] args) throws InterruptedException {

        for (int i = 0; i < CONSUMER_NUM; i++) {

            String name = "psf_consumer"+ i;
            Thread subscriberThread = new Thread(() -> {

                try {
                    //TODO: random destination

                    RandomStringGenerator generator = new RandomStringGenerator.Builder()
                            .withinRange('0', 'z')
                            .filteredBy(Character::isLetterOrDigit)
                            .build();

                    String destination = DESTINATION + generator.generate(3);
                    System.out.println(destination);

                    SubscriberPSF.subPSF(BROKER_URL, destination, name);

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            subscriberThread.start();

            Thread.sleep(CONSUMER_INTERVAL);

        }
    }
}






