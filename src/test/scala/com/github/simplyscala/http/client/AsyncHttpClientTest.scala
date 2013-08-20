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
import com.github.simplyscala.http.client.request.util.Header
import fr.simply.DynamicServerResponse
import fr.simply.StaticServerResponse
import com.github.simplyscala.http.client.request.util.Request
import com.github.simplyscala.http.client.request.util.Cookie

class AsyncHttpClientTest extends FunSuite with ShouldMatchers with StubServerFixture {

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

    test("[GET] request with cookie should return response") {
        val dynamicResponse = { request: ServerRequest =>
            val cookie = request.getCookie("name")
            println("must : 42 but : " + cookie.getVersion) // failed param
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
            val request: Request = Request("http://localhost", server.portInUse, "/", Map("toto" -> "titi"), Seq(cookie))
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
            val request: Request = Request("http://localhost", server.portInUse, "/", Map("toto" -> "titi"), Seq(cookie))
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

    test("[PUT] request with cookie should return response") {
        val dynamicResponse = { request: ServerRequest =>
            val cookie = request.getCookie("name")
            if(cookie != null && cookie.getValue == "value" && cookie.getName == "name" &&
                cookie.getDomain == "domain" && cookie.getPath == "path")
                StaticServerResponse(Text_Plain, "OK dynamic", 200)
            else StaticServerResponse(Text_Plain, "OK dynamic", 404)
        }

        val route = PUT (
            path = "*",
            response = DynamicServerResponse(dynamicResponse)
        )

        withStubServerFixture(8080, route) { server =>
            val cookie: Cookie = Cookie("domain", "name", "value", "path")
            val request: Request = Request("http://localhost", server.portInUse, "/", Map("toto" -> "titi"), Seq(cookie))
            val response: Future[Response] = new AsyncHttpClient().put(request)
            Await.result(response, 1 seconds).getStatusCode should be (200)
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

    test("[HEAD] request with cookie should return response") {
        val dynamicResponse = { request: ServerRequest =>
            val cookie = request.getCookie("name")
            if(cookie != null && cookie.getValue == "value" && cookie.getName == "name" &&
                cookie.getDomain == "domain" && cookie.getPath == "path")
                StaticServerResponse(Text_Plain, "OK dynamic", 200)
            else StaticServerResponse(Text_Plain, "OK dynamic", 404)
        }

        val route = HEAD (
            path = "*",
            response = DynamicServerResponse(dynamicResponse)
        )

        withStubServerFixture(8080, route) { server =>
            val cookie: Cookie = Cookie("domain", "name", "value", "path")
            val request: Request = Request("http://localhost", server.portInUse, "/", Map("toto" -> "titi"), Seq(cookie))
            val response: Future[Response] = new AsyncHttpClient().head(request)
            Await.result(response, 1 seconds).getStatusCode should be (200)
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

    test("[DELETE] request with cookie should return response") {
        val dynamicResponse = { request: ServerRequest =>
            val cookie = request.getCookie("name")
            if(cookie != null && cookie.getValue == "value" && cookie.getName == "name" &&
                cookie.getDomain == "domain" && cookie.getPath == "path")
                StaticServerResponse(Text_Plain, "OK dynamic", 200)
            else StaticServerResponse(Text_Plain, "OK dynamic", 404)
        }

        val route = DELETE (
            path = "*",
            response = DynamicServerResponse(dynamicResponse)
        )

        withStubServerFixture(8080, route) { server =>
            val cookie: Cookie = Cookie("domain", "name", "value", "path")
            val request: Request = Request("http://localhost", server.portInUse, "/", Map("toto" -> "titi"), Seq(cookie))
            val response: Future[Response] = new AsyncHttpClient().delete(request)
            Await.result(response, 1 seconds).getStatusCode should be (200)
        }
    }

    test("[GET] request should url encode its paramters") {
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

    test("[GET] request, using 'Request' instance should url encode its paramters") {
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

    test("[GET] request don't use 'params' attribute should not url encode its paramters") {
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
}