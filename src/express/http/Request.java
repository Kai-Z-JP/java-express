package express.http;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import express.ExpressUtils;
import express.expressfilter.ExpressFilter;
import express.http.cookie.Cookie;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.List;

/**
 * @author Simon Reinisch
 * @implNote Core modul of express, don't change anything.
 */
public class Request {

  private final HttpExchange HTTP_EXCHANGE;
  private final URI URI;                              // Request URI
  private final InputStream BODY;                     // Request body
  private final Headers HEADERS;                       // Request Headers
  private final String CONTENT_TYPE;                  // Request content-type
  private final Authorization AUTH;                   // Authorization header parsed

  private final HashMap<String, Object> MIDDLEWARE;   // Middleware Data
  private final HashMap<String, Cookie> COOKIES;      // Request cookies
  private final HashMap<String, String> QUERYS;       // URL Querys
  private final HashMap<String, String> FORM_QUERYS;  // Form Querys (application/x-www-form-urlencoded)

  private HashMap<String, String> params;             // URL Params, would be added in ExpressFilterImpl

  {
    this.MIDDLEWARE = new HashMap<>();
    this.params = new HashMap<>();
  }

  public Request(HttpExchange exchange) {
    this.HTTP_EXCHANGE = exchange;
    this.URI = exchange.getRequestURI();
    this.HEADERS = exchange.getRequestHeaders();
    this.BODY = exchange.getRequestBody();

    // Check if the request contains an body-content
    this.CONTENT_TYPE = HEADERS.get("Content-Type") == null ? "" : HEADERS.get("Content-Type").get(0);

    // Check if the request has an Authorization header
    this.AUTH = HEADERS.get("Authorization") == null ? null : new Authorization(HEADERS.get("Authorization").get(0));

    // Check if the request contains x-www-form-urlencoded form data
    this.FORM_QUERYS = CONTENT_TYPE.equals("application/x-www-form-urlencoded")
        ? RequestUtils.parseRawQuery(ExpressUtils.streamToString(BODY))
        : new HashMap<>();

    // Parse query and cookies, both returns not null if there is nothing
    this.QUERYS = RequestUtils.parseRawQuery(exchange.getRequestURI().getRawQuery());
    this.COOKIES = RequestUtils.parseCookies(exchange.getRequestHeaders());
  }

  /**
   * @return The request body as InputStream
   */
  public InputStream getBody() {
    return BODY;
  }

  /**
   * Pipe the body from this request to an OutputStream.
   *
   * @param os         The OutputStream.
   * @param bufferSize Buffersize, eg. 4096.
   * @throws IOException If an IO-Error occurs.
   */
  public void pipe(OutputStream os, int bufferSize) throws IOException {
    byte[] buffer = new byte[bufferSize];
    int n;
    while ((n = BODY.read(buffer)) != -1)
      os.write(buffer, 0, n);
    os.close();
  }

  /**
   * Get an request cookie by name.
   *
   * @param name The cookie name.
   * @return The cookie, null if there is no cookie with this name.
   */
  public Cookie getCookie(String name) {
    return COOKIES.get(name);
  }

  /**
   * Return all cookies from this request.
   *
   * @return All cookies.
   */
  public HashMap<String, Cookie> getCookies() {
    return COOKIES;
  }

  /**
   * Add an the content from an middleware
   *
   * @param middleware     The middleware
   * @param middlewareData The data from the middleware
   */
  public void addMiddlewareContent(ExpressFilter middleware, Object middlewareData) {
    MIDDLEWARE.put(middleware.getName(), middlewareData);
  }

  /**
   * Get the data from a specific middleware by name (Also the reason
   * why the interface ExpressFilter implements a getName())
   *
   * @param name The middleware name
   * @return The middleware object
   */
  public Object getMiddlewareContent(String name) {
    return MIDDLEWARE.get(name);
  }

  /**
   * @return The request user-agent.
   */
  public String getUserAgent() {
    return HEADERS.get("User-agent").get(0);
  }

  /**
   * @return The request host.
   */
  public String getHost() {
    return HEADERS.get("Host").get(0);
  }

  /**
   * @return The request content-type.
   */
  public String getContentType() {
    return CONTENT_TYPE;
  }

  /**
   * @return The request path.
   */
  public String getPath() {
    return this.URI.getPath();
  }

  /**
   * @return The entire request uri.
   */
  public URI getURI() {
    return this.URI;
  }

  /**
   * @return The request-method.
   */
  public String getMethod() {
    return HTTP_EXCHANGE.getRequestMethod();
  }

  /**
   * If there is an Authorization header, it was parsed and saved
   * in a Authorization Object.
   *
   * @return The Authorization object or null if there was no Authorization header present.
   */
  public Authorization getAuthorization() {
    return AUTH;
  }

  /**
   * @return True if there was an Authorization header and the Authorization object was successfully created.
   */
  public boolean hasAuthorization() {
    return AUTH != null;
  }

  /**
   * Returns a query from a form which uses the 'application/x-www-form-urlencoded' request header.
   *
   * @param name The name.
   * @return The value, null if there is none.
   */
  public String getFormQuery(String name) {
    return FORM_QUERYS.get(name);
  }

  /**
   * Returns an param from a dynamic url.
   *
   * @param param The param.
   * @return The value, null if there is none.
   */
  public String getParam(String param) {
    return params.get(param);
  }

  /**
   * Returns the value from the url-query.
   *
   * @param name The name.
   * @return The value, null if there is none.
   */
  public String getQuery(String name) {
    return QUERYS.get(name);
  }

  /**
   * Returns all querys from an x-www-form-urlencoded body.
   *
   * @return An entire list of key-values
   */
  public HashMap<String, String> getFormQuerys() {
    return FORM_QUERYS;
  }

  /**
   * Returns all params from the url.
   *
   * @return An entire list of key-values
   */
  public HashMap<String, String> getParams() {
    return params;
  }

  /**
   * Return all url-querys.
   *
   * @return An entire list of key-values
   */
  public HashMap<String, String> getQuerys() {
    return QUERYS;
  }

  /**
   * Set the params.
   *
   * @param params
   */
  public void setParams(HashMap<String, String> params) {
    this.params = params;
  }

  /**
   * Returns an header value.
   *
   * @param header The header name
   * @return An list with values.
   */
  public List<String> getHeader(String header) {
    return HEADERS.get(header);
  }
}
