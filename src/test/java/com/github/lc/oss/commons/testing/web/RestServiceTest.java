package com.github.lc.oss.commons.testing.web;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.opentest4j.AssertionFailedError;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.github.lc.oss.commons.encoding.Encodings;
import com.github.lc.oss.commons.testing.AbstractMockTest;
import com.github.lc.oss.commons.web.tokens.CsrfTokenManager;

public class RestServiceTest extends AbstractMockTest {

    private RestService service;

    @BeforeEach
    public void init() {
        this.service = new RestService();
    }

    @Test
    public void test_basicAuthHeader_errors() {
        try {
            this.service.basicAuthHeader(null, null);
            Assertions.fail("Expected assertion failure");
        } catch (AssertionFailedError ex) {
            Assertions.assertEquals("expected: not <null>", ex.getMessage());
        }

        try {
            this.service.basicAuthHeader("user", null);
            Assertions.fail("Expected assertion failure");
        } catch (AssertionFailedError ex) {
            Assertions.assertEquals("expected: not <null>", ex.getMessage());
        }

        try {
            this.service.basicAuthHeader("user:name", "password");
            Assertions.fail("Expected assertion failure");
        } catch (AssertionFailedError ex) {
            Assertions.assertEquals("Username cannot contain a ';' ==> expected: <false> but was: <true>", ex.getMessage());
        }
    }

    @Test
    public void test_basicAuthHeader() {
        Map<String, String> result = this.service.basicAuthHeader("user", "pass");
        Assertions.assertNotNull(result);
        String header = result.get(HttpHeaders.AUTHORIZATION);
        Assertions.assertEquals("Basic dXNlcjpwYXNz", header);
        String details = header.replace("Basic ", "");
        Assertions.assertEquals("dXNlcjpwYXNz", details);
        Assertions.assertEquals("user:pass", new String(Encodings.Base64.decode(details), StandardCharsets.UTF_8));
    }

    @Test
    public void test_call_badUrl() {
        try {
            this.service.call(null, "smb://", null, null, null);
            Assertions.fail("Expected excpetion");
        } catch (AssertionFailedError ex) {
            Assertions.assertEquals("Invalid URL", ex.getMessage());
        }
    }

    @Test
    public void test_call() {
        RestTemplate template = Mockito.mock(RestTemplate.class);
        RestService test = new RestService() {
            @Override
            public RestTemplate createRestTemplate() {
                return template;
            }
        };

        Mockito.when(template.exchange(ArgumentMatchers.notNull(), ArgumentMatchers.eq(HttpMethod.GET), ArgumentMatchers.notNull(),
                ArgumentMatchers.eq(Object.class))).thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));

        ResponseEntity<Object> result = test.call(HttpMethod.GET, "http://localhost", null, Object.class, null);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
    }

    @Test
    public void test_call_withHeaders() {
        RestTemplate template = Mockito.mock(RestTemplate.class);
        RestService test = new RestService() {
            @Override
            public RestTemplate createRestTemplate() {
                return template;
            }
        };

        Mockito.when(template.exchange(ArgumentMatchers.notNull(), ArgumentMatchers.eq(HttpMethod.GET), ArgumentMatchers.notNull(),
                ArgumentMatchers.eq(Object.class))).thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));

        Map<String, String> headers = new HashMap<>();
        headers.put("X-Test", "test");

        ResponseEntity<Object> result = test.call(HttpMethod.GET, "http://localhost", headers, Object.class, null);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
    }

    @Test
    public void test_call_csrfMissing_noHeaders() {
        RestTemplate template = Mockito.mock(RestTemplate.class);
        CsrfTokenManager csrfTokenManager = Mockito.mock(CsrfTokenManager.class);
        RestService test = new RestService() {
            @Override
            public RestTemplate createRestTemplate() {
                return template;
            }
        };
        this.setField("csrfTokenManager", csrfTokenManager, test);

        Mockito.when(csrfTokenManager.getHeaderId()).thenReturn("X-CSRF");

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(HttpHeaders.SET_COOKIE, "csrf=token");
        responseHeaders.add(HttpHeaders.SET_COOKIE, csrfTokenManager.getHeaderId() + "=token-hash");

        Mockito.when(template.exchange(ArgumentMatchers.notNull(), //
                ArgumentMatchers.eq(HttpMethod.GET), //
                ArgumentMatchers.argThat(req -> req.getHeaders().containsKey(csrfTokenManager.getHeaderId()) == false), //
                ArgumentMatchers.eq(Object.class))). //
                thenReturn(new ResponseEntity<>(null, responseHeaders, HttpStatus.FORBIDDEN));

        Mockito.when(template.exchange(ArgumentMatchers.notNull(), //
                ArgumentMatchers.eq(HttpMethod.GET), //
                ArgumentMatchers.argThat(req -> req.getHeaders().containsKey(csrfTokenManager.getHeaderId())), //
                ArgumentMatchers.eq(Object.class))). //
                thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));

        ResponseEntity<Object> result = test.call(HttpMethod.GET, "http://localhost", null, Object.class, null);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
    }

    @Test
    public void test_call_csrfMissing_wtihExtraHeaders() {
        RestTemplate template = Mockito.mock(RestTemplate.class);
        CsrfTokenManager csrfTokenManager = Mockito.mock(CsrfTokenManager.class);
        RestService test = new RestService() {
            @Override
            public RestTemplate createRestTemplate() {
                return template;
            }
        };
        this.setField("csrfTokenManager", csrfTokenManager, test);

        Mockito.when(csrfTokenManager.getHeaderId()).thenReturn("X-CSRF");

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(HttpHeaders.SET_COOKIE, "csrf=token");
        responseHeaders.add(HttpHeaders.SET_COOKIE, csrfTokenManager.getHeaderId() + "=token-hash");

        Mockito.when(template.exchange(ArgumentMatchers.notNull(), //
                ArgumentMatchers.eq(HttpMethod.GET), //
                ArgumentMatchers.argThat(req -> req.getHeaders().containsKey(csrfTokenManager.getHeaderId()) == false), //
                ArgumentMatchers.eq(Object.class))). //
                thenReturn(new ResponseEntity<>(null, responseHeaders, HttpStatus.FORBIDDEN));

        Mockito.when(template.exchange(ArgumentMatchers.notNull(), //
                ArgumentMatchers.eq(HttpMethod.GET), //
                ArgumentMatchers.argThat(req -> req.getHeaders().containsKey(csrfTokenManager.getHeaderId())), //
                ArgumentMatchers.eq(Object.class))). //
                thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("X-Data", "data");

        ResponseEntity<Object> result = test.call(HttpMethod.GET, "http://localhost", requestHeaders, Object.class, null);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
    }

    @Test
    public void test_call_csrfMissing_wtihExtraHeadersCookies() {
        RestTemplate template = Mockito.mock(RestTemplate.class);
        CsrfTokenManager csrfTokenManager = Mockito.mock(CsrfTokenManager.class);
        RestService test = new RestService() {
            @Override
            public RestTemplate createRestTemplate() {
                return template;
            }
        };
        this.setField("csrfTokenManager", csrfTokenManager, test);

        Mockito.when(csrfTokenManager.getHeaderId()).thenReturn("X-CSRF");

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(HttpHeaders.SET_COOKIE, "csrf=token");
        responseHeaders.add(HttpHeaders.SET_COOKIE, csrfTokenManager.getHeaderId() + "=token-hash");

        Mockito.when(template.exchange(ArgumentMatchers.notNull(), //
                ArgumentMatchers.eq(HttpMethod.GET), //
                ArgumentMatchers.argThat(req -> req.getHeaders().containsKey(csrfTokenManager.getHeaderId()) == false), //
                ArgumentMatchers.eq(Object.class))). //
                thenReturn(new ResponseEntity<>(null, responseHeaders, HttpStatus.FORBIDDEN));

        Mockito.when(template.exchange(ArgumentMatchers.notNull(), //
                ArgumentMatchers.eq(HttpMethod.GET), //
                ArgumentMatchers.argThat(req -> req.getHeaders().containsKey(csrfTokenManager.getHeaderId())), //
                ArgumentMatchers.eq(Object.class))). //
                thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put(HttpHeaders.COOKIE, "cid=cvalue");

        ResponseEntity<Object> result = test.call(HttpMethod.GET, "http://localhost", requestHeaders, Object.class, null);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
    }

    @Test
    public void test_call_csrfMissing_wtihExtraHeadersCookies_v2() {
        RestTemplate template = Mockito.mock(RestTemplate.class);
        CsrfTokenManager csrfTokenManager = Mockito.mock(CsrfTokenManager.class);
        RestService test = new RestService() {
            @Override
            public RestTemplate createRestTemplate() {
                return template;
            }
        };
        this.setField("csrfTokenManager", csrfTokenManager, test);

        Mockito.when(csrfTokenManager.getHeaderId()).thenReturn("X-CSRF");

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(HttpHeaders.SET_COOKIE, "csrf=token");
        responseHeaders.add(HttpHeaders.SET_COOKIE, csrfTokenManager.getHeaderId() + "=token-hash");

        Mockito.when(template.exchange(ArgumentMatchers.notNull(), //
                ArgumentMatchers.eq(HttpMethod.GET), //
                ArgumentMatchers.argThat(req -> req.getHeaders().containsKey(csrfTokenManager.getHeaderId()) == false), //
                ArgumentMatchers.eq(Object.class))). //
                thenReturn(new ResponseEntity<>(null, responseHeaders, HttpStatus.FORBIDDEN));

        Mockito.when(template.exchange(ArgumentMatchers.notNull(), //
                ArgumentMatchers.eq(HttpMethod.GET), //
                ArgumentMatchers.argThat(req -> req.getHeaders().containsKey(csrfTokenManager.getHeaderId())), //
                ArgumentMatchers.eq(Object.class))). //
                thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put(HttpHeaders.COOKIE, "cid=cvalue; cie=cvalue;");

        ResponseEntity<Object> result = test.call(HttpMethod.GET, "http://localhost", requestHeaders, Object.class, null);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
    }

    @Test
    public void test_call_csrfMissing_missingCookies() {
        RestTemplate template = Mockito.mock(RestTemplate.class);
        CsrfTokenManager csrfTokenManager = Mockito.mock(CsrfTokenManager.class);
        RestService test = new RestService() {
            @Override
            public RestTemplate createRestTemplate() {
                return template;
            }
        };
        this.setField("csrfTokenManager", csrfTokenManager, test);

        Mockito.when(csrfTokenManager.getHeaderId()).thenReturn("X-CSRF");

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(HttpHeaders.SET_COOKIE, "csrf=");
        responseHeaders.add(HttpHeaders.SET_COOKIE, csrfTokenManager.getHeaderId() + "=");

        Mockito.when(template.exchange(ArgumentMatchers.notNull(), //
                ArgumentMatchers.eq(HttpMethod.GET), //
                ArgumentMatchers.argThat(req -> req.getHeaders().containsKey(csrfTokenManager.getHeaderId()) == false), //
                ArgumentMatchers.eq(Object.class))). //
                thenReturn(new ResponseEntity<>(null, responseHeaders, HttpStatus.FORBIDDEN));

        Mockito.when(template.exchange(ArgumentMatchers.notNull(), //
                ArgumentMatchers.eq(HttpMethod.GET), //
                ArgumentMatchers.argThat(req -> req.getHeaders().containsKey(csrfTokenManager.getHeaderId())), //
                ArgumentMatchers.eq(Object.class))). //
                thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));

        ResponseEntity<Object> result = test.call(HttpMethod.GET, "http://localhost", null, Object.class, null);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
    }

    @Test
    public void test_call_forbidden_noCsrf() {
        RestTemplate template = Mockito.mock(RestTemplate.class);
        RestService test = new RestService() {
            @Override
            public RestTemplate createRestTemplate() {
                return template;
            }
        };

        Mockito.when(template.exchange(ArgumentMatchers.notNull(), ArgumentMatchers.eq(HttpMethod.GET), ArgumentMatchers.notNull(),
                ArgumentMatchers.eq(Object.class))).thenReturn(new ResponseEntity<>(HttpStatus.FORBIDDEN));

        ResponseEntity<Object> result = test.call(HttpMethod.GET, "http://localhost", null, Object.class, null);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(HttpStatus.FORBIDDEN, result.getStatusCode());
    }

    @Test
    public void test_call_csrfIsInvalid() {
        RestTemplate template = Mockito.mock(RestTemplate.class);
        CsrfTokenManager csrfTokenManager = Mockito.mock(CsrfTokenManager.class);
        RestService test = new RestService() {
            @Override
            public RestTemplate createRestTemplate() {
                return template;
            }
        };
        this.setField("csrfTokenManager", csrfTokenManager, test);

        Mockito.when(csrfTokenManager.getHeaderId()).thenReturn("X-CSRF");

        Mockito.when(template.exchange(ArgumentMatchers.notNull(), //
                ArgumentMatchers.eq(HttpMethod.GET), //
                ArgumentMatchers.argThat(req -> req.getHeaders().containsKey(csrfTokenManager.getHeaderId()) == true), //
                ArgumentMatchers.eq(Object.class))). //
                thenReturn(new ResponseEntity<>(HttpStatus.FORBIDDEN));

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put(csrfTokenManager.getHeaderId(), "token-hash");

        ResponseEntity<Object> result = test.call(HttpMethod.GET, "http://localhost", requestHeaders, Object.class, null);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(HttpStatus.FORBIDDEN, result.getStatusCode());
    }

    @Test
    public void test_callJson() {
        RestTemplate template = Mockito.mock(RestTemplate.class);
        RestService test = new RestService() {
            @Override
            public RestTemplate createRestTemplate() {
                return template;
            }
        };

        Mockito.when(template.exchange(ArgumentMatchers.notNull(), ArgumentMatchers.eq(HttpMethod.GET), ArgumentMatchers.notNull(),
                ArgumentMatchers.eq(String.class))).thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));

        ResponseEntity<JsonObject> result = test.callJson(HttpMethod.GET, "http://localhost", null, null, HttpStatus.NO_CONTENT);
        Assertions.assertNotNull(result);
        JsonObject body = result.getBody();
        Assertions.assertNull(body);
    }

    @Test
    public void test_callJson_v2() {
        RestTemplate template = Mockito.mock(RestTemplate.class);
        RestService test = new RestService() {
            @Override
            public RestTemplate createRestTemplate() {
                return template;
            }
        };

        Mockito.when(template.exchange(ArgumentMatchers.notNull(), ArgumentMatchers.eq(HttpMethod.GET), ArgumentMatchers.notNull(),
                ArgumentMatchers.eq(String.class))).thenReturn(new ResponseEntity<>("{\"key\" : \"value\"}", HttpStatus.OK));

        Map<String, String> headers = new HashMap<>();
        headers.put("X-Test", "test");

        ResponseEntity<JsonObject> result = test.callJson(HttpMethod.GET, "http://localhost", headers, null, HttpStatus.OK);
        Assertions.assertNotNull(result);
        JsonObject body = result.getBody();
        Assertions.assertNotNull(body);
        Assertions.assertEquals("value", body.getString("key"));
    }

    @Test
    public void test_callJson_v3() {
        RestTemplate template = Mockito.mock(RestTemplate.class);
        RestService test = new RestService() {
            @Override
            public RestTemplate createRestTemplate() {
                return template;
            }
        };

        Mockito.when(template.exchange(ArgumentMatchers.notNull(), ArgumentMatchers.eq(HttpMethod.POST), ArgumentMatchers.notNull(),
                ArgumentMatchers.eq(String.class))).thenReturn(new ResponseEntity<>("{\"error\" : \"1\"}", HttpStatus.UNPROCESSABLE_ENTITY));

        ResponseEntity<JsonObject> result = test.callJson(HttpMethod.POST, "http://localhost", null, new HashMap<String, String>(),
                HttpStatus.UNPROCESSABLE_ENTITY);
        Assertions.assertNotNull(result);
        JsonObject body = result.getBody();
        Assertions.assertNotNull(body);
        Assertions.assertEquals("1", body.getString("error"));
    }

    @Test
    public void test_callJson_v4() {
        RestTemplate template = Mockito.mock(RestTemplate.class);
        RestService test = new RestService() {
            @Override
            public RestTemplate createRestTemplate() {
                return template;
            }
        };

        Mockito.when(template.exchange(ArgumentMatchers.notNull(), ArgumentMatchers.eq(HttpMethod.GET), ArgumentMatchers.notNull(),
                ArgumentMatchers.eq(String.class))).thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        ResponseEntity<JsonObject> result = test.callJson(HttpMethod.GET, "http://localhost", null, null, HttpStatus.NOT_FOUND);
        Assertions.assertNotNull(result);
        JsonObject body = result.getBody();
        Assertions.assertNull(body);
    }

    @Test
    public void test_callJson_v5() {
        RestTemplate template = Mockito.mock(RestTemplate.class);
        RestService test = new RestService() {
            @Override
            public RestTemplate createRestTemplate() {
                return template;
            }
        };

        Mockito.when(template.exchange(ArgumentMatchers.notNull(), ArgumentMatchers.eq(HttpMethod.GET), ArgumentMatchers.notNull(),
                ArgumentMatchers.eq(String.class))).thenReturn(new ResponseEntity<>(HttpStatus.ACCEPTED));

        ResponseEntity<JsonObject> result = test.callJson(HttpMethod.GET, "http://localhost", null, null, HttpStatus.ACCEPTED);
        Assertions.assertNotNull(result);
        JsonObject body = result.getBody();
        Assertions.assertNull(body);
    }

    @Test
    public void test_callJson_returnedArray() {
        RestTemplate template = Mockito.mock(RestTemplate.class);
        RestService test = new RestService() {
            @Override
            public RestTemplate createRestTemplate() {
                return template;
            }
        };

        Mockito.when(template.exchange(ArgumentMatchers.notNull(), ArgumentMatchers.eq(HttpMethod.POST), ArgumentMatchers.notNull(),
                ArgumentMatchers.eq(String.class))).thenReturn(new ResponseEntity<>("[{\"key\" : \"value\"}]", HttpStatus.OK));

        try {
            test.callJson(HttpMethod.POST, "http://localhost", null, "{}", HttpStatus.OK);
            Assertions.fail("Expected assertion failure");
        } catch (AssertionFailedError ex) {
            Assertions.assertEquals("JSON response is not a JSON object. Body is vulnerable to interception attacks.", ex.getMessage());
        }
    }

    @Test
    public void test_getJson() {
        RestTemplate template = Mockito.mock(RestTemplate.class);
        RestService test = new RestService() {
            @Override
            public RestTemplate createRestTemplate() {
                return template;
            }
        };

        Mockito.when(template.exchange(ArgumentMatchers.notNull(), ArgumentMatchers.eq(HttpMethod.GET), ArgumentMatchers.notNull(),
                ArgumentMatchers.eq(String.class))).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        ResponseEntity<JsonObject> result = test.getJson("http://localhost");
        Assertions.assertNotNull(result);
        JsonObject body = result.getBody();
        Assertions.assertNull(body);
    }

    @Test
    public void test_getJson_withHeaders() {
        RestTemplate template = Mockito.mock(RestTemplate.class);
        RestService test = new RestService() {
            @Override
            public RestTemplate createRestTemplate() {
                return template;
            }
        };

        Mockito.when(template.exchange(ArgumentMatchers.notNull(), ArgumentMatchers.eq(HttpMethod.GET), ArgumentMatchers.notNull(),
                ArgumentMatchers.eq(String.class))).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        ResponseEntity<JsonObject> result = test.getJson("http://localhost", new HashMap<>());
        Assertions.assertNotNull(result);
        JsonObject body = result.getBody();
        Assertions.assertNull(body);
    }

    @Test
    public void test_postJson() {
        RestTemplate template = Mockito.mock(RestTemplate.class);
        RestService test = new RestService() {
            @Override
            public RestTemplate createRestTemplate() {
                return template;
            }
        };

        Mockito.when(template.exchange(ArgumentMatchers.notNull(), ArgumentMatchers.eq(HttpMethod.POST), ArgumentMatchers.notNull(),
                ArgumentMatchers.eq(String.class))).thenReturn(new ResponseEntity<>("{\"key\" : \"value\"}", HttpStatus.OK));

        ResponseEntity<JsonObject> result = test.postJson("http://localhost", "{}");
        Assertions.assertNotNull(result);
        JsonObject body = result.getBody();
        Assertions.assertNotNull(body);
        Assertions.assertEquals("value", body.getString("key"));
    }

    @Test
    public void test_postJson_withHeaders() {
        RestTemplate template = Mockito.mock(RestTemplate.class);
        RestService test = new RestService() {
            @Override
            public RestTemplate createRestTemplate() {
                return template;
            }
        };

        Mockito.when(template.exchange(ArgumentMatchers.notNull(), ArgumentMatchers.eq(HttpMethod.POST), ArgumentMatchers.notNull(),
                ArgumentMatchers.eq(String.class))).thenReturn(new ResponseEntity<>("{\"key\" : \"value\"}", HttpStatus.OK));

        ResponseEntity<JsonObject> result = test.postJson("http://localhost", "{}", new HashMap<>());
        Assertions.assertNotNull(result);
        JsonObject body = result.getBody();
        Assertions.assertNotNull(body);
        Assertions.assertEquals("value", body.getString("key"));
    }

    @Test
    public void test_putJson() {
        RestTemplate template = Mockito.mock(RestTemplate.class);
        RestService test = new RestService() {
            @Override
            public RestTemplate createRestTemplate() {
                return template;
            }
        };

        Mockito.when(template.exchange(ArgumentMatchers.notNull(), ArgumentMatchers.eq(HttpMethod.PUT), ArgumentMatchers.notNull(),
                ArgumentMatchers.eq(String.class))).thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));

        ResponseEntity<JsonObject> result = test.putJson("http://localhost", "{}");
        Assertions.assertNotNull(result);
        JsonObject body = result.getBody();
        Assertions.assertNull(body);
    }

    @Test
    public void test_putJson_withHeaders() {
        RestTemplate template = Mockito.mock(RestTemplate.class);
        RestService test = new RestService() {
            @Override
            public RestTemplate createRestTemplate() {
                return template;
            }
        };

        Mockito.when(template.exchange(ArgumentMatchers.notNull(), ArgumentMatchers.eq(HttpMethod.PUT), ArgumentMatchers.notNull(),
                ArgumentMatchers.eq(String.class))).thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));

        ResponseEntity<JsonObject> result = test.putJson("http://localhost", "{}", new HashMap<>());
        Assertions.assertNotNull(result);
        JsonObject body = result.getBody();
        Assertions.assertNull(body);
    }

    @Test
    public void test_deleteJson() {
        RestTemplate template = Mockito.mock(RestTemplate.class);
        RestService test = new RestService() {
            @Override
            public RestTemplate createRestTemplate() {
                return template;
            }
        };

        Mockito.when(template.exchange(ArgumentMatchers.notNull(), ArgumentMatchers.eq(HttpMethod.DELETE), ArgumentMatchers.notNull(),
                ArgumentMatchers.eq(String.class))).thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));

        ResponseEntity<JsonObject> result = test.deleteJson("http://localhost");
        Assertions.assertNotNull(result);
        JsonObject body = result.getBody();
        Assertions.assertNull(body);
    }

    @Test
    public void test_createRestTemplate() {
        RestService test = new RestService() {
            @Override
            public TestRestTemplateErrorHandler getErrorHandler() {
                return new TestRestTemplateErrorHandler();
            }
        };

        Assertions.assertNotNull(test.getErrorHandler());
        Assertions.assertNotNull(test.createRequestFactory());

        RestTemplate result = test.createRestTemplate();
        Assertions.assertNotNull(result);
    }

    @Test
    public void test_createRestTemplate_nulls() {
        RestService test = new RestService() {
            @Override
            public ClientHttpRequestFactory createRequestFactory() {
                return null;
            }
        };

        Assertions.assertNull(test.getErrorHandler());
        Assertions.assertNull(test.createRequestFactory());

        RestTemplate result = test.createRestTemplate();
        Assertions.assertNotNull(result);
    }

    @Test
    public void test_getTimeout() {
        Assertions.assertEquals(RestService.DEFAULT_TIMEOUT, this.service.getTimeout());
    }

    @Test
    public void test_toJson() {
        Map<String, String> map = new HashMap<>();
        map.put("key", "value");

        String result = this.service.toJson(map);
        Assertions.assertEquals("{\"key\":\"value\"}", result);
    }

    @Test
    public void test_toJson_error() {
        Object o = new Object() {
            @SuppressWarnings("unused")
            public String boom() throws Exception {
                throw new Exception("boom!");
            }
        };

        try {
            this.service.toJson(o);
            Assertions.fail("Expected assertion failure");
        } catch (AssertionFailedError ex) {
            Assertions.assertEquals("Unable to convert object to JSON", ex.getMessage());
        }
    }

    @Test
    public void test_fromJson() {
        JsonObject result = this.service.fromJson("{\"key\":\"value\"}");
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("value", result.get("key"));
    }

    @Test
    public void test_fromJson_error() {
        try {
            this.service.fromJson("asd");
            Assertions.fail("Expected assertion failure");
        } catch (AssertionFailedError ex) {
            Assertions.assertEquals("Unable to parse JSON", ex.getMessage());
        }
    }
}
