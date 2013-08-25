package com.github.simplyscala.http.client

import com.ning.http.client.{AsyncHttpClient => JavaAsyncHttpClient, Cookie => JavaCookie, AsyncHttpClientConfig, AsyncCompletionHandler, Response}
import concurrent.{Promise, Future}
import com.github.simplyscala.http.client.request.util._
import scala.concurrent.duration._
import com.github.simplyscala.http.client.request.util.Request
import com.github.simplyscala.http.client.request.util.ByteBody

/**
 * execute some HTTP Request (GET, POST, ...) asynchronously
 * @param requestTimeout define the timeout of the request - inifite by default - import scala.concurrent.duration._ to use '3 seconds' notation
 */
class AsyncHttpClient(requestTimeout: Duration = Duration.Inf) {
    // TODO provide constructor with AsyncHttpClientConfig.Builder parameter or wrap this class to case class

    private val javaClient = {
        val builder = new AsyncHttpClientConfig.Builder()
        //builder.setCompressionEnabled(true)
        requestTimeout match {
            case Duration.Inf | Duration.MinusInf =>
            case otherValue => builder.setRequestTimeoutInMs(requestTimeout.toMillis.toInt)  // TODO defense the position Long.toInt
        }
        builder.build()

        new JavaAsyncHttpClient(builder.build())
    }

    /**
     * execute HTTP GET request from simple String request
     * @param url the GET url want to execute 'http://mywebsite:8180/thepath'
     * @param params the GET query parameters [Optional] if you want your GET query parameters was url encoded use params
     *               and don't write url like 'http://mywebsite:8180/thepath?key1=value1;key2=value2'
     * @return an asynchronous [[com.ning.http.client.Response Response]]
     */
    def get(url: String, params: Map[String,String] = Map()): Future[Response] = {
        val promise = Promise[Response]()
        executeRequest(initPreparedGetRequest(url, params), promise)
        promise.future
    }

    // TODO Map(String, List(String)) on a le droit d'avoir deux fois la clé toto dans une url get, post etc...

    /**
     * execute HTTP GET request from Request instance
     * @param request [[Request Request]]
     * @return an asynchronous [[com.ning.http.client.Response Response]]
     */
    def get(request: Request): Future[Response] = {
        val promise = Promise[Response]()
        executeRequest(initPreparedGetRequest(request), promise)
        promise.future
    }

    /**
     * execute HTTP POST request from simple String request
     * @param url the POST url want to execute 'http://mywebsite:8180/thepath'
     * @param params form params for POST request
     * @return an asynchronous [[com.ning.http.client.Response Response]]
     */
    def post(url: String, params: Map[String,String] = Map()): Future[Response] = {
        val promise = Promise[Response]()
        executeRequest(initPreparedPostRequest(url, params), promise)
        promise.future
    }

    /**
     * execute HTTP POST request from Request instance
     * @param request: [[Request Request]]
     * @return an asynchronous [[com.ning.http.client.Response Response]]
     */
    def post(request: Request): Future[Response] = {
        val promise = Promise[Response]()
        executeRequest(initPreparedPostRequest(request), promise)
        promise.future
    }

    /**
     * execute HTTP PUT request from simple String request
     * @param url the PUT url want to execute 'http://mywebsite:8180/thepath'
     * @param params form params for PUT request
     * @return an asynchronous [[com.ning.http.client.Response Response]]
     */
    def put(url: String, params: Map[String,String] = Map()): Future[Response] = {
        val promise = Promise[Response]()
        executeRequest(initPreparedPutRequest(url, params), promise)
        promise.future
    }

    /**
     * execute HTTP PUT request from Request instance
     * @param request: [[Request Request]]
     * @return an asynchronous [[com.ning.http.client.Response Response]]
     */
    def put(request: Request): Future[Response] = {
        val promise = Promise[Response]()
        executeRequest(initPreparedPutRequest(request), promise)
        promise.future
    }

    /**
     * execute HTTP HEAD request from simple String request
     * HEAD request produce same server response than GET request except HEAD request produce an empty-body server response
     *
     * @example with params into url
     * {{{
     * new AsyncHttpClient().head("http://someurl:8080/test?param1=value1;param2=value2")
     * }}}
     *
     * @example with params not into url
     * {{{
     * new AsyncHttpClient().head("http://someurl:8080/test", Map("param1" -> "value1", "param2" -> "value2" ))
     * }}}
     *
     * @param url the HEAD url want to execute 'http://mywebsite:8180/thepath'
     * @param params form params for HEAD request
     * @return an asynchronous [[com.ning.http.client.Response Response]]
     */
    def head(url: String, params: Map[String,String] = Map()): Future[Response] = {
        val promise = Promise[Response]()
        executeRequest(initPreparedHeadRequest(url, params), promise)
        promise.future
    }

    /**
     * execute HTTP HEAD request from Request instance
     * HEAD request produce same server response than GET request except HEAD request produce an empty-body server response
     * @param request: [[Request Request]]
     * @return an asynchronous [[com.ning.http.client.Response Response]]
     */
    def head(request: Request): Future[Response] = {
        val promise = Promise[Response]()
        executeRequest(initPreparedHeadRequest(request), promise)
        promise.future
    }

    /**
     * execute HTTP DELETE request from simple String request
     * @param url the DELETE url want to execute 'http://mywebsite:8180/thepath'
     * @param params form params for PUT request
     * @return an asynchronous [[com.ning.http.client.Response Response]]
     */
    def delete(url: String, params: Map[String,String] = Map()): Future[Response] = {
        val promise = Promise[Response]()
        executeRequest(initPreparedDeleteRequest(url, params), promise)
        promise.future
    }

    /**
     * execute HTTP DELETE request from Request instance
     * @param request: [[Request Request]]
     * @return an asynchronous [[com.ning.http.client.Response Response]]
     */
    def delete(request: Request): Future[Response] = {
        val promise = Promise[Response]()
        executeRequest(initPreparedDeleteRequest(request), promise)
        promise.future
    }

    private def initPreparedGetRequest(url: String, params: Map[String, String]): JavaAsyncHttpClient#BoundRequestBuilder = {
        val preparedGetRequest = javaClient.prepareGet(url)
        params.foreach { case (k, v) => preparedGetRequest.addQueryParameter(k, v) }
        preparedGetRequest
    }

    private def initPreparedGetRequest(request: Request): JavaAsyncHttpClient#BoundRequestBuilder = {
        val preparedGetRequest = javaClient.prepareGet(s"${request.host}:${request.port}${request.path}")

        addCookieInPreparedRequest(request, preparedGetRequest)
        request.headers.foreach { header => preparedGetRequest.addHeader(header.key, header.value) }
        request.parameters.foreach { case (k, v) => preparedGetRequest.addQueryParameter(k, v) }

        /*request.body match {
            case ByteBody(body) => preparedGetRequest.setBody(body)
            case StringBody(body) => preparedGetRequest.setBody(body)
            case NoBody => // no body
        }*/

        preparedGetRequest
    }

    private def initPreparedPostRequest(url: String, params: Map[String, String]): JavaAsyncHttpClient#BoundRequestBuilder = {
        val preparedPostRequest = javaClient.preparePost(url)
        params.foreach { case (k, v) => preparedPostRequest.addParameter(k, v) }
        preparedPostRequest
    }

    private def initPreparedPostRequest(request: Request): JavaAsyncHttpClient#BoundRequestBuilder = {
        val url = s"${request.host}:${request.port}${request.path}"

        val preparedPostRequest = javaClient.preparePost(url)

        addCookieInPreparedRequest(request, preparedPostRequest)
        request.headers.foreach { header => preparedPostRequest.addHeader(header.key, header.value) }
        request.parameters.foreach { case (k, v) => preparedPostRequest.addParameter(k, v)}

        preparedPostRequest
    }

    private def initPreparedPutRequest(url: String, params: Map[String, String]): JavaAsyncHttpClient#BoundRequestBuilder = {
        val preparedPutRequest = javaClient.preparePut(url)
        params.foreach { case (k, v) => preparedPutRequest.addParameter(k, v) }
        preparedPutRequest
    }

    private def initPreparedPutRequest(request: Request): JavaAsyncHttpClient#BoundRequestBuilder = {
        val url = s"${request.host}:${request.port}${request.path}"

        val preparedPutRequest = javaClient.preparePut(url)

        addCookieInPreparedRequest(request, preparedPutRequest)
        request.headers.foreach { header => preparedPutRequest.addHeader(header.key, header.value) }
        request.parameters.foreach { case (k, v) => preparedPutRequest.addParameter(k, v)}

        preparedPutRequest
    }

    private def initPreparedHeadRequest(url: String, params: Map[String, String]): JavaAsyncHttpClient#BoundRequestBuilder = {
        val preparedHeadRequest = javaClient.prepareHead(url)
        params.foreach { case (k, v) => preparedHeadRequest.addQueryParameter(k, v) }
        preparedHeadRequest
    }

    private def initPreparedHeadRequest(request: Request): JavaAsyncHttpClient#BoundRequestBuilder = {
        val url = s"${request.host}:${request.port}${request.path}"

        val preparedHeadRequest = javaClient.prepareHead(url)

        addCookieInPreparedRequest(request, preparedHeadRequest)
        request.headers.foreach { header => preparedHeadRequest.addHeader(header.key, header.value) }
        request.parameters.foreach { case (k, v) => preparedHeadRequest.addQueryParameter(k, v)}

        preparedHeadRequest
    }

    private def initPreparedDeleteRequest(url: String, params: Map[String, String]): JavaAsyncHttpClient#BoundRequestBuilder = {
        val preparedDeleteRequest = javaClient.prepareDelete(url)
        params.foreach { case (k, v) => preparedDeleteRequest.addParameter(k, v) }
        preparedDeleteRequest
    }

    private def initPreparedDeleteRequest(request: Request): JavaAsyncHttpClient#BoundRequestBuilder = {
        val url = s"${request.host}:${request.port}${request.path}"

        val preparedDeleteRequest = javaClient.prepareDelete(url)

        addCookieInPreparedRequest(request, preparedDeleteRequest)
        request.headers.foreach { header => preparedDeleteRequest.addHeader(header.key, header.value) }
        request.parameters.foreach { case (k, v) => preparedDeleteRequest.addParameter(k, v)}

        preparedDeleteRequest
    }

    private def executeRequest(preparedRequest: JavaAsyncHttpClient#BoundRequestBuilder, promise: Promise[Response]) {
        preparedRequest.execute(new AsyncCompletionHandler[Response] {
            def onCompleted(response: Response): Response = { promise.success(response); response }
            override def onThrowable(t: Throwable ) { promise.failure(t) }
        })
    }

    private def addCookieInPreparedRequest(request: Request, preparedRequest: JavaAsyncHttpClient#BoundRequestBuilder) {
        request.cookies.foreach { cookie =>
            preparedRequest.addCookie(new JavaCookie(cookie.domain, cookie.name, cookie.value, cookie.path,
                0, false, 0) //TODO version par défaut à retirer quand cookie validé
            )
        }
    }
}