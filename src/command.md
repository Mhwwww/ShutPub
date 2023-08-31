
[//]: # (configure the variables)
sudo apt-get update
sudo apt-get install default-jre



ssh-keygen -R 34.159.108.117
scp /Users/minghe/test/target/Broker.jar debian@34.159.108.117:~




PSF_BROKER_URL="tcp://0.0.0.0:61616" java -jar Broker.jar
PSF_BROKER_URL="tcp://34.107.50.43:61616" java -jar Subscriber.jar 


PSF_BROKER_URL="tcp://34.107.50.43:61616" PSF_PRODUCER_NUM="2000" PSF_MESSAGE_NUM="125"  java -jar Publisher.jar 
