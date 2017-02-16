package ru.kabor.demand.prediction.email;

/** Parameter for building email*/
public class EmailMessageParameters {
	/** Body of email*/
	String messageBody;
	/** Address*/
	String email;
	/** Link to attachment*/
	String attachmentLink;

	public EmailMessageParameters(String messageBody, String email, String attachmentLink) {
		super();
		this.messageBody = messageBody;
		this.email = email;
		this.attachmentLink = attachmentLink;
	}

	public String getMessageBody() {
		return messageBody;
	}

	public void setMessageBody(String messageBody) {
		this.messageBody = messageBody;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getAttachmentLink() {
		return attachmentLink;
	}

	public void setAttachmentLink(String attachmentLink) {
		this.attachmentLink = attachmentLink;
	}

}
