package com.github.simplyscala.http.client

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import fr.simply.fixture.StubServerFixture
import fr.simply.{StaticServerResponse, GET}
import fr.simply.util.Text_Plain
import concurrent.{Await, Future}
import com.ning.http.client.{ListenableFuture, Response}
import concurrent.duration.Duration

class AsyncHttpClientTest extends FunSuite with ShouldMatchers with StubServerFixture {

    test("should return response") {
        val route = GET (
            path = "*",
            response = StaticServerResponse(Text_Plain, "yo", 200)
        )

        withStubServerFixture(8080, route) { server =>
            val response: Future[Response] = new AsyncHttpClient().get(s"http://localhost:${server.portInUse}")
            Await.result(response, Duration.Inf).getStatusCode should be (200)
        }
    }
}