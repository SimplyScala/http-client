package com.github.simplyscala.http.client

import com.ning.http.client.{AsyncCompletionHandler, Response}
import com.ning.http.client.{ AsyncHttpClient => JavaAsyncHttpClient }
import com.ning.http.client.{Cookie => JavaCookie }
import concurrent.{Promise, Future}
import request.util.Request

class AsyncHttpClient {
    private val javaClient = new JavaAsyncHttpClient()

    def get(url: String): Future[Response] = {
        val promise = Promise[Response]()

        executeGetRequest(url, promise)

        promise.future
    }

    def get(request: Request): Future[Response] = {
        val promise = Promise[Response]()

        val url: String = s"${request.host}:${request.port}${request.path}"
        val parametersUrl = request.parameters.foldLeft("?"){ case (acc,(k,v)) => acc + k + "=" + v }

        val preparedGetRequest = javaClient.prepareGet( s"${url + parametersUrl}" )
        request.cookies.foreach { cookie =>
            preparedGetRequest.addCookie(new JavaCookie(cookie.domain, cookie.name, cookie.value, cookie.path,
                                                    0, false, 0) //TODO version par défaut à retirer quand cookie validé
            )
        }

        preparedGetRequest.execute(new AsyncCompletionHandler[Response] {
            def onCompleted(response: Response): Response = { promise.success(response); response }
        })

        promise.future
    }

    def post(url: String, params: Map[String,String] = Map()): Future[Response] = {
        val promise = Promise[Response]()

        executePostRequest(url, params, promise)

        promise.future
    }

    private def executeGetRequest(url: String, promise: Promise[Response]) {
        javaClient.prepareGet(url).execute(new AsyncCompletionHandler[Response] {
            def onCompleted(response: Response): Response = { promise.success(response); response }
        })
    }

    private def executePostRequest(url: String, params: Map[String,String], promise: Promise[Response]) {
        val preparedPostRequest = javaClient.preparePost(url)

        params.foreach { case (k,v) => preparedPostRequest.addParameter(k,v) }

        preparedPostRequest.execute(new AsyncCompletionHandler[Response] {
            def onCompleted(response: Response): Response = { promise.success(response); response }
        })
    }
}

/*Exemple API asynchrone
======================

val asyncHttpClient = new AsyncHttpClient(httpOptions, httpHeaders) // options::timeout, proxy etc ...
val result: Future[HttpResponse] = asyncHttpClient.get("http://myurl.com")

Example API synchrone
=====================

val httpClient = new HttpClient(httpOptions, httpHeaders)
val result: HttpResponse = httpClient.get(url: String, port: Int)

Example API HttpResponse
========================

val response = ...
response.header Option[Header]
response.body Option[Body] .toString, .toJson, .toXml, toStream ????
response.code Int
response.size Int
response.sender Option[Sender]
response*/