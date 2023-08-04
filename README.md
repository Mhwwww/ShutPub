# Publisher-Side Filtering

## Broker
Using ActiveMQ to be the prototype broker.

The connection manager gets client information, 
and subscription(filters) will be used by the inference engine to generate thresholds on meta topics.

Run ['BrokerWithNotification1'](./src/main/java/org/example/broker/BrokerWithNotification1.java) to start the broker,
You can apply your broker configuration in this file by changing line 13 to the file path.
The embedded broker initialization is also provided in the comment.

## Subscriber
The Subscriber sends subscriptions that contain filters to Broker.
Run ['Subscriber'](./src/main/java/org/example/subscriber/Subscriber.java) to start the subscriber.

## Publisher
A publisher middleware will initialize automatically with the publisher initialization.

The middleware will subscribe to the corresponding meta-topic, 
and when there is a filter value, the publisher will only send the filter-matching messages to Broker.

Run ['PublisherWithPSF'](./src/main/java/org/example/publisher/PublisherWithPSF.java) to start the publisher.

