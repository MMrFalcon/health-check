package com.falcon.apps.health.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

@Service
public class HealthCheckService {
    Logger log = LoggerFactory.getLogger(HealthCheckService.class);

    private final WebClient webClient;
    private final MailSender mailSender;

    public HealthCheckService(WebClient.Builder builder, MailSender mailSender) {
        this.webClient = builder.baseUrl("https://td-p.pl").build();
        this.mailSender = mailSender;
    }

//    @Scheduled(fixedRate = 5000)
    @Scheduled(fixedRate = 5000000)
    public Mono<HttpStatusCode> checkHealth() {
        return this.webClient.get().uri("/").accept(MediaType.TEXT_HTML).retrieve().toEntity(String.class)
                .map(ResponseEntity::getStatusCode).log().map(httpStatusCode -> {
                    sendEmail();
                    return httpStatusCode;
                });
    }

    private void sendEmail() {
        log.info("Request for send email");
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo("kubasokol123456@gmail.com");
        mailMessage.setText("Hello there");
        mailMessage.setFrom("noreplay@javahasit.com");
        mailMessage.setSubject("Health Check Service Info");
        mailSender.send(mailMessage);
    }


}
