package controller;

import com.smaato.smaato.AmazonKinesisConfigTest;
import com.smaato.smaato.controller.APIController;
import com.smaato.smaato.service.KinesisService;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = APIController.class)
@Import({KinesisService.class, AmazonKinesisConfigTest.class})
@Slf4j
@ActiveProfiles("test")
public class SmaatoControllerTest {

    @Autowired
    private WebTestClient webClient;

    @SpyBean
    APIController apiController;

    @SneakyThrows
    @Test
    void shouldReturnOkWhenIdOnlyPresent() {

        webClient.get()
                .uri("/api/smaato/accept?id=17")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody().consumeWith(res -> new String(Objects.requireNonNull(res.getResponseBody())).equals("ok"));

        assertThat(apiController.getRequestsCounter().longValue()).isEqualTo(1);

    }

    @SneakyThrows
    @Test
    void shouldReturnOkWhenEndpointParamPresentAndNotFailing() {
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setResponseCode(200));
        server.start();

        webClient.get()
                .uri("/api/smaato/accept?id=17&endpoint=http://"+server.getHostName())
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody().consumeWith(res -> new String(Objects.requireNonNull(res.getResponseBody())).equals("ok"));

        assertThat(apiController.getRequestsCounter().longValue()).isEqualTo(1);
        server.shutdown();

    }

    @SneakyThrows
    @Test
    void shouldReturnFailWhenEndpointParamPresentAndFailing() {
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setResponseCode(400));
        server.start();

        webClient.get()
                .uri("/api/smaato/accept?id=17&endpoint=http://"+server.getHostName())
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody().consumeWith(res -> new String(Objects.requireNonNull(res.getResponseBody())).equals("fail"));

        assertThat(apiController.getRequestsCounter().longValue()).isEqualTo(1);
        server.shutdown();

    }

    @AfterEach
    void shutDown() {
        apiController.getRequestsCounter();
    }

}
