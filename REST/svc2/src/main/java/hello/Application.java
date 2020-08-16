package hello;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.beans.factory.annotation.Value;

@SpringBootApplication
@RestController
public class Application {

	private static final Logger log = LoggerFactory.getLogger(Application.class);

	@Value("${DELAY_MS:10}")
	private long delayMs;

	@RequestMapping("/")
	public String home(@RequestBody String body) {
		log.info("Received request");
		/*
		try {
			Thread.sleep(delayMs);
		}
		catch (InterruptedException e) {
            System.out.println("Sleep get interrupted ");
		}
		*/
		log.info("Sending response");
		log.info("----------------------------");
		return body;
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}

