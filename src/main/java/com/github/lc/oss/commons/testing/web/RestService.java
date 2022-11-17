package com.github.lc.oss.commons.testing.web;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.http.client.HttpClient;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.Assertions;
import org.opentest4j.AssertionFailedError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.lc.oss.commons.encoding.Encodings;
import com.github.lc.oss.commons.web.tokens.CsrfTokenManager;

public class RestService {
    private static final StringHttpMessageConverter UTF_8_CONVERTER = new StringHttpMessageConverter(StandardCharsets.UTF_8);
    private static final RedirectStrategy REDIRECT_STRATEGY = new DefaultRedirectStrategy(new String[0]);

    protected static final int DEFAULT_TIMEOUT = 30 * 1000;

    @Autowired(required = false)
    private TestRestTemplateErrorHandler errorHandler;
    @Autowired(required = false)
    private CsrfTokenManager csrfTokenManager;

    public Map<String, String> basicAuthHeader(String username, String password) {
        Assertions.assertNotNull(username);
        Assertions.assertNotNull(password);
        Assertions.assertFalse(username.contains(":"), "Username cannot contain a ';'");

        String value = username + ":" + password;
        value = "Basic " + Encodings.Base64.encode(value.getBytes(StandardCharsets.UTF_8));

        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.AUTHORIZATION, value);
        return headers;
    }

    public <T> ResponseEntity<T> call(HttpMethod method, String url, Map<String, String> headers, Class<T> responseType, Object body) {
        HttpHeaders requestHeaders = new HttpHeaders();
        if (headers != null) {
            headers.forEach((k, v) -> requestHeaders.add(k, v));
        }

        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException ex) {
            throw new AssertionFailedError("Invalid URL");
        }

        ResponseEntity<T> response = this.createRestTemplate().exchange(uri, method, new HttpEntity<>(body, requestHeaders), responseType);
        if (response.getStatusCode() == HttpStatus.FORBIDDEN && //
                this.getCsrfTokenManager() != null && //
                (headers == null || !headers.containsKey(this.getCsrfTokenManager().getHeaderId()))) //
        {
            Map<String, String> extraHeaders = new HashMap<>();
            if (headers != null) {
                extraHeaders.putAll(headers);
            }
            List<String> cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
            String cookieHeader = cookies.stream(). //
                    map(c -> c.split(";")[0]). //
                    collect(Collectors.joining("; "));
            String allCookies = extraHeaders.get(HttpHeaders.COOKIE);
            if (allCookies != null) {
                if (!allCookies.endsWith(";")) {
                    allCookies += ";";
                }
                allCookies += " ";
                allCookies += cookieHeader;
            } else {
                allCookies = cookieHeader;
            }
            extraHeaders.put(HttpHeaders.COOKIE, allCookies);
            String csrfHeader = cookies.stream(). //
                    filter(c -> c.startsWith(this.getCsrfTokenManager().getHeaderId())). //
                    map(c -> c.split(";")[0]). //
                    map(c -> c.replace(this.getCsrfTokenManager().getHeaderId() + "=", "")). //
                    findAny(). //
                    orElse("");
            extraHeaders.put(this.getCsrfTokenManager().getHeaderId(), csrfHeader);
            return this.call(method, url, extraHeaders, responseType, body);
        }
        return response;
    }

    public ResponseEntity<JsonObject> callJson(HttpMethod method, String url, Map<String, String> headers, Object body, HttpStatus expectedStatus) {
        String data = null;
        if (body instanceof String) {
            data = (String) body;
        } else if (body != null) {
            data = this.toJson(body);
        }

        ResponseEntity<String> result = this.call(method, url, headers, String.class, data);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(expectedStatus, result.getStatusCode());

        if (expectedStatus == HttpStatus.NO_CONTENT || expectedStatus == HttpStatus.NOT_FOUND) {
            Assertions.assertNull(result.getBody());
            return new ResponseEntity<>(null, result.getHeaders(), result.getStatusCode());
        }

        if (result.getBody() == null) {
            return new ResponseEntity<>(null, result.getHeaders(), result.getStatusCode());
        }

        if (result.getBody().charAt(0) != '{') {
            throw new AssertionFailedError("JSON response is not a JSON object. Body is vulnerable to interception attacks.");
        }

        JsonObject jsonObject = this.fromJson(result.getBody());
        return new ResponseEntity<>(jsonObject, result.getHeaders(), result.getStatusCode());
    }

    public ClientHttpRequestFactory createRequestFactory() {
        /*
         * Long standing Java bug - PATCH isn't supported by default :(
         *
         * Note: HttpClient is not reusable :(
         */
        HttpClient client = HttpClientBuilder.create(). //
                setRedirectStrategy(RestService.REDIRECT_STRATEGY). //
                build();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(client);
        factory.setConnectTimeout(this.getTimeout());
        factory.setReadTimeout(this.getTimeout());
        return factory;
    }

    public RestTemplate createRestTemplate() {
        RestTemplate rest = new RestTemplate();
        TestRestTemplateErrorHandler errorHandler = this.getErrorHandler();
        if (errorHandler != null) {
            rest.setErrorHandler(errorHandler);
        }
        ClientHttpRequestFactory factory = this.createRequestFactory();
        if (factory != null) {
            rest.setRequestFactory(factory);
        }

        /*
         * Spring 5.2+ "bug" - encoding headers are no longer supplied so JSON strings
         * get ISO_8859_1 instead of UTF-8 - which is a bug since JSON is UTF-8...
         */
        rest.getMessageConverters().add(0, RestService.UTF_8_CONVERTER);
        return rest;
    }

    protected CsrfTokenManager getCsrfTokenManager() {
        return this.csrfTokenManager;
    }

    public JsonObject fromJson(String json) {
        try {
            return new ObjectMapper().readValue(json, JsonObject.class);
        } catch (JsonProcessingException ex) {
            throw new AssertionFailedError("Unable to parse JSON");
        }
    }

    public ResponseEntity<JsonObject> getJson(String url) {
        return this.getJson(url, HttpStatus.OK);
    }

    public ResponseEntity<JsonObject> getJson(String url, HttpStatus expectedStatus) {
        return this.getJson(url, null, expectedStatus);
    }

    public ResponseEntity<JsonObject> getJson(String url, Map<String, String> headers) {
        return this.getJson(url, headers, HttpStatus.OK);
    }

    public ResponseEntity<JsonObject> getJson(String url, Map<String, String> headers, HttpStatus expectedStatus) {
        return this.callJson(HttpMethod.GET, url, headers, null, expectedStatus);
    }

    public ResponseEntity<JsonObject> postJson(String url, Object body) {
        return this.postJson(url, body, null);
    }

    public ResponseEntity<JsonObject> postJson(String url, Object body, Map<String, String> headers) {
        return this.postJson(url, body, headers, HttpStatus.OK);
    }

    public ResponseEntity<JsonObject> postJson(String url, Object body, Map<String, String> headers, HttpStatus expectedStatus) {
        Map<String, String> allHeaders = new HashMap<>();
        allHeaders.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        if (headers != null) {
            allHeaders.putAll(headers);
        }
        return this.callJson(HttpMethod.POST, url, allHeaders, body, expectedStatus);
    }

    public ResponseEntity<JsonObject> putJson(String url, Object body) {
        return this.putJson(url, body, null);
    }

    public ResponseEntity<JsonObject> putJson(String url, Object body, Map<String, String> headers) {
        return this.putJson(url, body, headers, HttpStatus.NO_CONTENT);
    }

    public ResponseEntity<JsonObject> putJson(String url, Object body, Map<String, String> headers, HttpStatus expectedStatus) {
        Map<String, String> allHeaders = new HashMap<>();
        allHeaders.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        if (headers != null) {
            allHeaders.putAll(headers);
        }
        return this.callJson(HttpMethod.PUT, url, allHeaders, body, expectedStatus);
    }

    public ResponseEntity<JsonObject> deleteJson(String url) {
        return this.deleteJson(url, null);
    }

    public ResponseEntity<JsonObject> deleteJson(String url, Map<String, String> headers) {
        return this.deleteJson(url, headers, HttpStatus.NO_CONTENT);
    }

    public ResponseEntity<JsonObject> deleteJson(String url, Map<String, String> headers, HttpStatus expectedStatus) {
        return this.callJson(HttpMethod.DELETE, url, headers, null, expectedStatus);
    }

    public TestRestTemplateErrorHandler getErrorHandler() {
        return this.errorHandler;
    }

    public int getTimeout() {
        return RestService.DEFAULT_TIMEOUT;
    }

    public String toJson(Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException ex) {
            throw new AssertionFailedError("Unable to convert object to JSON");
        }
    }
}
