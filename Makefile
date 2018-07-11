include env_make
NS = conbrad
VERSION ?= latest
REPO = redisproxy

.PHONY: build run test stop clean

# Only build if image does not exist, otherwise we get stalled by SBT dependencies every time
build:
	@if [ "$$(docker images -q $(NS)/$(REPO) 2> /dev/null)" == "" ]; then docker build -t $(NS)/$(REPO):$(VERSION) .; fi

run: build
	docker run -i $(PORTS) $(ENV) $(NS)/$(REPO) sbt run

test: build
	docker run -i $(NS)/$(REPO) sbt test

stop:
	docker stop $$(docker ps -aq --filter ancestor=$(NS)/$(REPO))

clean: stop
	docker rm $$(docker ps -aq --filter ancestor=$(NS)/$(REPO)); \
	docker rmi $$(docker images $(NS)/$(REPO)); \

default: build