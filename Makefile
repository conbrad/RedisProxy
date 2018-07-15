include env_make
NS = conbrad
VERSION ?= latest
REPO = redisproxy

.PHONY: run test stop clean

run:
	docker-compose up proxy redis

test:
	docker-compose up proxy-test

stop:
	docker stop $$(docker ps -aq --filter ancestor=$(NS)/$(REPO))

clean: stop
	docker rm $$(docker ps -aq --filter ancestor=$(NS)/$(REPO)); \
	docker rmi $$(docker images $(NS)/$(REPO)); \

default: test