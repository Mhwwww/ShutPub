# Publisher-Side Filtering




## Broker
Using ActiveMQ to be the prototype broker.
After successful client connection, 
client information is got by Connection Manager, 
and Subscription filters will be used by inference engine generating the threshold on meta topic.

Run ['BrokerWithNotification1'](./src/main/java/org/example/broker/BrokerWithNotification1.java) to start the broker,
In this file, you can apply your own broker configuration by changing the line 13 to the file path.
The broker embedded initialization is also provide in the comment.


## Subscriber
The Subscriber send subscriptions which contain filters to Broker.
Run ['Subscriber'](./src/main/java/org/example/subscriber/Subscriber.java) to start the subscriber.

## Publisher
A publisher middleware will initialize automatically wiht the publisher initialization.

The middleware will subscribe to the corresponding meta topic, 
and when there is a filter value, the publisher will only send the filter-matching messages to Broker.


Run ['PublisherWithPSF'](./src/main/java/org/example/publisher/PublisherWithPSF.java) to start the publisher.

