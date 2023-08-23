BROKER_IMAGE_NAME = broker-image
SUBSCRIBER_IMAGE_NAME = subscriber-image
PUBLISHER_IMAGE_NAME = publisher-image

.PHONY: all build_broker build_subscriber build_publisher run_broker run_subscriber run_publisher

all: build_broker build_subscriber build_publisher

build_broker:
	docker build -t $(BROKER_IMAGE_NAME) ./src/main/java/org/example/broker

build_subscriber:
	docker build -t $(SUBSCRIBER_IMAGE_NAME) ./src/main/java/org/example/subscriber

build_publisher:
	docker build -t $(PUBLISHER_IMAGE_NAME) ./src/main/java/org/example/publisher

run_broker:
	docker run -d --name broker-container $(BROKER_IMAGE_NAME)

run_subscriber:
	docker run -d --name subscriber-container $(SUBSCRIBER_IMAGE_NAME)

run_publisher:
	docker run -d --name publisher-container $(PUBLISHER_IMAGE_NAME)

