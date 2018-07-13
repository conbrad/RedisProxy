FROM ubuntu

RUN apt-get update && apt-get install -y redis-server

EXPOSE 6379

# Run Redis Server
ENTRYPOINT ["/usr/bin/redis-server"]