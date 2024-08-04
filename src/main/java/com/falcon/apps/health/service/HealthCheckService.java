package com.falcon.apps.health.service;

import com.falcon.apps.health.dto.QueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

@Service
public class HealthCheckService {
    Logger log = LoggerFactory.getLogger(HealthCheckService.class);

    private final MailSender mailSender;
    private final WebClient.Builder webClientBuilder;

    @Value("#{'${websites_for_check}'.split(',')}")
    private List<String> listOfWebsites;

    @Value("${email_to}")
    private String emailTo;

    @Value("${email_subject}")
    private String emailSubject;

    @Value("${email_from}")
    private String emailFrom;

    public HealthCheckService(WebClient.Builder builder, MailSender mailSender) {
        this.mailSender = mailSender;
        webClientBuilder = builder;
    }

    //    @Scheduled(fixedRate = 5000)
    @Scheduled(fixedRate = 5000000)
    public void checkHealth() {
        Flux.fromIterable(listOfWebsites).log()
                .flatMap(websiteForCheck -> {
                    WebClient client = webClientBuilder.baseUrl(websiteForCheck).build();
                    return sendQuery(client, websiteForCheck);
                }).log()
                .subscribe(queryResponse -> {
                    if (!queryResponse.statusCode().is2xxSuccessful()) {
                        sendEmail(queryResponse);
                    }
                });
    }

    private Mono<QueryResponse> sendQuery(WebClient client, String url) {
        return client.get().uri("/").accept(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_HTML).retrieve().toEntity(String.class)
                .map(ResponseEntity::getStatusCode)
                .onErrorResume(error -> {
                    log.error("Error while HTTP query process", error);
                    return Mono.just(HttpStatusCode.valueOf(500));
                }).log()
                .map(httpStatusCode -> new QueryResponse(url, httpStatusCode));
    }

    private void sendEmail(QueryResponse queryResponse) {
        log.info("Request for send email");
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(emailTo);
        mailMessage.setText(queryResponse.url() + ": " + queryResponse.statusCode().value());
        mailMessage.setFrom(emailFrom);
        mailMessage.setSubject(emailSubject);
        mailSender.send(mailMessage);
    }


}
