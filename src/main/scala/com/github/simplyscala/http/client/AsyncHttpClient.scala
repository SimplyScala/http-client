package com.github.simplyscala.http.client

import com.ning.http.client.{AsyncHttpClient => JavaAsyncHttpClient, Cookie => JavaCookie, AsyncHttpClientConfig, AsyncCompletionHandler, Response}
import concurrent.{Promise, Future}
import request.util.Request
import scala.concurrent.duration._

/**
 * execute some HTTP Request (GET, POST, ...) asynchronously
 * @param requestTimeout define the timeout of the request - inifite by default - import scala.concurrent.duration._ to use '3 seconds' notation
 */
class AsyncHttpClient(requestTimeout: Duration = Duration.Inf) {
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
     * @param url the GET url want to execute 'http://mywebsite:8180/thepath?key1=value1;key2=value2'
     * @return an asynchronous [[com.ning.http.client.Response Response]]
     */
    def get(url: String): Future[Response] = {
        val promise = Promise[Response]()
        executeRequest(javaClient.prepareGet(url), promise)
        promise.future
    }

    /**
     * execute HTTP GET request from Request instance
     * @param request [[com.github.simplyscala.http.client.request.util.Request Request]]
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
     * execute HTTP GET request from Request instance
     * @param request: [[com.github.simplyscala.http.client.request.util.Request Request]]
     * @return an asynchronous [[com.ning.http.client.Response Response]]
     */
    def post(request: Request): Future[Response] = {
        val promise = Promise[Response]()
        executeRequest(initPreparedPostRequest(request), promise)
        promise.future
    }

    private def initPreparedGetRequest(request: Request): JavaAsyncHttpClient#BoundRequestBuilder = {
        val preparedGetRequest = javaClient.prepareGet(buildGetUrl(request))

        addCookieInPreparedRequest(request, preparedGetRequest)
        request.headers.foreach { header => preparedGetRequest.addHeader(header.key, header.value) }

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

    private def executeRequest(preparedRequest: JavaAsyncHttpClient#BoundRequestBuilder, promise: Promise[Response]) {
        preparedRequest.execute(new AsyncCompletionHandler[Response] {
            def onCompleted(response: Response): Response = { promise.success(response); response }
        })
    }

    private def buildGetUrl(request: Request): String = {
        val url = s"${request.host}:${request.port}${request.path}"
        val parametersUrl = request.parameters.foldLeft("?"){ case (acc,(k,v)) => acc + k + "=" + v }

        url + parametersUrl
    }

    private def addCookieInPreparedRequest(request: Request, preparedRequest: JavaAsyncHttpClient#BoundRequestBuilder) {
        request.cookies.foreach { cookie =>
            preparedRequest.addCookie(new JavaCookie(cookie.domain, cookie.name, cookie.value, cookie.path,
                0, false, 0) //TODO version par défaut à retirer quand cookie validé
            )
        }
    }
}