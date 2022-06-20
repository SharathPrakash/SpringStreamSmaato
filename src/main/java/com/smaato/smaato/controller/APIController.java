package com.smaato.smaato.controller;


import com.smaato.smaato.model.RequestCountRTO;
import com.smaato.smaato.service.KinesisService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;


@RestController
@RequestMapping(path="/api/smaato")
@Slf4j
@Getter
public class APIController {

    private static final Logger logger = LoggerFactory.getLogger(APIController.class);

    private WebClient client;

    private Sinks.Many sink;
    private final AtomicLong requestsCounter;
    private final KinesisService kinesisService;

    public APIController(KinesisService kinesisService) {
        this.kinesisService = kinesisService;
        initializeIdPublisher();
        requestsCounter = new AtomicLong(0);

        scheduleRequestCounter();
    }

    @GetMapping("/test")
    public String getStatus(){
        return "Working";
    }



    @GetMapping(path = "/accept")
    public Mono<String> accept(@RequestParam String id, @RequestParam(required = false) String endpoint) {
        sink.emitNext(id, (signalType, emitResult) -> emitResult == Sinks.EmitResult.FAIL_NON_SERIALIZED);
        requestsCounter.incrementAndGet();
        return Optional.ofNullable(endpoint)
                .map(uri -> WebClient.builder()
                        .baseUrl(uri)
                        .filter(ExchangeFilterFunction.ofResponseProcessor(res -> {
                                    log.info("Response status {}", res.statusCode());
                                    return Mono.just(res);
                                }
                        ))
                        .build()
                        .post()
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .body(
                                BodyInserters.fromValue(
                                        new RequestCountRTO(requestsCounter.get())
                                )
                        )
                        .retrieve()
                        .bodyToMono(String.class)
                        .onErrorResume(throwable -> Mono.just("fail"))
                ).orElse(Mono.just("ok"));
    }

    private void scheduleRequestCounter() {
        Mono.just(1).repeat()
                .delayElements(Duration.ofMinutes(1))
                .concatMap(ignore -> countDistinctIds())
                .doOnNext(kinesisService::streamData)
                .doOnError(e-> log.error(e.getMessage()))
                .subscribe(count -> {
                    log.info("[{}] distinct ids were processed in the last minute", count);
                    initializeIdPublisher();
                    requestsCounter.set(0);
                });
    }

    public Mono<Long> countDistinctIds() {
        sink.tryEmitComplete();
        return sink.asFlux().distinct().count();
    }

    private void initializeIdPublisher() {
        this.sink = Sinks.many().multicast().onBackpressureBuffer(Integer.MAX_VALUE);
    }
}
