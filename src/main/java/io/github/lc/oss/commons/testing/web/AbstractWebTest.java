package io.github.lc.oss.commons.testing.web;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;

import io.github.lc.oss.commons.testing.AbstractTest;

public abstract class AbstractWebTest extends AbstractTest {
    @Autowired
    private RestService restService;

    @Value("#{pathNormalizer.dir('${testing.application.url:}', 'http://localhost:8080/')}")
    private String appUrl;

    protected RestService getRestService() {
        return this.restService;
    }

    protected String getUrl(String relativePath) {
        return this.prefixUrl(this.appUrl, relativePath);
    }

    protected String prefixUrl(String prefix, String url) {
        /* Strict null check, this has special meaning */
        if (url == null || prefix == null) {
            return url;
        }

        if (url.startsWith(prefix)) {
            return url;
        }

        String s = url;
        if (s.startsWith("/")) {
            s = s.substring(1);
        }

        if (prefix.endsWith("/")) {
            s = prefix + s;
        } else {
            s = prefix + "/" + s;
        }
        return s;
    }

    protected void assertHeader(String id, String value, HttpHeaders actual) {
        Assertions.assertNotNull(actual);
        List<String> values = actual.get(id);
        Assertions.assertNotNull(values);
        Assertions.assertEquals(1, values.size());
        Assertions.assertEquals(value.toLowerCase(), values.iterator().next().toLowerCase());
    }
}
