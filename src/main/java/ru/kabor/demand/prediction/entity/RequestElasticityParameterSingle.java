package ru.kabor.demand.prediction.entity;

import java.io.Serializable;

import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/** That class describes JSON request from admin mode for calculating elasticity for one shop and one product */
@XmlRootElement(name="requestElasticityParameterSingle")
@XmlAccessorType(javax.xml.bind.annotation.XmlAccessType.FIELD)
public class RequestElasticityParameterSingle implements Serializable {

	@Transient
	private static final long serialVersionUID = 982935960830220122L;
	@XmlElement
	private Integer requestId;
	@XmlElement
	private Integer whsId;
	@XmlElement
	private Integer artId;

	public RequestElasticityParameterSingle() {
		super();
	}

	public RequestElasticityParameterSingle(Integer requestId, Integer whsId, Integer artId) {
		super();
		this.requestId = requestId;
		this.whsId = whsId;
		this.artId = artId;
	}

	@Override
	public String toString() {
		return "RequestElasticityParameterSingle [requestId=" + requestId + ", whsId=" + whsId + ", artId=" + artId + "]";
	}

	public Integer getRequestId() {
		return requestId;
	}

	public void setRequestId(Integer requestId) {
		this.requestId = requestId;
	}

	public Integer getWhsId() {
		return whsId;
	}

	public void setWhsId(Integer whsId) {
		this.whsId = whsId;
	}

	public Integer getArtId() {
		return artId;
	}

	public void setArtId(Integer artId) {
		this.artId = artId;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
