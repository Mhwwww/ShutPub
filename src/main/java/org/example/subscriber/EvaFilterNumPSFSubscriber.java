package org.example.subscriber;

import org.apache.commons.text.RandomStringGenerator;

import java.util.ArrayList;
import java.util.List;

import static org.example.cong.Configuration.*;

public class EvaFilterNumPSFSubscriber {
    static List<String> selectors = new ArrayList<>();
    static List<String> destinations = new ArrayList<>();

    public static void main(String[] args) throws InterruptedException {

        generateSelectors();
        generateDestinations();

        System.out.println(selectors);
            for (int consumerNum = 0; consumerNum < CONSUMER_NUM; consumerNum++) {
                for (int filterNum = 0; filterNum < FILTER_NUM; filterNum++) {

                String name = "psf_" + consumerNum + "_" + filterNum;
                int finalFilterNum = filterNum;
                int finalConsumerNum = consumerNum;
                Thread subscriberThread = new Thread(() -> {
                    try {
                        String selector = selectors.get(finalFilterNum);
                        EvaFilterNumSubscriberPSF.subPSF(BROKER_URL, destinations.get(finalConsumerNum), name, selector);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
                subscriberThread.start();
            }
            Thread.sleep(CONSUMER_INTERVAL);
        }
    }
    private static List<String> generateSelectors() {
        //List<String> selectors = new ArrayList<>();
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

        // Random string with length FILTER_NUM
        StringBuilder baseSelector = new StringBuilder();
        for (int j = 0; j < FILTER_NUM; j++) {
            int randomIndex = (int) (Math.random() * characters.length());
            char randomChar = characters.charAt(randomIndex);
            baseSelector.append(randomChar);
        }

        for (int i = FILTER_NUM; i > 0; i--) {
            selectors.add("messageContent='" + baseSelector + "'");
            // Decrease the length of the filter
            if (baseSelector.length() > 1) {
                baseSelector.setLength(baseSelector.length() - 1);
            }
        }
        return selectors;
    }


    private static List<String> generateDestinations() {
        for (int i= 0; i < CONSUMER_NUM; i++){
            RandomStringGenerator generator = new RandomStringGenerator.Builder()
                    .withinRange('0', 'z')
                    .filteredBy(Character::isLetterOrDigit)
                    .build();

            String destination = DESTINATION + generator.generate(3);
            destinations.add(destination);
        }

        System.out.println(destinations);
        return destinations;
    }
}
