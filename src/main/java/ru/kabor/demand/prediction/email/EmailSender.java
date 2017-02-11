package ru.kabor.demand.prediction.email;

import javax.annotation.PostConstruct;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import ru.kabor.demand.prediction.repository.DataRepository;

/** Class for sending Email messages to client*/
@Component
public class EmailSender {
	
	@Autowired
	DataRepository dataRepository;

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
	private String fullLogin;
	private Session session;

	public EmailSender() {
		super();
	}

	@PostConstruct
	private void init() throws EmailSenderException {
		
		if (this.loginSuffix != null && !this.loginSuffix.trim().equals("")) {
			this.fullLogin = this.login +"@" +this.loginSuffix;
		} else {
			this.fullLogin = this.login;
		}
		
		this.session = createSSLSession();
		if(this.session==null){
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
	
	/** Send message with link to forecast demand
	 * @param requestId v_request.requestId*/
	public void sendMessageWithResult(Long requestId) throws AddressException, MessagingException {
		String userEmail = this.dataRepository.getEmailByRequestId(requestId);
		if (userEmail != null) {
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(this.fullLogin));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(userEmail)); 
			message.setSubject("Your demand forecast");												//TODO: make good text
			message.setText("Test with result");
			Transport.send(message);
		}
	};

	/** Send message with error message. Error message will be taken from v_request.response_text 
	 *  @param requestId v_request.requestId*/
	public void sendMessageWithError(Long requestId) throws AddressException, MessagingException {
		String userEmail = this.dataRepository.getEmailByRequestId(requestId);
		String responseTest = this.dataRepository.getResponseTextByRequestId(requestId);
		if (userEmail != null && responseTest != null) {
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(this.fullLogin));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(userEmail));
			message.setSubject("Demand forecast error");											//TODO: make good text
			message.setText(responseTest);
			Transport.send(message);
		}
	};
	
	/** Send message with error message. Error message will be taken from parameter 
	 *  @param requestId v_request.requestId
	 *  @param errorMessage message with error*/
	public void sendMessageWithError(Long requestId, String errorMessage) throws AddressException, MessagingException{
		String userEmail = this.dataRepository.getEmailByRequestId(requestId);
		if (userEmail != null) {
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(this.fullLogin));
			message.setRecipients(Message.RecipientType.TO,InternetAddress.parse(userEmail));
			message.setSubject("Demand forecast errror");											//TODO: make good text
			message.setText(errorMessage);
			Transport.send(message);
		}
	};

}
