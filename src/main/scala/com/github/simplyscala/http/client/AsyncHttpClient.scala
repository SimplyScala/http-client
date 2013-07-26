package com.github.simplyscala.http.client

import com.ning.http.client.{AsyncCompletionHandler, Response}
import concurrent.{Promise, Future}

class AsyncHttpClient {
    private val javaClient = new com.ning.http.client.AsyncHttpClient()

    def get(url: String): Future[Response] = {
        val promise = Promise[Response]()

        javaClient.prepareGet(url).execute(new AsyncCompletionHandler[Response]{
            def onCompleted(response: Response): Response = {
                promise.success(response)
                response
            }
        })

        promise.future
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