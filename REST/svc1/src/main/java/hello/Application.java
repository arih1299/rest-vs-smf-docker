package hello;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

@SpringBootApplication
@RestController
public class Application {

	private static final Logger log = LoggerFactory.getLogger(Application.class);
	
	@Value("${BACKEND_URL:http://localhost:8082}")
	private String backendurl;

	@RequestMapping("/")
	public String home(@RequestBody String body) {
		log.info("Received request");
		ResponseEntity<String> response = new RestTemplate().exchange( backendurl,
			HttpMethod.POST, new HttpEntity(body, null), String.class);
		String backendres = response.getBody();

		log.info("Received answer: " + backendres);
		log.info("Sending response");
		log.info("----------------------------");
		return backendres;
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}

