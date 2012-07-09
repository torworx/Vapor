package evymind.vapor.core.http;

/**
 * An HTTP request.
 *
 * <h3>Accessing Query Parameters and Cookie</h3>
 * <p>
 * Unlike the Servlet API, a query string is constructed and decomposed by
 * {@link QueryStringEncoder} and {@link QueryStringDecoder}.  {@link Cookie}
 * support is also provided separately via {@link CookieEncoder} and
 * {@link CookieDecoder}.
 * @see HttpResponse
 * @see CookieEncoder
 * @see CookieDecoder
 */
public interface HttpRequest extends HttpMessage {

    /**
     * Returns the method of this request.
     */
    HttpMethod getMethod();

    /**
     * Sets the method of this request.
     */
    void setMethod(HttpMethod method);

    /**
     * Returns the URI (or path) of this request.
     */
    String getUri();

    /**
     * Sets the URI (or path) of this request.
     */
    void setUri(String uri);
}
