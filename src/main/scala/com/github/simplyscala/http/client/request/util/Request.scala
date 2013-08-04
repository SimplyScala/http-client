package com.github.simplyscala.http.client.request.util

case class Request(host: String,
                   port: Int = 80,
                   path: String,
                   parameters: Map[String,String] = Map(),
                   cookies: Seq[Cookie] = Nil,
                   headers: Seq[Header] = Nil
                   )

case class Cookie(domain: String = "",
                  name: String = "",
                  value: String = "",
                  path: String = ""/*,
                  maxAge: Int = 0,
                  secure: Boolean = false,
                  version: Int = 0,
                  httpOnly: Boolean = false,    TODO IMPOSSIBLE DE VALIDER LE FONCTIONNEMENT POUR L'INSTANT
                  discard: Boolean = false,
                  comment: String = "",
                  commentUrl: String = "",
                  ports: Iterable[Int] = Nil*/
                  )

case class Header(key: String, value: String)

/*
AsyncHttpClient.BoundRequestBuilder 	addBodyPart(Part part)
AsyncHttpClient.BoundRequestBuilder 	addCookie(Cookie cookie)
AsyncHttpClient.BoundRequestBuilder 	addHeader(String name, String value)
AsyncHttpClient.BoundRequestBuilder 	addParameter(String key, String value)
AsyncHttpClient.BoundRequestBuilder 	addQueryParameter(String name, String value)
Request 	build()
ListenableFuture<Response> 	execute()
<T> ListenableFuture<T> 	execute(AsyncHandler<T> handler)
AsyncHttpClient.BoundRequestBuilder 	setBody(byte[] data)
AsyncHttpClient.BoundRequestBuilder 	setBody(Request.EntityWriter dataWriter)
AsyncHttpClient.BoundRequestBuilder 	setBody(Request.EntityWriter dataWriter, long length)
AsyncHttpClient.BoundRequestBuilder 	setBody(InputStream stream)
AsyncHttpClient.BoundRequestBuilder 	setBody(String data)
AsyncHttpClient.BoundRequestBuilder 	setHeader(String name, String value)
AsyncHttpClient.BoundRequestBuilder 	setHeaders(FluentCaseInsensitiveStringsMap headers)
AsyncHttpClient.BoundRequestBuilder 	setHeaders(Map<String, Collection<String>> headers)
AsyncHttpClient.BoundRequestBuilder 	setParameters(FluentStringsMap parameters)
AsyncHttpClient.BoundRequestBuilder 	setParameters(Map<String, Collection<String>> parameters)
AsyncHttpClient.BoundRequestBuilder 	setSignatureCalculator(SignatureCalculator signatureCalculator)
AsyncHttpClient.BoundRequestBuilder 	setUrl(String url)
AsyncHttpClient.BoundRequestBuilder 	setVirtualHost(String virtualHost)*/
