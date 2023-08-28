
[//]: # (configure the variables)
PSF_BROKER_URL="tcp://localhost:61615" PSF_DESTINATION="hahaha" java -jar target/Broker.jar

ssh-keygen -R 34.159.108.117
scp /Users/minghe/test/target/Broker.jar debian@34.159.108.117:~

