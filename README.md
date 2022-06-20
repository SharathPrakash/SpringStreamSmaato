# SpringStreamSmaato
A Spring Reactive application which interacts with Stream Server like Amazon Kinesis.

This Microservice project exposes 1 endpoint that accepts id and endpoint query params.

#### Dependency
* java 1.8
* Spring Boot 2.7.0
* spring-cloud-starter-aws 2.2.6
* Kinesis 1.12.243

#### Significance
  1. Its a `GET` requests, then emits each request's  into a `reactor.core.publisher.Sinks` publisher
  for later processing and in case endpoint query param is present it fires a post request to it including the current requests count
  , each minute it counts all distinct published ids to AWS Kinesis Data stream.

#### Implementation Bottleneck

  1. The requirement was to process 10k requests per second its a clear indication for me to favor webflux and netty
   over the normal servlet web stack. [reference](https://filia-aleks.medium.com/microservice-performance-battle-spring-mvc-vs-webflux-80d39fd81bf0)
  2. AWS Kinesis it's easy to push data to and very scalable. 

#### How it works    
1. AWS Kinesis
    Assumption that this service runs in a machine with contains `.aws` folder with valid aws access credentials.
    
    Kinesis Stream is made optional you can toggle it on or off based on the configuration can be found in `application.properties`
    it's set to true by default,
    
2. Performance Test

    I used apache benchmark to get some figures the results were as follows
    
    ```
   Hostname:        localhost
   Port:            8080
   
   Path:          /api/smaato/accept?id=ddsa
   Length:        2 bytes
   
   Concurrency Level:      100
   Time taken for tests:   2.034 seconds
   Complete requests:      10000
   Failed requests:        0
   Keep-Alive requests:    0
   Total transferred:      800000 bytes
   HTML transferred:       20000 bytes
   Requests per second:    6002.58 [#/sec] (mean)
   Time per request:       20.126 [ms] (mean)
   Time per request:       0.206 [ms] (mean, across all concurrent requests)
   Transfer rate:          422.31 [Kbytes/sec] received
   
   Connection Times (ms)
                 min  mean[+/-sd] median   max
   Connect:        0    1   8.3      1     352
   Processing:     2   16  40.7      9     361
   Waiting:        2   15  37.7      9     363
   Total:          5   17  40.3     10     365
   
   Percentage of the requests served within a certain time (ms)
     50%     12
     66%     16
     75%     17
     80%     18
     90%     24
     95%     30
     98%    110
     99%    200
    100%    416 (longest request)
   ```
    Results are from mine personal system with config `2,4 GHz 16-Core Intel Core i7`, Also created the 
    other webservice which processes the post requests on the same machine.
    
   
#### Running app with docker
   Simply run `./start.sh` from your terminal make sure that you have `mvn` and `docker` installed
   
#### Running app with maven
   run `mvn clean install`
