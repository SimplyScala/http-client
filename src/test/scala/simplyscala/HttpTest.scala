package simplyscala

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import fr.simply.{ServerResponse, GET, StubServer}
import com.ning.http.client.{Response, ListenableFuture, AsyncHttpClient}


class HttpTest extends FunSuite with ShouldMatchers {
    test("test async http client api") {
        val route = GET (
            path = "/test",
            response = ServerResponse("text/plain", "yo man !", 200)
        )

        val server = new StubServer(8080, route).start

        val httpClient = new AsyncHttpClient()

        val mayBeResponse: ListenableFuture[Response] = httpClient.prepareGet("http://localhost:8080/test").execute()

        mayBeResponse.get().getStatusCode should be (200)

        server.stop
    }
}