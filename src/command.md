
[//]: # (configure java env)
sudo apt-get update
sudo apt-get install default-jre
sudo apt-get install sysstat

[//]: # (configure the variables)
ssh-keygen -R 34.159.108.117
scp /Users/minghe/test/target/Broker.jar debian@34.159.108.117:~

PSF_BROKER_URL="tcp://0.0.0.0:61616" java -jar Broker.jar
PSF_BROKER_URL="tcp://0.0.0.0:61616" java -jar BaselineBroker.jar

PSF_BROKER_URL="tcp://34.159.87.243:61616" java -jar Subscriber.jar

PSF_BROKER_URL="tcp://34.159.87.243:61616" PSF_CONSUMER_NUM="3000" PSF_CONSUMER_INTERVAL="500" PSF_FILTER_NUM="1" java -jar SubscriberPSF.jar

PSF_BROKER_URL="tcp://34.159.87.243:61616" PSF_PRODUCER_NUM="100" PSF_MESSAGE_NUM="20000" PSF_MESSAGE_INTERVAL="200" PSF_PUB_WAIT_TIME="10000" java -jar Publisher.jar
PSF_BROKER_URL="tcp://34.159.87.243:61616" PSF_PRODUCER_NUM="100" PSF_MESSAGE_NUM="20000" PSF_MESSAGE_INTERVAL="25" PSF_PUB_WAIT_TIME="10000" java -jar BaselinePublisher.jar


[//]: # (upload jar files and script)

scp /Users/minghe/test/target/Broker.jar debian@34.89.206.160:~
scp /Users/minghe/test/script.sh debian@34.89.206.160:~


scp /Users/minghe/test/target/Publisher.jar debian@34.32.22.1:~
scp /Users/minghe/test/script.sh debian@34.32.22.1:~

scp /Users/minghe/test/target/Subscriber.jar debian@34.163.213.187:~


baselinebroker_cpu_pub_100_msg_2000_mi_5_pi_100_wait_1000.csv
baselinepublisher_cpu_pub_100_msg_2000_mi_5_pi_100_wait_1000.csv

broker_cpu_pub_100_msg_2000_mi_5_pi_100_wait_1000.csv
publisher_cpu_pub_100_msg_2000_mi_5_pi_100_wait_1000.csv