package ru.kabor.demand.prediction.email;

import java.text.MessageFormat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ru.kabor.demand.prediction.repository.DataRepository;

/** It creates email body for sending information about request state*/
@Component
@Scope("singleton")
public class EmailBodyCreator {
	
	@Autowired
	DataRepository dataRepository;

	/** Create email parameter when user just sended request to service
	 * @param requestId id of request
	 * @return parameter of email for user
	 */
	public EmailMessageParameters getMessageRequestAddedText(Long requestId){
		StringBuilder messageBodyBuilder = new StringBuilder("");
		String userEmail = this.dataRepository.getEmailByRequestId(requestId);

		messageBodyBuilder.append("<p>Dear {0}.</p>");
		messageBodyBuilder.append("<p>We have already begun to fulfill your request #{1}.</p>");
		messageBodyBuilder.append("<p>When the execution is completed, we will send you an email notification.</p>");

		String messageBody = MessageFormat.format(messageBodyBuilder.toString(), userEmail,requestId.toString());

		EmailMessageParameters emailMessageParameters = new EmailMessageParameters(messageBody, userEmail, null);
		return emailMessageParameters;
	}

	/** Create email parameter when server finished making forecast
	 * @param requestId id of request
	 * @return parameter of email for user
	 */
	public EmailMessageParameters getMessageWithForecastResultText(Long requestId){
		StringBuilder messageBodyBuilder = new StringBuilder("");
		String userEmail = this.dataRepository.getEmailByRequestId(requestId);
		String filePath = this.dataRepository.getAttachmentPathByRequestId(requestId);
		
		messageBodyBuilder.append("<p>Dear {0}.</p>");
		messageBodyBuilder.append("<p>Your request {1} has been successfully fulfilled!</p>");
		messageBodyBuilder.append("<p>You can download forecast at <a href=\"{2}\">Demand Forecast</a>  .</p>");
		messageBodyBuilder.append("<p>That link will be available in 48 hours.</p>");
		
		String messageBody = MessageFormat.format(messageBodyBuilder.toString(), userEmail,requestId.toString(),filePath);
		
		EmailMessageParameters emailMessageParameters = new EmailMessageParameters(messageBody, userEmail, filePath);
		return emailMessageParameters;
	}
	
	/** Create email parameter when server finished calculating elasticity
	 * @param requestId id of request
	 * @return parameter of email for user
	 */
	public EmailMessageParameters getMessageWithElasticityResultText(Long requestId){
		StringBuilder messageBodyBuilder = new StringBuilder("");
		String userEmail = this.dataRepository.getEmailByRequestId(requestId);
		String filePath = this.dataRepository.getAttachmentPathByRequestId(requestId);
		
		messageBodyBuilder.append("<p>Dear {0}.</p>");
		messageBodyBuilder.append("<p>Your request {1} has been successfully fulfilled!</p>");
		messageBodyBuilder.append("<p>You can download elasticity calculation at <a href=\"{2}\">Elasticity calculation</a>  .</p>");
		messageBodyBuilder.append("<p>That link will be available in 48 hours.</p>");
		
		String messageBody = MessageFormat.format(messageBodyBuilder.toString(), userEmail,requestId.toString(),filePath);
		
		EmailMessageParameters emailMessageParameters = new EmailMessageParameters(messageBody, userEmail, filePath);
		return emailMessageParameters;
	}
	
	/** Create email parameter when server finished calculating elasticity
	 * @param requestId id of request
	 * @param userEmail email where to send
	 * @param filePath path to attachment file 
	 * @return parameter of email for user
	 */
	public EmailMessageParameters getMessageWithElasticityResultText(Long requestId, String userEmail, String filePath){
		StringBuilder messageBodyBuilder = new StringBuilder("");
		
		messageBodyBuilder.append("<p>Dear {0}.</p>");
		messageBodyBuilder.append("<p>Your request {1} has been successfully fulfilled!</p>");
		messageBodyBuilder.append("<p>You can download elasticity calculation at <a href=\"{2}\">Elasticity calculation</a>  .</p>");
		messageBodyBuilder.append("<p>That link will be available in 48 hours.</p>");
		
		String messageBody = MessageFormat.format(messageBodyBuilder.toString(), userEmail,requestId.toString(),filePath);
		
		EmailMessageParameters emailMessageParameters = new EmailMessageParameters(messageBody, userEmail, filePath);
		return emailMessageParameters;
	}
	
	/** Create email parameter when occurred  exception
	 * @param requestId id of request
	 * @return parameter of email for user
	 */
	public EmailMessageParameters getMessageWithErrorText(Long requestId){
		StringBuilder messageBodyBuilder = new StringBuilder("");
		String userEmail = this.dataRepository.getEmailByRequestId(requestId);
		String errorMessage = this.dataRepository.getResponseTextByRequestId(requestId);
		
		messageBodyBuilder.append("<p>Dear {0}.</p>");
		messageBodyBuilder.append("<p>We couldn’t make prediction by request {1}.</p>");
		messageBodyBuilder.append("<p>Here is a description of error: {2}.</p>");
		
		String messageBody = MessageFormat.format(messageBodyBuilder.toString(), userEmail, requestId.toString(), errorMessage);
		
		EmailMessageParameters emailMessageParameters = new EmailMessageParameters(messageBody, userEmail, null);
		return emailMessageParameters;
	}
	
	/** Create email parameter when occurred  exception
	 * @param requestId id of request
	 * @param errorMessage error message
	 * @return parameter of email for user
	 */
	public EmailMessageParameters getMessageWithErrorText(Long requestId, String errorMessage){
		StringBuilder messageBodyBuilder = new StringBuilder("");
		String userEmail = this.dataRepository.getEmailByRequestId(requestId);
		
		messageBodyBuilder.append("<p>Dear {0}.</p>");
		messageBodyBuilder.append("<p>We couldn’t make prediction by request {1}.</p>");
		messageBodyBuilder.append("<p>Here is a description of error: {2}.</p>");
		
		String messageBody = MessageFormat.format(messageBodyBuilder.toString(), userEmail, requestId.toString(), errorMessage);
		
		EmailMessageParameters emailMessageParameters = new EmailMessageParameters(messageBody, userEmail, null);
		return emailMessageParameters;
	}
}
