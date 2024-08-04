package com.falcon.apps.health.dto;

import org.springframework.http.HttpStatusCode;

public record QueryResponse(String url, HttpStatusCode statusCode) {
}
