package simplyscala

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import fr.simply.{StaticServerResponse, GET, StubServer}
import com.ning.http.client.{Response, ListenableFuture, AsyncHttpClient}
import fr.simply.util._


class HttpTest extends FunSuite with ShouldMatchers {
    test("test async http client api") {
        val route = GET (
            path = "/test",
            response = StaticServerResponse(Text_Plain, "yo man !", 200)
        )

        val server = new StubServer(8080, route).start

        val httpClient = new AsyncHttpClient()

        val mayBeResponse: ListenableFuture[Response] = httpClient.prepareGet("http://localhost:8080/test").execute()

        mayBeResponse.get().getStatusCode should be (200)

        server.stop
    }
}