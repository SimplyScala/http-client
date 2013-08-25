package com.github.simplyscala.http.client

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import fr.simply.fixture.StubServerFixture
import fr.simply.{DynamicServerResponse, GET, StaticServerResponse}
import fr.simply.util.Text_Plain
import scala.concurrent.{Await, Future}
import com.ning.http.client.Response
import com.github.simplyscala.http.client.request.util.Request
import org.simpleframework.http.{ Request => ServerRequest }
import concurrent.duration._

class AsyncHttpClientEncodingParamsTest extends FunSuite with ShouldMatchers with StubServerFixture {

    test("[GET] request should url encode its parameters") {
        val dynamicResponse = { request: ServerRequest =>
            val paramsEncoded = request.getParameter("toto")
            if(paramsEncoded == "+dslmdk12=ER") StaticServerResponse(Text_Plain, "OK encoded", 200)
            else StaticServerResponse(Text_Plain, "KO", 403)
        }

        val route = GET (
            path = "*",
            response = DynamicServerResponse(dynamicResponse)
        )

        withStubServerFixture(8080, route) { server =>
            val response: Future[Response] = new AsyncHttpClient().get(s"http://localhost:${server.portInUse}", Map("toto" -> "+dslmdk12=ER"))
            Await.result(response, 1 seconds).getStatusCode should be (200)
        }
    }

    test("[GET] request, using 'Request' instance should url encode its parameters") {
        val dynamicResponse = { request: ServerRequest =>
            val paramsEncoded = request.getParameter("toto")
            if(paramsEncoded == "+dslmdk12=ER") StaticServerResponse(Text_Plain, "OK encoded", 200)
            else StaticServerResponse(Text_Plain, "KO", 403)
        }

        val route = GET (
            path = "*",
            response = DynamicServerResponse(dynamicResponse)
        )

        withStubServerFixture(8080, route) { server =>
            val request = Request("http://localhost", server.portInUse, "/", Map("toto" -> "+dslmdk12=ER"))
            val response: Future[Response] = new AsyncHttpClient().get(request)
            Await.result(response, 1 seconds).getStatusCode should be (200)
        }
    }

    test("[GET] request don't use 'params' attribute should not url encode its parameters") {
        val dynamicResponse = { request: ServerRequest =>
            val paramsEncoded = request.getParameter("toto")
            if(paramsEncoded == "+dslmdk12=ER") StaticServerResponse(Text_Plain, "OK encoded", 200)
            else StaticServerResponse(Text_Plain, "KO", 403)
        }

        val route = GET (
            path = "*",
            response = DynamicServerResponse(dynamicResponse)
        )

        withStubServerFixture(8080, route) { server =>
            val response: Future[Response] = new AsyncHttpClient().get(s"http://localhost:${server.portInUse}?toto=+dslmdk12=ER")
            Await.result(response, 1 seconds).getStatusCode should be (403)
        }
    }

    test("[AFTER][POST] encoding params ...") {

    }

}