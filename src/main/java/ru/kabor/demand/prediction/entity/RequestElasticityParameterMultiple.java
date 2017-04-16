package ru.kabor.demand.prediction.entity;

import java.io.Serializable;

import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/** That class describes JSON request from admin mode for calculating elasticity for many shops and many products */
@XmlRootElement(name="RequestElasticityParameterMultiple")
@XmlAccessorType(javax.xml.bind.annotation.XmlAccessType.FIELD)
public class RequestElasticityParameterMultiple implements Serializable {

	@Transient
	private static final long serialVersionUID = 1115470581610276978L;
	@Transient
	@XmlElement
	private Integer requestId;
	@XmlElement
	private String whsIdBulk;
	@XmlElement
	private String artIdBulk;
	@XmlElement
	private Boolean isSendByEmail;
	@XmlElement
	private String email;

	public RequestElasticityParameterMultiple() {
		super();
	}

	public RequestElasticityParameterMultiple(Integer requestId, String whsIdBulk, String artIdBulk, Boolean isSendByEmail, String email) {
		super();
		this.requestId = requestId;
		this.whsIdBulk = whsIdBulk;
		this.artIdBulk = artIdBulk;
		this.isSendByEmail = isSendByEmail;
		this.email = email;
	}

	@Override
	public String toString() {
		return "RequestElasticityParameterMultiple [requestId=" + requestId + ", whsIdBulk=" + whsIdBulk + ", artIdBulk=" + artIdBulk + ", isSendByEmail=" + isSendByEmail
				+ ", email=" + email + "]";
	}

	public Integer getRequestId() {
		return requestId;
	}

	public void setRequestId(Integer requestId) {
		this.requestId = requestId;
	}

	public String getWhsIdBulk() {
		return whsIdBulk;
	}

	public void setWhsIdBulk(String whsIdBulk) {
		this.whsIdBulk = whsIdBulk;
	}

	public String getArtIdBulk() {
		return artIdBulk;
	}

	public void setArtIdBulk(String artIdBulk) {
		this.artIdBulk = artIdBulk;
	}

	public Boolean getIsSendByEmail() {
		return isSendByEmail;
	}

	public void setIsSendByEmail(Boolean isSendByEmail) {
		this.isSendByEmail = isSendByEmail;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
