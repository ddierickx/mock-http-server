package com.github.kristofa.test.http;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * {@link HttpResponseProvider} that keeps expected request/responses in memory.
 * <p>
 * Simple to use for not too complex request/responses.
 * 
 * @see MockHttpServer
 * @author kristof
 */
public class SimpleHttpResponseProvider implements HttpResponseProvider {

    private HttpRequestImpl latestRequest;

    private final Map<HttpRequest, HttpResponse> expectedRequests = new HashMap<HttpRequest, HttpResponse>();
    private final Set<HttpRequest> receivedRequests = new HashSet<HttpRequest>();

    /**
     * Provide an expected request with content.
     * 
     * @param method HTTP method.
     * @param path Path.
     * @param contentType Content type.
     * @param data Data, content.
     * @return current {@link SimpleHttpResponseProvider}. Allows chaining calls.
     */
    public SimpleHttpResponseProvider expect(final Method method, final String path, final String contentType,
        final String data) {
        latestRequest = new HttpRequestImpl();
        latestRequest.method(method).path(path).content(data.getBytes())
            .httpMessageHeader(HttpMessageHeaderField.CONTENTTYPE.getValue(), contentType);
        return this;
    }

    /**
     * Provide an expected request without content.
     * 
     * @param method HTTP method.
     * @param path Path.
     * @return current {@link SimpleHttpResponseProvider}. Allows chaining calls.
     */
    public SimpleHttpResponseProvider expect(final Method method, final String path) {
        latestRequest = new HttpRequestImpl();
        latestRequest.method(method).path(path);
        return this;
    }

    /**
     * Provide expected response for latest given request.
     * 
     * @param httpCode Http response code.
     * @param contentType Content type.
     * @param data Data.
     * @return current {@link SimpleHttpResponseProvider}. Allows chaining calls.
     */
    public SimpleHttpResponseProvider respondWith(final int httpCode, final String contentType, final String data) {
        final HttpResponseImpl response = new HttpResponseImpl(httpCode, contentType, data == null ? null : data.getBytes());
        expectedRequests.put(latestRequest, response);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpResponse getResponse(final HttpRequest request) {
        receivedRequests.add(request);
        return expectedRequests.get(request);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void verify() throws UnsatisfiedExpectationException {

        if (!expectedRequests.keySet().equals(receivedRequests)) {

            final Collection<HttpRequest> missing = new HashSet<HttpRequest>();

            for (final HttpRequest expectedRequest : expectedRequests.keySet()) {
                if (!receivedRequests.contains(expectedRequest)) {
                    missing.add(expectedRequest);
                }
            }

            final Collection<HttpRequest> unexpected = new HashSet<HttpRequest>();
            for (final HttpRequest receivedRequest : receivedRequests) {
                if (!expectedRequests.keySet().contains(receivedRequest)) {
                    unexpected.add(receivedRequest);
                }
            }

            throw new UnsatisfiedExpectationException(missing, unexpected);

        }

    }

}
