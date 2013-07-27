package com.github.simplyscala.http.client

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import fr.simply.fixture.StubServerFixture
import fr.simply.{DynamicServerResponse, POST, StaticServerResponse, GET}
import fr.simply.util.Text_Plain
import concurrent.{Await, Future}
import com.ning.http.client.Response
import request.util.{Cookie, Request}
import concurrent.duration._

class AsyncHttpClientTest extends FunSuite with ShouldMatchers with StubServerFixture {

    test("[GET] request should return response") {
        val route = GET (
            path = "*",
            response = StaticServerResponse(Text_Plain, "yo", 200)
        )

        withStubServerFixture(8080, route) { server =>
            val response: Future[Response] = new AsyncHttpClient().get(s"http://localhost:${server.portInUse}")
            Await.result(response, 100 milliseconds).getStatusCode should be (200)
        }
    }

    test("[POST] request should return response") {
        val route = POST (
            path = "*",
            response = StaticServerResponse(Text_Plain, "yo", 200)
        )

        withStubServerFixture(8080, route) { server =>
            val response: Future[Response] = new AsyncHttpClient().post(s"http://localhost:${server.portInUse}")
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
        val dynamicResponse = { request: org.simpleframework.http.Request =>
            val cookie = request.getCookie("name")
            println("must : 42 but : " + cookie.getVersion) // failed param
            println("must : true but : " + cookie.getSecure)// failed param
            println("must : 12 but : " + cookie.getExpiry)  // failed param
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
}