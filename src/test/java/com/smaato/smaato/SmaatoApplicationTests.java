package com.smaato.smaato;

import com.smaato.smaato.service.KinesisService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class SmaatoApplicationTests {

	@MockBean
	KinesisService kinesisService;

	@Test
	void contextLoads() {
	}

}
