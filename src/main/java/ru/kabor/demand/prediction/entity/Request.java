package ru.kabor.demand.prediction.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.validator.constraints.Email;


@Entity
@Table(name = "v_request")
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "request_id")
    private int id;

    @Column(length = 45, nullable = false)
    @Email
    private String email;

    @Column(name = "send_date_time", nullable = false)
    private LocalDateTime sendDateTime;

    @Column(nullable = false)
    private int status;

    @Column(name = "response_text", length = 500)
    private String responseText;

    @Column(name = "attachment_path", length = 150)
    private String attachmentPath;

    @Column(name = "document_path", length = 150, nullable = false)
    private String documentPath;

    @OneToMany(mappedBy = "request", targetEntity = SalesRest.class, cascade = CascadeType.ALL)
    Set<SalesRest> salesRest = new HashSet<>();

    @OneToOne(mappedBy = "request", targetEntity = ForecastParameter.class, cascade = CascadeType.ALL)
    ForecastParameter forecastParameter;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Request other = (Request) obj;
		if (id != other.id)
			return false;
		return true;
	}

	public Request() {
		super();
	}

	public Request(int id, String email, LocalDateTime sendDateTime, int status, String responseText,
			String attachmentPath, String documentPath, Set<SalesRest> salesRest, ForecastParameter forecastParameter) {
		super();
		this.id = id;
		this.email = email;
		this.sendDateTime = sendDateTime;
		this.status = status;
		this.responseText = responseText;
		this.attachmentPath = attachmentPath;
		this.documentPath = documentPath;
		this.salesRest = salesRest;
		this.forecastParameter = forecastParameter;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public LocalDateTime getSendDateTime() {
		return sendDateTime;
	}

	public void setSendDateTime(LocalDateTime sendDateTime) {
		this.sendDateTime = sendDateTime;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getResponseText() {
		return responseText;
	}

	public void setResponseText(String responseText) {
		this.responseText = responseText;
	}

	public String getAttachmentPath() {
		return attachmentPath;
	}

	public void setAttachmentPath(String attachmentPath) {
		this.attachmentPath = attachmentPath;
	}

	public String getDocumentPath() {
		return documentPath;
	}

	public void setDocumentPath(String documentPath) {
		this.documentPath = documentPath;
	}

	public Set<SalesRest> getSalesRest() {
		return salesRest;
	}

	public void setSalesRest(Set<SalesRest> salesRest) {
		this.salesRest = salesRest;
	}

	public ForecastParameter getForecastParameter() {
		return forecastParameter;
	}

	public void setForecastParameter(ForecastParameter forecastParameter) {
		this.forecastParameter = forecastParameter;
	}
    
    
}
