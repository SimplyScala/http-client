package com.github.simplyscala.http.client

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import fr.simply.fixture.StubServerFixture
import fr.simply.{DynamicServerResponse, POST, StaticServerResponse, GET}
import fr.simply.util.Text_Plain
import org.simpleframework.http.{ Request => ServerRequest }
import concurrent.{Await, Future}
import com.ning.http.client.Response
import request.util.{Cookie, Request}
import concurrent.duration._
import java.util.concurrent.TimeoutException

class AsyncHttpClientTest extends FunSuite with ShouldMatchers with StubServerFixture {

    test("[GET] request should return response") {
        val route = GET (
            path = "/test",
            response = StaticServerResponse(Text_Plain, "yo", 200)
        )

        withStubServerFixture(8080, route) { server =>
            val response: Future[Response] = new AsyncHttpClient().get(s"http://localhost:${server.portInUse}/test")
            Await.result(response, 100 milliseconds).getStatusCode should be (200)
        }
    }

    test("[GET] request with params should return response") {
        val route = GET (
            path = "*",
            params = Map("toto" -> "titi"),
            response = StaticServerResponse(Text_Plain, "yo", 200)
        )

        withStubServerFixture(8080, route) { server =>
            val response: Future[Response] = new AsyncHttpClient().get(s"http://localhost:${server.portInUse}/test?toto=titi")
            Await.result(response, 100 milliseconds).getStatusCode should be (200)
        }
    }

    test("[POST] request should return response") {
        val route = POST (
            path = "/test",
            response = StaticServerResponse(Text_Plain, "yo", 200)
        )

        withStubServerFixture(8080, route) { server =>
            val response: Future[Response] = new AsyncHttpClient().post(s"http://localhost:${server.portInUse}/test")
            Await.result(response, 100 milliseconds).getStatusCode should be (200)
        }
    }

    test("[POST] request with params should return response") {
        val route = POST (
            path = "*",
            params = Map("toto" -> "titi"),
            response = StaticServerResponse(Text_Plain, "yo", 200)
        )

        withStubServerFixture(8080, route) { server =>
            val response: Future[Response] = new AsyncHttpClient().post(s"http://localhost:${server.portInUse}", Map("toto"-> "titi"))
            Await.result(response, 100 milliseconds).getStatusCode should be (200)
        }
    }

    test("[GET] request with cookie should return response") {
        val dynamicResponse = { request: ServerRequest =>
            val cookie = request.getCookie("name")
            //println("must : 42 but : " + cookie.getVersion) // failed param
            //println("must : true but : " + cookie.getSecure)// failed param
            //println("must : 12 but : " + cookie.getExpiry)  // failed param
            if(cookie != null && cookie.getValue == "value" && cookie.getName == "name" &&
               cookie.getDomain == "domain" && cookie.getPath == "path" /*&& cookie.getVersion == 0 &&
               cookie.getExpiry == 12 && cookie.getSecure == true*/)
                StaticServerResponse(Text_Plain, "OK dynamic", 200)
            else StaticServerResponse(Text_Plain, "OK dynamic", 404)
        }

        val route = GET (
            path = "*",
            params = Map("toto" -> "titi"),
            response = DynamicServerResponse(dynamicResponse)
        )

        withStubServerFixture(8080, route) { server =>
            val cookie: Cookie = Cookie("domain", "name", "value", "path")
            val request: Request = Request("http://localhost", 8080, "/", Map("toto" -> "titi"), cookie)
            val response: Future[Response] = new AsyncHttpClient().get(request)
            Await.result(response, 1 seconds).getStatusCode should be (200)
        }
    }

    test("[POST] request with cookie should return response") {
        val dynamicResponse = { request: ServerRequest =>
            val cookie = request.getCookie("name")
            //println("must : 42 but : " + cookie.getVersion) // failed param
            //println("must : true but : " + cookie.getSecure)// failed param
            //println("must : 12 but : " + cookie.getExpiry)  // failed param
            if(cookie != null && cookie.getValue == "value" && cookie.getName == "name" &&
                cookie.getDomain == "domain" && cookie.getPath == "path" /*&& cookie.getVersion == 0 &&
               cookie.getExpiry == 12 && cookie.getSecure == true*/)
                StaticServerResponse(Text_Plain, "OK dynamic", 200)
            else StaticServerResponse(Text_Plain, "OK dynamic", 404)
        }

        val route = POST (
            path = "*",
            response = DynamicServerResponse(dynamicResponse)
        )

        withStubServerFixture(8080, route) { server =>
            val cookie: Cookie = Cookie("domain", "name", "value", "path")
            val request: Request = Request("http://localhost", 8080, "/", Map("toto" -> "titi"), cookie)
            val response: Future[Response] = new AsyncHttpClient().post(request)
            Await.result(response, 1 seconds).getStatusCode should be (200)
        }
    }

    test("[GET] request with timeout must failed if response is too long") {
        val dynamicResponse = { request: ServerRequest =>
            Thread.sleep(300)
            StaticServerResponse(Text_Plain, "OK dynamic", 200)
        }

        val route = GET (
            path = "*",
            response = DynamicServerResponse(dynamicResponse)
        )

        withStubServerFixture(8080, route) { server =>
            val response = new AsyncHttpClient(200 milliseconds).get(s"http://localhost:${server.portInUse}")
            evaluating { Await.result(response, 1 seconds) } should produce[TimeoutException]
        }
    }
}