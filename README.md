# Publisher-Side Filtering
ShutPub is a publisher-side middleware that moves content-based filtering functionality from the broker to the publisher so that only messages that have a recipient are transmitted. 

## Research
If you use this software in a publication, please cite it as: 

### Text
Minghe Wang, Trever Schirmer, Tobias Pfandzelter, and David Bermbach. 2024. ShutPub: Publisher-side Filtering for Content-based Pub/Sub on the Edge. In Proceedings of the 7th International Workshop on Edge Systems, Analytics and Networking (EdgeSys '24). Association for Computing Machinery, New York, NY, USA, 13–18. https://doi.org/10.1145/3642968.3654815

### BibTeX

```bibtex
@inproceedings{wang2024shutpub,
  title={ShutPub: Publisher-side Filtering for Content-based Pub/Sub on the Edge},
  author={Wang, Minghe and Schirmer, Trever and Pfandzelter, Tobias and Bermbach, David},
  booktitle={Proceedings of the 7th International Workshop on Edge Systems, Analytics and Networking},
  pages={13--18},
  year={2024}
}
```
For a full list of publications, please see [our website](https://www.tu.berlin/en/3s/research/publications).


## Broker
Using ActiveMQ to be the prototype broker.

The connection manager gets connection information, and subscription(filters) will be used by the inference engine to generate thresholds on meta topics.

Run [BrokerPSF](./src/main/java/org/example/broker/BrokerPSF.java) to start the broker,

You can apply your broker configuration by changing content in [Configuration](./src/main/java/org/example/cong/Configuration.java).

It is also possible to configure broker using a XML file, here is a [sample](./src/main/java/org/example/broker/activemq.xml), and the broker initialization code is provided in the comment.

## Subscriber
The Subscriber sends subscriptions that contain filters to the Broker.
Run [SimpleSubscriber](./src/main/java/org/example/subscriber/SimpleSubscriber.java) to start the subscriber.

## Publisher
A publisher middleware will initialize automatically with the publisher initialization.

The middleware will subscribe to the corresponding meta-topic, 
and when there is a filter value, the publisher will only send the filter-matching messages to the Broker.

Run ['SimplePSFPublisher'](./src/main/java/org/example/publisher/PublisherWithPSF.java) to start the publisher.

