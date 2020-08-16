package hello;

import com.solacesystems.jcsmp.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.IOException;

@SpringBootApplication
@RestController
public class Application {

	private static final Logger log = LoggerFactory.getLogger(Application.class);
	
	@Value("${SOL_URL:tcp://localhost:55555}")
	private String solurl;

	@Value("${SOL_USER:default}")
	private String username;

	@Value("${SOL_PASS:default}")
	private String password;

	@Value("${SOL_VPN:default}")
	private String vpn;

	@Value("${DELAY_MS:10}")
	private long delayMs;

	private JCSMPProperties properties = new JCSMPProperties();
	private JCSMPSession session;

	/** Anonymous inner-class for handling publishing events */
	@SuppressWarnings("unused")
	XMLMessageProducer producer;

	XMLMessageConsumer consumer;
	TextMessage request;
	Requestor requestor;
	FlowReceiver flow;

	final Topic topic = JCSMPFactory.onlyInstance().createTopic("demo/core/rest/req");


	@PostConstruct
	private void init() throws JCSMPException {

		// Solace parts
		// Create a JCSMP Session
		properties.setProperty(JCSMPProperties.HOST, solurl);     // host:port
		properties.setProperty(JCSMPProperties.USERNAME, username); // client-username
		properties.setProperty(JCSMPProperties.VPN_NAME,  vpn); // message-vpn
		properties.setProperty(JCSMPProperties.PASSWORD, password); // client-password
		session =  JCSMPFactory.onlyInstance().createSession(properties);
		session.connect();

		//This will have the session create the producer and consumer required
		//by the Requestor used below.

		producer = session.getMessageProducer(new JCSMPStreamingPublishEventHandler() {
			@Override
			public void responseReceived(String messageID) {
				System.out.println("Producer received response for msg: " + messageID);
			}
			@Override
			public void handleError(String messageID, JCSMPException e, long timestamp) {
				System.out.printf("Producer received error for msg: %s@%s - %s%n",
						messageID,timestamp,e);
			}
		});


		/** Anonymous inner-class for request handling **/
		final XMLMessageConsumer cons = session.getMessageConsumer(new XMLMessageListener() {
			@Override
			public void onReceive(BytesXMLMessage request) {

				if (request.getReplyTo() != null) {
					System.out.println("Received request");
					TextMessage reply = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
					Destination replyDest = request.getReplyTo();

					final String text = "Sample response";
					reply.setText(text);

					try {
						// Thread.sleep(delayMs);
                        producer.sendReply(request, reply);
						System.out.println("Sending response: " + text);
					} catch (JCSMPException e) { //| InterruptedException e) {
						System.out.println("Error sending reply.");
						e.printStackTrace();
					}
				} else {
					System.out.println("Received message without reply-to field");
				}

			}

			public void onException(JCSMPException e) {
				System.out.printf("Consumer received exception: %s%n", e);
			}
		});

		session.addSubscription(topic);
		cons.start();
	}


	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}

