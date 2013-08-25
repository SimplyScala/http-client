package com.github.simplyscala.http.client

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import fr.simply.fixture.StubServerFixture
import fr.simply._
import fr.simply.util.Text_Plain
import org.simpleframework.http.{ Request => ServerRequest }
import concurrent.{Await, Future}
import com.ning.http.client.Response
import concurrent.duration._
import java.util.concurrent.TimeoutException
import fr.simply.DynamicServerResponse
import fr.simply.StaticServerResponse

class AsyncHttpClientSimpleAPITest extends FunSuite with ShouldMatchers with StubServerFixture {

    test("[GET] request should return response") {
        val route = GET (
            path = "/test",
            response = StaticServerResponse(Text_Plain, "yo", 200)
        )

        withStubServerFixture(8080, route) { server =>
            val response: Future[Response] = new AsyncHttpClient().get(s"http://localhost:${server.portInUse}/test")
            Await.result(response, 300 milliseconds).getStatusCode should be (200)
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
            Await.result(response, 300 milliseconds).getStatusCode should be (200)
        }
    }

    test("[POST] request should return response") {
        val route = POST (
            path = "/test",
            response = StaticServerResponse(Text_Plain, "yo", 200)
        )

        withStubServerFixture(8080, route) { server =>
            val response: Future[Response] = new AsyncHttpClient().post(s"http://localhost:${server.portInUse}/test")
            Await.result(response, 300 milliseconds).getStatusCode should be (200)
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
            Await.result(response, 300 milliseconds).getStatusCode should be (200)
        }
    }

    test("[ANY] when server is down, should return Exception") {
        val response: Future[Response] = new AsyncHttpClient().get(s"http://localhost:123/test")
        evaluating { Await.result(response, 300 milliseconds) } should produce[java.net.ConnectException]
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

    test("[PUT] request") {
        val route = PUT (
            path = "/test",
            params = Map("test" -> "value"),
            response = StaticServerResponse(Text_Plain, "yo", 200)
        )

        withStubServerFixture(8080, route) { server =>
            val response: Future[Response] = new AsyncHttpClient().put(s"http://localhost:${server.portInUse}/test", Map("test" -> "value"))
            Await.result(response, 200 milliseconds).getStatusCode should be (200)
        }
    }

    test("[HEAD] request") {
        val route = HEAD (
            path = "/test",
            params = Map("test" -> "value"),
            response = StaticServerResponse(Text_Plain, "", 200)
        )

        withStubServerFixture(8080, route) { server =>
            val response: Future[Response] = new AsyncHttpClient().head(s"http://localhost:${server.portInUse}/test", Map("test" -> "value"))
            Await.result(response, 200 milliseconds).getStatusCode should be (200)
        }
    }

    test("[DELETE] request") {
        val route = DELETE (
            path = "/test",
            params = Map("test" -> "value"),
            response = StaticServerResponse(Text_Plain, "", 200)
        )

        withStubServerFixture(8080, route) { server =>
            val response: Future[Response] = new AsyncHttpClient().delete(s"http://localhost:${server.portInUse}/test", Map("test" -> "value"))
            Await.result(response, 200 milliseconds).getStatusCode should be (200)
        }
    }
}