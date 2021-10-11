package org.example;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.function.BiConsumer;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        final Flux<String> typings = Flux.create(emitter -> {
            try {
                emitter.next("j");
                Thread.sleep(1000);
                emitter.next("ja");
                Thread.sleep(500);
                emitter.next("jav");
                Thread.sleep(50);
                emitter.next("java");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });


        final WebClient webClient = WebClient.create("http://localhost:8080/api/search");
        // TODO: "j", "ja", "jav", "java"

        typings
                .sampleTimeout(s -> Mono.empty().delaySubscription(Duration.ofMillis(100)))
                .switchMap(
                        o -> webClient
                                .get()
                                .uri(builder -> builder.queryParam("text", o).build())
                                .retrieve()
                                .bodyToMono(String[].class)
                )

                .subscribe(
                        data -> System.out.println(Arrays.asList(data)),
                        error -> System.out.println(error.getMessage()),
                        () -> System.out.println("complete")
                );

        typings.blockLast();
    }
}
