
[//]: # (configure java env for google cloud instance)
sudo apt-get update
sudo apt-get install default-jre
sudo apt-get install sysstat

[//]: # (ssh key error when connect to google cloud)
ssh-keygen -R 34.141.109.109

[//]: # (copy files to google cloud)
[//]: # (broker)
scp /Users/minghe/test/target/Broker.jar debian@35.246.196.53:~

[//]: # (publisher)
scp /Users/minghe/test/target/PSFPublisherNetworkExp.jar debian@34.141.109.109:~
scp /Users/minghe/test/target/BaselinePublisherNetworkExp.jar debian@34.141.109.109:~

[//]: # (subscriber)
scp /Users/minghe/test/target/SubscriberNetworkExp.jar debian@34.89.143.73:~


[//]: # (configure the variables)
PSF_BROKER_URL="tcp://35.246.196.53:61616" java -jar Broker.jar
PSF_BROKER_URL="tcp://0.0.0.0:61616" java -jar BaselineBroker.jar

PSF_BROKER_URL="tcp://35.246.196.53:61616" java -jar Subscriber.jar

PSF_BROKER_URL="tcp://34.159.87.243:61616" PSF_CONSUMER_NUM="3000" PSF_CONSUMER_INTERVAL="500" PSF_FILTER_NUM="1" java -jar SubscriberPSF.jar

[//]: # (ABCDE)
PSF_BROKER_URL="tcp://35.246.196.53:61616" PSF_PUB_WAIT_TIME="20000" PSF_MESSAGE_INTERVAL="2000"  java -jar PSFPublisherNetworkExp.jar
PSF_BROKER_URL="tcp://35.246.196.53:61616" PSF_PUB_WAIT_TIME="20000" PSF_MESSAGE_INTERVAL="2000"  java -jar BaselinePublisher.jar


[//]: # **(ABCDE)
PSF_BROKER_URL**="tcp://34.89.143.73:61616" PSF_MESSAGE_INTERVAL="2000" PUB_SELECTORS="A,A,A,A,A" java -jar PSFPublisherNetworkExp.jar >>pub.log 2>&1
PSF_BROKER_URL="tcp://34.89.143.73:61616" PSF_MESSAGE_INTERVAL="2000" PUB_SELECTORS="A,A,A,A,A" java -jar BaselinePublisherNetworkExp.jar  >>pub.log 2>&1

PSF_BROKER_URL="tcp://35.246.196.53:61616" PSF_PUB_WAIT_TIME="20000" PSF_MESSAGE_INTERVAL="2000" PUB_SELECTORS="A,A,C,D,E"  java -jar BaselinePublisher.jar

[//]: # (Network experiments)
PSF_BROKER_URL="tcp://0.0.0.0:61616" java -jar Broker.jar >>broker.log 2>&1

PSF_BROKER_URL="tcp://35.246.196.53:61616" PSF_PUB_WAIT_TIME="10000" PSF_MESSAGE_INTERVAL="2000"  java -jar PSFPublisherNetworkExp.jar
PSF_BROKER_URL="tcp://35.246.196.53:61616" PUB_WAIT_TIME="20000" java -jar BaselinePublisherNetworkExp.jar


PSF_BROKER_URL="tcp://34.89.143.73:61616" java -jar SubscriberNetworkExp.jar >>sub.log 2>&1


[//]: # (upload jar files and script)
scp /Users/minghe/test/target/Broker.jar debian@34.89.206.160:~
scp /Users/minghe/test/script.sh debian@34.89.206.160:~


scp /Users/minghe/test/target/Publisher.jar debian@34.32.22.1:~
scp /Users/minghe/test/script.sh debian@34.32.22.1:~

scp /Users/minghe/test/target/Subscriber.jar debian@34.163.213.187:~


baselinebroker_cpu_pub_100_msg_2000_mi_5_pi_100_wait_1000.csv
~~baselinepublisher_cpu_pub_100_msg_2000_mi_5_pi_100_wait_1000.csv~~

broker_cpu_pub_100_msg_2000_mi_5_pi_100_wait_1000.csv
publisher_cpu_pub_100_msg_2000_mi_5_pi_100_wait_1000.csv