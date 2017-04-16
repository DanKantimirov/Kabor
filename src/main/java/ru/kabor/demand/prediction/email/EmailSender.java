package ru.kabor.demand.prediction.email;

import java.util.Date;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.PostConstruct;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ru.kabor.demand.prediction.repository.DataRepository;

/** It send email message to client */
@Component
@Scope("singleton")
public class EmailSender {
	
	@Autowired
	DataRepository dataRepository;
	
	@Autowired
	EmailBodyCreator emailBodyCreator;
	
	private static final Logger LOG = LoggerFactory.getLogger(EmailSender.class);

	@Value("${email.host}")
	private String host;
	@Value("${email.port}")
	private String port;
	@Value("${email.login}")
	private String login;
	@Value("${email.loginSuffix}")
	private String loginSuffix;
	@Value("${email.password}")
	private String password;
	@Value("${email.useSSL}")
	private String useSSL;
	@Value("${contact.email}")
	private String contactEmail;
	private String fullLogin;
	private Session session;

	public EmailSender() {
		super();
	}

	/** Inits parameters
	 * @throws EmailSenderException
	 */
	@PostConstruct
	private void init() throws EmailSenderException {
		if (this.loginSuffix != null && !this.loginSuffix.trim().equals("")) {
			this.fullLogin = this.login +"@" +this.loginSuffix;
		} else {
			this.fullLogin = this.login;
		}
		
		Callable<Session> gettingSessionTask = () -> {return this.createSSLSession();};
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		try{
			Future<Session> futureSession = executorService.submit(gettingSessionTask);
			this.session = futureSession.get(30, TimeUnit.SECONDS);
		} catch(TimeoutException|ExecutionException|InterruptedException e){
			LOG.error("Can't initializa session to post server.", e);
			throw new EmailSenderException("Can't initializa session to post server");
		} finally{
			executorService.shutdownNow();
		}
		if(this.session==null){
			LOG.error("Can not get Email session");
			throw new EmailSenderException("Can not get Email session");
		}
	}

	/** Create simple Session*/
	private Session createSSLSession() {
		Session session;
		Properties props = new Properties();
		props.put("mail.smtp.host", this.host);
		props.put("mail.smtp.port", this.port);
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		
		if(this.useSSL!=null && this.useSSL.toLowerCase().equals("true")){
			props.put("mail.smtp.socketFactory.port", this.port);
			props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		}

		session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(fullLogin, password);
			}
		});
		
		return session;
	}

	
	/** Send user comment to our email
	 * @param firstname firstname
	 * @param lastname lastname
	 * @param clientEmail clientEmail
	 * @param comments comment
	 */
	public void sendContactEmail(String firstname, String lastname, String clientEmail, String comments) {
		try {
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(this.fullLogin));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(contactEmail));
			String fromString = firstname + " " + lastname + " " + clientEmail + "\n";
			message.setSubject("Need forecast new comment from " + firstname + " " + lastname);
			message.setSentDate(new Date());
			message.setContent(fromString + comments, "text/html");
			Transport.send(message);
		} catch (AddressException e) {
			LOG.error("Can't send email.", e);
		} catch (MessagingException e) {
			LOG.error("Can't send email.", e);
		}
	}
	
	/** Send message to user that we got user's request
	 * @param requestId id of request
	 * @throws EmailSenderException
	 * @throws MessagingException
	 */
	public void sendMessageRequestAdded(Long requestId) throws EmailSenderException, MessagingException {
		EmailMessageParameters emailMessageParameters = this.emailBodyCreator.getMessageRequestAddedText(requestId);
		String userEmail = emailMessageParameters.getEmail();
		String messageBody = emailMessageParameters.getMessageBody();

		if (userEmail == null || userEmail.trim().equals("")) {
			throw new EmailSenderException("Empty email address. RequestId:" + requestId);
		}

		if (messageBody == null || messageBody.trim().equals("")) {
			throw new EmailSenderException("Empty message body. RequestId:" + requestId);
		}

		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress(this.fullLogin));
		message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(userEmail));
		message.setSubject("Request " + requestId.toString() + " has been added to execution queue");
		message.setSentDate(new Date());
		message.setContent(messageBody, "text/html");
		Transport.send(message);
	}	
	
	/** Send message to user that forecast has been successfully fulfilled
	 * @param requestId id of request
	 * @throws AddressException
	 * @throws MessagingException
	 * @throws EmailSenderException
	 */
	public void sendMessageWithForecastResult(Long requestId)
			throws AddressException, MessagingException, EmailSenderException {
		EmailMessageParameters emailMessageParameters = this.emailBodyCreator.getMessageWithForecastResultText(requestId);
		String userEmail = emailMessageParameters.getEmail();
		String messageBody = emailMessageParameters.getMessageBody();
		String attachmentPath = emailMessageParameters.getAttachmentLink();

		if (userEmail == null || userEmail.trim().equals("")) {
			throw new EmailSenderException("Empty email address. RequestId:" + requestId);
		}

		if (messageBody == null || messageBody.trim().equals("")) {
			throw new EmailSenderException("Empty message body. RequestId:" + requestId);
		}

		if (attachmentPath == null || attachmentPath.trim().equals("")) {
			throw new EmailSenderException("Empty attachmentPath. RequestId:" + requestId);
		}

		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress(this.fullLogin));
		message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(userEmail));
		message.setSubject("Request " + requestId.toString() + " has been successfully fulfilled");
		message.setSentDate(new Date());
		message.setContent(messageBody, "text/html");
		Transport.send(message);
	};
	
	/** Send message to user that elasticity has been successfully calculated
	 * @param requestId id of request
	 * @throws AddressException
	 * @throws MessagingException
	 * @throws EmailSenderException
	 */
	public void sendMessageWithElasticityResult(Long requestId)
			throws AddressException, MessagingException, EmailSenderException {
		EmailMessageParameters emailMessageParameters = this.emailBodyCreator.getMessageWithElasticityResultText(requestId);
		String userEmail = emailMessageParameters.getEmail();
		String messageBody = emailMessageParameters.getMessageBody();
		String attachmentPath = emailMessageParameters.getAttachmentLink();

		if (userEmail == null || userEmail.trim().equals("")) {
			throw new EmailSenderException("Empty email address. RequestId:" + requestId);
		}

		if (messageBody == null || messageBody.trim().equals("")) {
			throw new EmailSenderException("Empty message body. RequestId:" + requestId);
		}

		if (attachmentPath == null || attachmentPath.trim().equals("")) {
			throw new EmailSenderException("Empty attachmentPath. RequestId:" + requestId);
		}

		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress(this.fullLogin));
		message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(userEmail));
		message.setSubject("Request " + requestId.toString() + " has been successfully fulfilled");
		message.setSentDate(new Date());
		message.setContent(messageBody, "text/html");
		Transport.send(message);
	};
	
	
	/** Send message to user that elasticity has been successfully calculated (database mode)
	 * @param requestId id of request
	 * @param userEmail email of user
	 * @param filePath path to result file
	 * @throws AddressException
	 * @throws MessagingException
	 * @throws EmailSenderException
	 */
	public void sendMessageWithElasticityResult (Long requestId, String userEmail, String filePath) throws AddressException, MessagingException, EmailSenderException{
	
		EmailMessageParameters emailMessageParameters = this.emailBodyCreator.getMessageWithElasticityResultText(requestId, userEmail, filePath);
		String messageBody = emailMessageParameters.getMessageBody();
		String attachmentPath = emailMessageParameters.getAttachmentLink();

		if (userEmail == null || userEmail.trim().equals("")) {
			throw new EmailSenderException("Empty email address. RequestId:" + requestId);
		}

		if (messageBody == null || messageBody.trim().equals("")) {
			throw new EmailSenderException("Empty message body. RequestId:" + requestId);
		}

		if (attachmentPath == null || attachmentPath.trim().equals("")) {
			throw new EmailSenderException("Empty attachmentPath. RequestId:" + requestId);
		}

		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress(this.fullLogin));
		message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(userEmail));
		message.setSubject("Request " + requestId.toString() + " has been successfully fulfilled");
		message.setSentDate(new Date());
		message.setContent(messageBody, "text/html");
		Transport.send(message);
		
	};
	

	/** Send message to user that exception occurred
	 * @param requestId id of request
	 * @throws AddressException
	 * @throws MessagingException
	 * @throws EmailSenderException
	 */
	public void sendMessageWithError(Long requestId) throws AddressException, MessagingException, EmailSenderException {
		EmailMessageParameters emailMessageParameters = this.emailBodyCreator.getMessageWithErrorText(requestId);
		String userEmail = emailMessageParameters.getEmail();
		String messageBody = emailMessageParameters.getMessageBody();

		if (userEmail == null || userEmail.trim().equals("")) {
			throw new EmailSenderException("Empty email address. RequestId:" + requestId);
		}

		if (messageBody == null || messageBody.trim().equals("")) {
			throw new EmailSenderException("Empty message body. RequestId:" + requestId);
		}

		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress(this.fullLogin));
		message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(userEmail));
		message.setSubject("Request " + requestId.toString() + " has crashed");
		message.setSentDate(new Date());
		message.setContent(messageBody, "text/html");
		Transport.send(message);
	};
	
	/** Send message to user that exception occurred
	 * @param requestId id of request
	 * @param errorMessage message with error
	 * @throws AddressException
	 * @throws MessagingException
	 * @throws EmailSenderException
	 */
	public void sendMessageWithError(Long requestId, String errorMessage)
			throws AddressException, MessagingException, EmailSenderException {
		EmailMessageParameters emailMessageParameters = this.emailBodyCreator.getMessageWithErrorText(requestId,
				errorMessage);
		String userEmail = emailMessageParameters.getEmail();
		String messageBody = emailMessageParameters.getMessageBody();

		if (userEmail == null || userEmail.trim().equals("")) {
			throw new EmailSenderException("Empty email address. RequestId:" + requestId);
		}

		if (messageBody == null || messageBody.trim().equals("")) {
			throw new EmailSenderException("Empty message body. RequestId:" + requestId);
		}

		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress(this.fullLogin));
		message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(userEmail));
		message.setSubject("Request " + requestId.toString() + " has crashed");
		message.setSentDate(new Date());
		message.setContent(messageBody, "text/html");
		Transport.send(message);
	}
}
