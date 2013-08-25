package com.github.simplyscala.http.client

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import fr.simply._
import fr.simply.util.Text_Plain
import scala.concurrent.Await
import fr.simply.fixture.StubServerFixture
import concurrent.duration._
import fr.simply.StaticServerResponse
import com.github.simplyscala.http.client.request.util.Request

class AsyncHttpClientRequestAPITest extends FunSuite with ShouldMatchers with StubServerFixture {

    test("[GET] request using 'Request instance' should return response") {
        val route = GET (
            path = "/test",
            params = Map("toto" -> "titi"),
            response = StaticServerResponse(Text_Plain, "yo", 200)
        )

        withStubServerFixture(8080, route) { server =>
            val request = Request("http://localhost", server.portInUse, "/test", Map("toto" -> "titi"))
            val response = new AsyncHttpClient().get(request)
            Await.result(response, 300 milliseconds).getStatusCode should be (200)
        }
    }

    test("[POST] request using 'Request instance' should return response") {
        val route = POST (
            path = "/test",
            params = Map("toto" -> "titi"),
            response = StaticServerResponse(Text_Plain, "yo", 200)
        )

        withStubServerFixture(8080, route) { server =>
            val request = Request("http://localhost", server.portInUse, "/test", Map("toto" -> "titi"))
            val response = new AsyncHttpClient().post(request)
            Await.result(response, 300 milliseconds).getStatusCode should be (200)
        }
    }

    test("[PUT] request using 'Request instance' should return response") {
        val route = PUT (
            path = "/test",
            params = Map("test" -> "value"),
            response = StaticServerResponse(Text_Plain, "yo", 200)
        )

        withStubServerFixture(8080, route) { server =>
            val request = Request("http://localhost", server.portInUse, "/test", Map("test" -> "value"))
            val response = new AsyncHttpClient().put(request)
            Await.result(response, 200 milliseconds).getStatusCode should be (200)
        }
    }

    test("[HEAD] request using 'Request instance' should return response") {
        val route = HEAD (
            path = "/test",
            params = Map("test" -> "value"),
            response = StaticServerResponse(Text_Plain, "", 200)
        )

        withStubServerFixture(8080, route) { server =>
            val request = Request("http://localhost", server.portInUse, "/test", Map("test" -> "value"))
            val response = new AsyncHttpClient().head(request)
            Await.result(response, 200 milliseconds).getStatusCode should be (200)
        }
    }

    test("[DELETE] request using 'Request instance' should return response") {
        val route = DELETE (
            path = "/test",
            params = Map("test" -> "value"),
            response = StaticServerResponse(Text_Plain, "", 200)
        )

        withStubServerFixture(8080, route) { server =>
            val request = Request("http://localhost", server.portInUse, "/test", Map("test" -> "value"))
            val response = new AsyncHttpClient().delete(request)
            Await.result(response, 200 milliseconds).getStatusCode should be (200)
        }
    }
}