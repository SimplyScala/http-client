package com.github.simplyscala.http.client

import fr.simply.{DynamicServerResponse, GET, StaticServerResponse}
import fr.simply.util.Text_Plain
import com.github.simplyscala.http.client.request.util.{Request, Body}
import org.simpleframework.http.{ Request => ServerRequest }
import concurrent.duration._
import scala.concurrent.Await
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import fr.simply.fixture.StubServerFixture

class AsyncHttpClientRequestAPIWithBodyTest extends FunSuite with ShouldMatchers with StubServerFixture {

    ignore("[GET] request using 'Request instance', with StringBody, should return response") {
        val dynamicResponse = { request: ServerRequest =>
            val paramsEncoded = request.getNames
            println("eee " + paramsEncoded)
            if(paramsEncoded == "+dslmdk12=ER") StaticServerResponse(Text_Plain, "OK encoded", 200)
            else StaticServerResponse(Text_Plain, "KO", 403)
        }

        val route = GET (
            path = "/test",
            response = DynamicServerResponse(dynamicResponse)
        )

        withStubServerFixture(8080, route) { server =>
            val request = Request("http://localhost", server.portInUse, "/test", body = Body("toto"))
            val response = new AsyncHttpClient().get(request)
            Await.result(response, 300 milliseconds).getStatusCode should be (200)
        }
    }
}