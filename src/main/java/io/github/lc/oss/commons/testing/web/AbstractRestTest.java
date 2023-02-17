package io.github.lc.oss.commons.testing.web;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public abstract class AbstractRestTest extends AbstractWebTest {
    protected <T> List<T> assertJsonArray(JsonObject object, String id, int length) {
        Assertions.assertNotNull(object, "Object cannot be null");
        List<T> array = object.getArray(id);
        Assertions.assertNotNull(array);
        Assertions.assertEquals(length, array.size());
        return array;
    }

    protected void assertJsonMessage(JsonObject object, String category, String severity, int number) {
        this.assertJsonMessage(object, category, severity, number, null);
    }

    protected void assertJsonMessage(JsonObject object, String category, String severity, int number, String optionalText) {
        Assertions.assertNotNull(object);
        List<JsonObject> messages = object.getArray("messages");
        Assertions.assertNotNull(messages);
        JsonObject match = messages.stream().filter(m -> category.equals(m.getString("category")) && //
                severity.equals(m.getString("severity")) && //
                number == m.getInt("number") && //
                (optionalText == null || optionalText.equals(m.getString("text")))). //
                findAny(). //
                orElse(null);
        Assertions.assertNotNull(match, String.format("%s.%s.%d not found", category, severity, number));
    }

    @SuppressWarnings("unchecked")
    protected <T> T assertJsonNotNull(JsonObject object, String id) {
        Assertions.assertNotNull(object, "Object cannot be null");
        Object value = object.get(id);
        Assertions.assertNotNull(value);
        return (T) value;
    }

    protected void assertJsonNull(JsonObject object, String id) {
        Assertions.assertNotNull(object, "Object cannot be null");
        Assertions.assertNull(object.get(id));
    }

    protected <T> T assertJsonOptional(JsonObject actual, String field, Object expected) {
        if (expected == null) {
            this.assertJsonNull(actual, field);
            return null;
        } else {
            return this.assertJsonProperty(actual, field, expected);
        }
    }

    protected <T> T assertJsonProperty(JsonObject object, String id, Object expected) {
        Assertions.assertNotNull(object, "Object cannot be null");
        Assertions.assertNotNull(expected, "expected cannot be null");
        T property = object.getProperty(id);
        if (expected instanceof Enum<?>) {
            Assertions.assertEquals(((Enum<?>) expected).name(), property);
        } else {
            Assertions.assertEquals(expected, property);
        }
        return property;
    }

    protected JsonObject assertJsonObject(JsonObject parent, String id) {
        Assertions.assertNotNull(parent, "Parent cannot be null");
        JsonObject child = parent.getChild(id);
        Assertions.assertNotNull(child);
        return child;
    }

    protected Map<String, String> basicAuthHeader(String username, String password) {
        return this.getRestService().basicAuthHeader(username, password);
    }

    protected <T> ResponseEntity<T> call(HttpMethod method, String url, Map<String, String> headers, Class<T> responseType, Object body) {
        return this.getRestService().call(method, this.getUrl(url), headers, responseType, body);
    }

    protected ResponseEntity<JsonObject> callJson(HttpMethod method, String url, Map<String, String> headers, Object body, HttpStatus expectedStatus) {
        return this.getRestService().callJson(method, this.getUrl(url), headers, body, expectedStatus);
    }

    protected JsonObject fromJson(String json) {
        return this.getRestService().fromJson(json);
    }

    protected ResponseEntity<JsonObject> getJson(String url) {
        return this.getRestService().getJson(this.getUrl(url));
    }

    protected ResponseEntity<JsonObject> getJson(String url, HttpStatus expectedStatus) {
        return this.getRestService().getJson(this.getUrl(url), expectedStatus);
    }

    protected ResponseEntity<JsonObject> getJson(String url, Map<String, String> headers) {
        return this.getRestService().getJson(this.getUrl(url), headers);
    }

    protected ResponseEntity<JsonObject> getJson(String url, Map<String, String> headers, HttpStatus expectedStatus) {
        return this.getRestService().getJson(this.getUrl(url), headers, expectedStatus);
    }

    protected ResponseEntity<JsonObject> postJson(String url, Object body) {
        return this.getRestService().postJson(this.getUrl(url), body);
    }

    protected ResponseEntity<JsonObject> postJson(String url, Object body, Map<String, String> headers) {
        return this.getRestService().postJson(this.getUrl(url), body, headers);
    }

    protected ResponseEntity<JsonObject> postJson(String url, Object body, Map<String, String> headers, HttpStatus expectedStatus) {
        return this.getRestService().postJson(this.getUrl(url), body, headers, expectedStatus);
    }

    protected ResponseEntity<JsonObject> putJson(String url, Object body) {
        return this.getRestService().putJson(this.getUrl(url), body);
    }

    protected ResponseEntity<JsonObject> putJson(String url, Object body, Map<String, String> headers) {
        return this.getRestService().putJson(this.getUrl(url), body, headers);
    }

    protected ResponseEntity<JsonObject> putJson(String url, Object body, Map<String, String> headers, HttpStatus expectedStatus) {
        return this.getRestService().putJson(this.getUrl(url), body, headers, expectedStatus);
    }

    public ResponseEntity<JsonObject> deleteJson(String url) {
        return this.getRestService().deleteJson(this.getUrl(url));
    }

    public ResponseEntity<JsonObject> deleteJson(String url, Map<String, String> headers) {
        return this.getRestService().deleteJson(this.getUrl(url), headers);
    }

    public ResponseEntity<JsonObject> deleteJson(String url, Map<String, String> headers, HttpStatus expectedStatus) {
        return this.getRestService().deleteJson(this.getUrl(url), headers, expectedStatus);
    }

    protected String toJson(Object object) {
        return this.getRestService().toJson(object);
    }
}
