# RedisProxy

## Overview
A proxy server that acts as a read-through cache for redis GET results. It contains both a HTTP and redis TCP protocol interface. It is implemented in Scala but sticks to a mostly OOP paradigm to remain understandable to a wider audience, using functional patterns mostly for iterating over collections, pattern matching and the Option monad to enforce compile-time safety. 

## Setup
Run make test, this will build the project and run the tests
Note the first build will be slow, due to sbt dependency resolution
Make run, to run the http and proxy server
It is assumed that each server instance runs make run, which inherently means each instance runs it’s own {proxy,redis} pair
Configurable options have defaults set and can be read/changed under the environment key in docker-compose.yml
With the *exception* of the proxy server port which must be changed in .sbtopts

## HTTP Service
By default runs on `localhost:9000` and includes a single route: `/:key` where `key` is the key you want to look up in redis. Stack is Play! Framework v2.6.

## Redis TCP Protocol Service
By default runs on `localhost:9002`. It parses `get <key>` requests and uses the same cache instance that the HTTP service uses.

## Key Design Features / Architectural Overview

### Parallelism:
**TDLR;** Leverage Play! Async request handling, design cache around assumed concurrency
  
Though a bonus requirement, it greatly influenced the foundational design of the core data structures to behave in a concurrent setting. 

We leverage Play’s asynchronous request handling out of the box to enable parallel requests. We also exploit the fact that Play comes packaged with Akka Streams so we can build a TCP server with it and use the same thread pool(s) as the web application. This effectively makes the HTTP and TCP server “one scalable unit”.

### Cache:
**TLDR;** Assume concurrency up front, O(1) reads, writes and LRU by combining a hashmap and a linked list. Writes are locked to synchronize update of hashmap and LRU. Reads are not but benign reads are avoided by using an underlying concurrent hashmap.

* All data structures that make up the cache live in package services.util
* Supports lookups and insertions in O(1) time via a hashmap
* Keys are Nodes of a LinkedList, which allows us to splice out nodes and put them at the tail on O(1) time to create an efficient LRU queue
* The backing hashmap is from java.util.concurrent which effectively shards data so that only parts of the map are locked during updates, reducing lock contention

Updating the cache means updating 2 shared resources that must remain in sync; the hashmap and the linked list.
To synchronize updates to both, we use a shared lock for requests that mutate.

We **do not** use a lock for reads since reading a value from the map can be inconsistent with the LinkedList, and the underlying concurrent hashmap guarantees atomic writes which prevents [benign reads](https://bartoszmilewski.com/2014/10/01/benign-data-races-2/)

The design ultimately supports more read throughput which is in line with the goals of the read-through cache


## Time Spent
* Project platform setup (framework/libraries, docker, etc): 2 hours
* Core cache implementation (design, concurrency planning, implementation, tests): 6 hours
* Redis Protocol Server (Learning Akka streams library, protocol parser, integrating with existing project): 6 hours
* Documentation: 2 hours
* Total: 16 hours

## Missing requirements


