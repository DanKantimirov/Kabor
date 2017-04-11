package ru.kabor.demand.prediction.entity;

import javax.persistence.*;

/** It represents parameters for calculating elasticity (from database) */
@Entity
@Table(name = "v_elasticity_parameter")
public class ElasticityParameter {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@OneToOne(optional = false)
	@JoinColumn(name = "request_id", nullable = false)
	private Request request;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Request getRequest() {
		return request;
	}

	public void setRequest(Request request) {
		this.request = request;
	}

}
