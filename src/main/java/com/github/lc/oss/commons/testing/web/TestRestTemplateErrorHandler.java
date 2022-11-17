package com.github.lc.oss.commons.testing.web;

import java.io.IOException;

import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

public class TestRestTemplateErrorHandler implements ResponseErrorHandler {
    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        /* The test itself should determine if there is an error, never the framework */
        return false;
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        /* See hasError(), nothing to do here */
    }
}
