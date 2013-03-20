package org.jsoup;

import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Map;
import java.util.Collection;
import java.io.IOException;

/**
 * A Connection provides a convenient interface to fetch content from the web, and parse them into Documents.
 * <p>
 * To get a new Connection, use {@link Jsoup#connect(String)}. Connections contain {@link Connection.Request}
 * and {@link Connection.Response} objects. The request objects are reusable as prototype requests.
 * <p>
 * Request configuration can be made using either the shortcut methods in Connection (e.g. {@link #userAgent(String)}),
 * or by methods in the Connection.Request object directly. All request configuration must be made before the request
 * is executed.
 */
public interface Connection {

    /**
     * GET and POST http methods.
     */
    enum Method {
        GET, POST
    }

    /**
     * Set the request URL to fetch. The protocol must be HTTP or HTTPS.
     * @param url URL to connect to
     * @return this Connection, for chaining
     */
    Connection url(URL url);

    /**
     * Set the request URL to fetch. The protocol must be HTTP or HTTPS.
     * @param url URL to connect to
     * @return this Connection, for chaining
     */
    Connection url(String url);

    /**
     * Set the request user-agent header.
     * @param userAgent user-agent to use
     * @return this Connection, for chaining
     */
    Connection userAgent(String userAgent);

    /**
     * Set the request timeouts (connect and read). If a timeout occurs, an IOException will be thrown. The default
     * timeout is 3 seconds (3000 millis). A timeout of zero is treated as an infinite timeout.
     * @param millis number of milliseconds (thousandths of a second) before timing out connects or reads.
     * @return this Connection, for chaining
     */
    Connection timeout(int millis);

    /**
     * Set the maximum bytes to read from the (uncompressed) connection into the body, before the connection is closed,
     * and the input truncated. The default maximum is 1MB. A max size of zero is treated as an infinite amount (bounded
     * only by your patience and the memory available on your machine).
     * @param bytes number of bytes to read from the input before truncating
     * @return this Connection, for chaining
     */
    Connection maxBodySize(int bytes);

    /**
     * Set the request referrer (aka "referer") header.
     * @param referrer referrer to use
     * @return this Connection, for chaining
     */
    Connection referrer(String referrer);

    /**
     * Configures the connection to (not) follow server redirects. By default this is <b>true</b>.
     * @param followRedirects true if server redirects should be followed.
     * @return this Connection, for chaining
     */
    Connection followRedirects(boolean followRedirects);

    /**
     * Set the request method to use, GET or POST. Default is GET.
     * @param method HTTP request method
     * @return this Connection, for chaining
     */
    Connection method(Connection.Method method);

    /**
     * Configures the connection to not throw exceptions when a HTTP error occurs. (4xx - 5xx, e.g. 404 or 500). By
     * default this is <b>false</b>; an IOException is thrown if an error is encountered. If set to <b>true</b>, the
     * response is populated with the error body, and the status message will reflect the error.
     * @param ignoreHttpErrors - false (default) if HTTP errors should be ignored.
     * @return this Connection, for chaining
     */
    Connection ignoreHttpErrors(boolean ignoreHttpErrors);

    /**
     * Ignore the document's Content-Type when parsing the response. By default this is <b>false</b>, an unrecognised
     * content-type will cause an IOException to be thrown. (This is to prevent producing garbage by attempting to parse
     * a JPEG binary image, for example.) Set to true to force a parse attempt regardless of content type.
     * @param ignoreContentType set to true if you would like the content type ignored on parsing the response into a
     * Document.
     * @return this Connection, for chaining
     */
    Connection ignoreContentType(boolean ignoreContentType);

    /**
     * Add a request data parameter. Request parameters are sent in the request query string for GETs, and in the request
     * body for POSTs. A request may have multiple values of the same name.
     * @param key data key
     * @param value data value
     * @return this Connection, for chaining
     */
    Connection data(String key, String value);

// --Commented out by Inspection START (3/20/13 10:02 AM):
//    /**
//     * Adds all of the supplied data to the request data parameters
//     * @param data map of data parameters
//     * @return this Connection, for chaining
//     */
//    Connection data(Map<String, String> data);
// --Commented out by Inspection STOP (3/20/13 10:02 AM)

    /**
     * Add a number of request data parameters. Multiple parameters may be set at once, e.g.:
     * <code>.data("name", "jsoup", "language", "Java", "language", "English");</code> creates a query string like:
     * <code>?name=jsoup&language=Java&language=English</code>
     * @param keyvals a set of key value pairs.
     * @return this Connection, for chaining
     */
    Connection data(String... keyvals);

// --Commented out by Inspection START (3/20/13 10:02 AM):
//    /**
//     * Set a request header.
//     * @param name header name
//     * @param value header value
//     * @return this Connection, for chaining
//     * @see Connection.Request#headers()
//     */
//    Connection header(String name, String value);
// --Commented out by Inspection STOP (3/20/13 10:02 AM)

    /**
     * Set a cookie to be sent in the request.
     * @param name name of cookie
     * @param value value of cookie
     * @return this Connection, for chaining
     */
    Connection cookie(String name, String value);

    /**
     * Adds each of the supplied cookies to the request.
     * @param cookies map of cookie name -> value pairs
     * @return this Connection, for chaining
     */
    Connection cookies(Map<String, String> cookies);

    /**
     * Provide an alternate parser to use when parsing the response to a Document.
     * @param parser alternate parser
     * @return this Connection, for chaining
     */
    Connection parser(Parser parser);

    /**
     * Execute the request as a GET, and parse the result.
     * @return parsed Document
     * @throws MalformedURLException if the request URL is not a HTTP or HTTPS URL, or is otherwise malformed
     * @throws HttpStatusException if the response is not OK and HTTP response errors are not ignored
     * @throws UnsupportedMimeTypeException if the response mime type is not supported and those errors are not ignored
     * @throws SocketTimeoutException if the connection times out
     * @throws IOException on error
     */
    Document get() throws IOException;

    /**
     * Execute the request as a POST, and parse the result.
     * @return parsed Document
     * @throws MalformedURLException if the request URL is not a HTTP or HTTPS URL, or is otherwise malformed
     * @throws HttpStatusException if the response is not OK and HTTP response errors are not ignored
     * @throws UnsupportedMimeTypeException if the response mime type is not supported and those errors are not ignored
     * @throws SocketTimeoutException if the connection times out
     * @throws IOException on error
     */
    Document post() throws IOException;

    /**
     * Execute the request.
     * @return a response object
     * @throws MalformedURLException if the request URL is not a HTTP or HTTPS URL, or is otherwise malformed
     * @throws HttpStatusException if the response is not OK and HTTP response errors are not ignored
     * @throws UnsupportedMimeTypeException if the response mime type is not supported and those errors are not ignored
     * @throws SocketTimeoutException if the connection times out
     * @throws IOException on error
     */
    Connection.Response execute() throws IOException;

    /**
     * Get the request object associated with this connection
     * @return request
     */
    Connection.Request request();

// --Commented out by Inspection START (3/20/13 10:02 AM):
//    /**
//     * Set the connection's request
//     * @param request new request object
//     * @return this Connection, for chaining
//     */
//    Connection request(Connection.Request request);
// --Commented out by Inspection STOP (3/20/13 10:02 AM)

    /**
     * Get the response, once the request has been executed
     * @return response
     */
    Connection.Response response();

// --Commented out by Inspection START (3/20/13 10:02 AM):
//    /**
//     * Set the connection's response
//     * @param response new response
//     * @return this Connection, for chaining
//     */
//    Connection response(Connection.Response response);
// --Commented out by Inspection STOP (3/20/13 10:02 AM)


    /**
     * Common methods for Requests and Responses
     * @param <T> Type of Base, either Request or Response
     */
    interface Base<T extends Connection.Base> {

        /**
         * Get the URL
         * @return URL
         */
        URL url();

        /**
         * Set the URL
         * @param url new URL
         * @return this, for chaining
         */
        T url(URL url);

        /**
         * Get the request method
         * @return method
         */
        Connection.Method method();

        /**
         * Set the request method
         * @param method new method
         * @return this, for chaining
         */
        T method(Connection.Method method);

        /**
         * Get the value of a header. This is a simplified header model, where a header may only have one value.
         * <p>
         * Header names are case insensitive.
         * @param name name of header (case insensitive)
         * @return value of header, or null if not set.
         * @see #hasHeader(String)
         * @see #cookie(String)
         */
        String header(String name);

        /**
         * Set a header. This method will overwrite any existing header with the same case insensitive name. 
         * @param name Name of header
         * @param value Value of header
         * @return this, for chaining
         */
        T header(String name, String value);

        /**
         * Check if a header is present
         * @param name name of header (case insensitive)
         * @return if the header is present in this request/response
         */
        boolean hasHeader(String name);

        /**
         * Remove a header by name
         * @param name name of header to remove (case insensitive)
         * @return this, for chaining
         */
        T removeHeader(String name);

        /**
         * Retrieve all of the request/response headers as a map
         * @return headers
         */
        Map<String, String> headers();

        /**
         * Get a cookie value by name from this request/response.
         * <p>
         * Response objects have a simplified cookie model. Each cookie set in the response is added to the response
         * object's cookie key=value map. The cookie's path, domain, and expiry date are ignored.
         * @param name name of cookie to retrieve.
         * @return value of cookie, or null if not set
         */
        String cookie(String name);

        /**
         * Set a cookie in this request/response.
         * @param name name of cookie
         * @param value value of cookie
         * @return this, for chaining
         */
        T cookie(String name, String value);

        /**
         * Check if a cookie is present
         * @param name name of cookie
         * @return if the cookie is present in this request/response
         */
        boolean hasCookie(String name);

// --Commented out by Inspection START (3/20/13 10:02 AM):
//        /**
//         * Remove a cookie by name
//         * @param name name of cookie to remove
//         * @return this, for chaining
//         */
//        T removeCookie(String name);
// --Commented out by Inspection STOP (3/20/13 10:02 AM)

        /**
         * Retrieve all of the request/response cookies as a map
         * @return cookies
         */
        Map<String, String> cookies();

    }

    /**
     * Represents a HTTP request.
     */
    interface Request extends Connection.Base<Connection.Request> {
        /**
         * Get the request timeout, in milliseconds.
         * @return the timeout in milliseconds.
         */
        int timeout();

        /**
         * Update the request timeout.
         * @param millis timeout, in milliseconds
         * @return this Request, for chaining
         */
        Connection.Request timeout(int millis);

        /**
         * Get the maximum body size, in milliseconds.
         * @return the maximum body size, in milliseconds.
         */
        int maxBodySize();

        /**
         * Update the maximum body size, in milliseconds.
         * @param bytes maximum body size, in milliseconds.
         * @return this Request, for chaining
         */
        Connection.Request maxBodySize(int bytes);

        /**
         * Get the current followRedirects configuration.
         * @return true if followRedirects is enabled.
         */
        boolean followRedirects();

        /**
         * Configures the request to (not) follow server redirects. By default this is <b>true</b>.
         *
         * @param followRedirects true if server redirects should be followed.
         * @return this Request, for chaining
         */
        Connection.Request followRedirects(boolean followRedirects);

        /**
         * Get the current ignoreHttpErrors configuration.
         * @return true if errors will be ignored; false (default) if HTTP errors will cause an IOException to be thrown.
         */
        boolean ignoreHttpErrors();

    	/**
    	 * Configures the request to ignore HTTP errors in the response.
    	 * @param ignoreHttpErrors set to true to ignore HTTP errors.
         * @return this Request, for chaining
    	 */
        Connection.Request ignoreHttpErrors(boolean ignoreHttpErrors);

        /**
         * Get the current ignoreContentType configuration.
         * @return true if invalid content-types will be ignored; false (default) if they will cause an IOException to be thrown.
         */
        boolean ignoreContentType();

        /**
    	 * Configures the request to ignore the Content-Type of the response.
    	 * @param ignoreContentType set to true to ignore the content type.
         * @return this Request, for chaining
    	 */
        Connection.Request ignoreContentType(boolean ignoreContentType);

        /**
         * Add a data parameter to the request
         * @param keyval data to add.
         * @return this Request, for chaining
         */
        Connection.Request data(Connection.KeyVal keyval);

        /**
         * Get all of the request's data parameters
         * @return collection of keyvals
         */
        Collection<Connection.KeyVal> data();

        /**
         * Specify the parser to use when parsing the document.
         * @param parser parser to use.
         * @return this Request, for chaining
         */
        Connection.Request parser(Parser parser);

        /**
         * Get the current parser to use when parsing the document.
         * @return current Parser
         */
        Parser parser();
    }

    /**
     * Represents a HTTP response.
     */
    interface Response extends Connection.Base<Connection.Response> {
    	
    	/**
         * Get the status code of the response.
         * @return status code
         */
        int statusCode();

// --Commented out by Inspection START (3/20/13 10:02 AM):
//        /**
//         * Get the status message of the response.
//         * @return status message
//         */
//        String statusMessage();
// --Commented out by Inspection STOP (3/20/13 10:02 AM)

        /**
         * Get the character set name of the response.
         * @return character set name
         */
        String charset();

        /**
         * Get the response content type (e.g. "text/html");
         * @return the response content type
         */
        String contentType();

        /**
         * Parse the body of the response as a Document.
         * @return a parsed Document
         * @throws IOException on error
         */
        Document parse();

        /**
         * Get the body of the response as a plain string.
         * @return body
         */
        String body();

        /**
         * Get the body of the response as an array of bytes.
         * @return body bytes
         */
        byte[] bodyAsBytes();
    }

    /**
     * A Key Value tuple.
     */
    interface KeyVal {

// --Commented out by Inspection START (3/20/13 10:02 AM):
//        /**
//         * Update the key of a keyval
//         * @param key new key
//         * @return this KeyVal, for chaining
//         */
//        Connection.KeyVal key(String key);
// --Commented out by Inspection STOP (3/20/13 10:02 AM)

        /**
         * Get the key of a keyval
         * @return the key
         */
        String key();

// --Commented out by Inspection START (3/20/13 10:02 AM):
//        /**
//         * Update the value of a keyval
//         * @param value the new value
//         * @return this KeyVal, for chaining
//         */
//        Connection.KeyVal value(String value);
// --Commented out by Inspection STOP (3/20/13 10:02 AM)

        /**
         * Get the value of a keyval
         * @return the value
         */
        String value();
    }
}

