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

	private JCSMPProperties properties = new JCSMPProperties();
	private JCSMPSession session;

	/** Anonymous inner-class for handling publishing events */
	@SuppressWarnings("unused")
	XMLMessageProducer producer;

	XMLMessageConsumer consumer;
	TextMessage request;
	FlowReceiver flow;

	final Topic topic = JCSMPFactory.onlyInstance().createTopic("demo/core/rest/req");
	final Queue replyQ = JCSMPFactory.onlyInstance().createQueue("REPLY_Q");

	//Time to wait for a reply before timing out
	final int timeoutMs = 1000;

	@RequestMapping("/")
	public String home(@RequestBody String body) {
		log.info("Received REST request");

		String respStr = "";

		try {
			request.setText(body);
			request.setReplyTo(replyQ);

//			BytesXMLMessage reply = requestor.request(request, timeoutMs, topic);
			producer.send(request, topic);

			BytesXMLMessage reply = flow.receive(timeoutMs);

			// Process the reply
			if (reply != null) {
				if (reply instanceof TextMessage) {
					respStr = ((TextMessage) reply).getText();
					System.out.printf("TextMessage response received: '%s'%n", respStr);
				}
				else if (reply instanceof StreamMessage) {
					respStr = ((StreamMessage) reply).dump();
					System.out.printf("StreamMessage response received: '%s'%n", respStr);
				}
				else {
					System.out.printf("Response Message Dump:%n%s%n",reply.dump());
				}
			}

			reply = null;

		} catch (JCSMPRequestTimeoutException e) {
			System.out.println("Failed to receive a reply in " + timeoutMs + " msecs");
		} catch (JCSMPException e) {
			System.out.println("JCSMPException: " + e.getMessage());
		}

		return respStr;
	}

	@PostConstruct
	private void init() throws JCSMPException {

		// Instantiates the SMF stuff at the initiation, reuse for each REST requests later
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

		consumer = session.getMessageConsumer((XMLMessageListener)null);
		consumer.start();
		request = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
		request.setDeliveryMode(DeliveryMode.PERSISTENT);
//		requestor = session.createRequestor();

		ConsumerFlowProperties flowProps = new ConsumerFlowProperties();
		flowProps.setEndpoint(replyQ);
		flow = session.createFlow(null, flowProps);
		flow.start();

	}


	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}

