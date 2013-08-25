package com.github.simplyscala.http.client

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import fr.simply.fixture.StubServerFixture
import fr.simply._
import fr.simply.util.Text_Plain
import scala.concurrent.Await
import org.simpleframework.http.{ Request => ServerRequest }
import concurrent.duration._
import com.github.simplyscala.http.client.request.util.Header
import fr.simply.DynamicServerResponse
import fr.simply.StaticServerResponse
import com.github.simplyscala.http.client.request.util.Request

class AsyncHttpCllientRequestAPIWithHeaderTest extends FunSuite with ShouldMatchers with StubServerFixture {

    test("[GET] request with header should return response") {
        val dynamicResponse = { request: ServerRequest =>
            println(request.getHeader)
            if(request.getValue("toto") == "titi"  && request.getValue("key") == "value") StaticServerResponse(Text_Plain, "OK dynamic", 200)
            else StaticServerResponse(Text_Plain, "KO", 404)
        }

        val route = GET (
            path = "*",
            response = DynamicServerResponse(dynamicResponse)
        )

        withStubServerFixture(8080, route) { server =>
            val request = Request("http://localhost", server.portInUse, "/", headers = Seq(Header("toto", "titi"), Header("key", "value")))
            val response = new AsyncHttpClient().get(request)
            Await.result(response, 300 milliseconds).getStatusCode should be (200)
        }
    }

    test("[POST] request with header should return response") {
        val dynamicResponse = { request: ServerRequest =>
            println(request.getHeader)
            if(request.getValue("toto") == "titi"  && request.getValue("key") == "value") StaticServerResponse(Text_Plain, "OK dynamic", 200)
            else StaticServerResponse(Text_Plain, "KO", 404)
        }

        val route = POST (
            path = "*",
            response = DynamicServerResponse(dynamicResponse)
        )

        withStubServerFixture(8080, route) { server =>
            val request = Request("http://localhost", server.portInUse, "/", headers = Seq(Header("toto", "titi"), Header("key", "value")))
            val response = new AsyncHttpClient().post(request)
            Await.result(response, 300 milliseconds).getStatusCode should be (200)
        }
    }

    test("[PUT] request with header should return response") {
        val dynamicResponse = { request: ServerRequest =>
            println(request.getHeader)
            if(request.getValue("toto") == "titi"  && request.getValue("key") == "value") StaticServerResponse(Text_Plain, "OK dynamic", 200)
            else StaticServerResponse(Text_Plain, "KO", 404)
        }

        val route = PUT (
            path = "*",
            response = DynamicServerResponse(dynamicResponse)
        )

        withStubServerFixture(8080, route) { server =>
            val request = Request("http://localhost", server.portInUse, "/", headers = Seq(Header("toto", "titi"), Header("key", "value")))
            val response = new AsyncHttpClient().put(request)
            Await.result(response, 300 milliseconds).getStatusCode should be (200)
        }
    }

    test("[HEAD] request with header should return response") {
        val dynamicResponse = { request: ServerRequest =>
            println(request.getHeader)
            if(request.getValue("toto") == "titi"  && request.getValue("key") == "value") StaticServerResponse(Text_Plain, "OK dynamic", 200)
            else StaticServerResponse(Text_Plain, "KO", 404)
        }

        val route = HEAD (
            path = "*",
            response = DynamicServerResponse(dynamicResponse)
        )

        withStubServerFixture(8080, route) { server =>
            val request = Request("http://localhost", server.portInUse, "/", headers = Seq(Header("toto", "titi"), Header("key", "value")))
            val response = new AsyncHttpClient().head(request)
            Await.result(response, 300 milliseconds).getStatusCode should be (200)
        }
    }

    test("[DELETE] request with header should return response") {
        val dynamicResponse = { request: ServerRequest =>
            println(request.getHeader)
            if(request.getValue("toto") == "titi"  && request.getValue("key") == "value") StaticServerResponse(Text_Plain, "OK dynamic", 200)
            else StaticServerResponse(Text_Plain, "KO", 404)
        }

        val route = DELETE (
            path = "*",
            response = DynamicServerResponse(dynamicResponse)
        )

        withStubServerFixture(8080, route) { server =>
            val request = Request("http://localhost", server.portInUse, "/", headers = Seq(Header("toto", "titi"), Header("key", "value")))
            val response = new AsyncHttpClient().delete(request)
            Await.result(response, 300 milliseconds).getStatusCode should be (200)
        }
    }
}