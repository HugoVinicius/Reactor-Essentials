package com.reactoressentials;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Locale;
import java.util.concurrent.Flow;

@Slf4j
public class MonoTest {

    @Test
    public void monoSubcriber() {
        String name = "Hugo Vinicius";
        Mono<String> mono = Mono.just(name).log();

        mono.subscribe();

        log.info("---------------------------");
        StepVerifier.create(mono)
               .expectNext(name)
                .verifyComplete();
    }

    @Test
    public void monoSubcriberConsumer() {
        String name = "Hugo Vinicius";
        Mono<String> mono = Mono.just(name).log();

        mono.subscribe(s -> log.info("Value {}", s));

        log.info("---------------------------");
        StepVerifier.create(mono)
                .expectNext(name)
                .verifyComplete();
    }

    @Test
    public void monoSubcriberError() {
        String name = "Hugo Vinicius";
        Mono<String> mono = Mono.just(name)
                .map( s -> {throw new RuntimeException( "Testing mono with error");});

        mono.subscribe(s -> log.info("Name {}", s), s -> log.error("Somethinbg bad happened"));
        mono.subscribe(s -> log.info("Name {}", s), Throwable::printStackTrace);

        log.info("---------------------------");
        StepVerifier.create(mono)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    public void monoSubcriberConsumerComplete() {
        String name = "Hugo Vinicius";
        Mono<String> mono = Mono.just(name)
                .log()
                .map(String::toUpperCase);

        mono.subscribe(s -> log.info("Value {}", s)
                , Throwable::printStackTrace, () -> log.info("FINISHED!"));

        log.info("---------------------------");
        StepVerifier.create(mono)
                .expectNext(name.toUpperCase())
                .verifyComplete();
    }

    @Test
    public void monoSubcriberConsumerSubscription() {
        String name = "Hugo Vinicius";
        Mono<String> mono = Mono.just(name)
                .log()
                .map(String::toUpperCase);

        mono.subscribe(s -> log.info("Value {}", s)
                , Throwable::printStackTrace
                , () -> log.info("FINISHED!")
                , subscription -> subscription.request(5));

        log.info("---------------------------");


        StepVerifier.create(mono)
                .expectNext(name.toUpperCase())
                .verifyComplete();
    }

    @Test
    public void monoDoOnMethods() {
        String name = "Hugo Vinicius";
        Mono<Object> mono = Mono.just(name)
                .log()
                .map(String::toUpperCase)
                .doOnSubscribe(subscription -> log.info("Subscribed"))
                .doOnRequest(longNumber -> log.info("Request Received, starting doing something..."))
                .doOnNext(s -> log.info("Value is here. Executing doOnNext{}", s))
                .flatMap(s -> Mono.empty())
                .doOnNext(s -> log.info("Value is here. Executing doOnNext{}", s))// will no be executed
                .doOnSuccess(s -> log.info("doOnSucess executed {}", s));

        mono.subscribe(s -> log.info("Value {}", s)
                , Throwable::printStackTrace, () -> log.info("FINISHED!"));

        log.info("---------------------------");

        StepVerifier.create(mono)
                .expectNext(name.toUpperCase())
                .verifyComplete();
    }

    @Test
    public void monoDoOnError() {
        Mono<Object> erro = Mono.error(new IllegalArgumentException("Illegal argument exception erro"))
                .doOnError(e -> MonoTest.log.error("Error message: {}", e.getMessage()))
                .log();

        StepVerifier.create(erro)
                .expectError(IllegalArgumentException.class)
                .verify();
    }
    
    @Test
    public void monoDoOnErrorResume() {
        String name = "Hugo Vinicius";
        Mono<Object> erro = Mono.error(new IllegalArgumentException("Illegal argument exception erro"))
                .onErrorResume(s -> {
                    log.info("Inside On Error Resume");
                    return Mono.just(name);
                })
                .doOnError(e -> MonoTest.log.error("Error message: {}", e.getMessage()))
                .log();

        StepVerifier.create(erro)
                .expectNext(name)
                .verifyComplete();
    }
    
    @Test
    public void monoDoOnErrorReturn() {
        String name = "Hugo Vinicius";
        Mono<Object> erro = Mono.error(new IllegalArgumentException("Illegal argument exception erro"))
                .onErrorReturn("EMPTY")
                .onErrorResume(s -> {
                    log.info("Inside On Error Resume");
                    return Mono.just(name);
                })
                .doOnError(e -> MonoTest.log.error("Error message: {}", e.getMessage()))
                .log();

        StepVerifier.create(erro)
                .expectNext("EMPTY")
                .verifyComplete();
    }
}
