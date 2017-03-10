package ru.kabor.demand.prediction.entity;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "v_sales_rest")
public class SalesRest {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@Column(name = "whs_id", nullable = false)
	private int whsId;

	@Column(name = "art_id", nullable = false)
	private int artId;

	@Column(name = "day_id", nullable = false)
	private LocalDate dayId;

	@Column(name = "sale_qnty")
	private double saleQnty;

	@Column(name = "rest_qnty")
	private double restQnty;

	@ManyToOne(optional = false)
	@JoinColumn(name = "request_id", nullable = false)
	private Request request;

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
		SalesRest other = (SalesRest) obj;
		if (id != other.id)
			return false;
		return true;
	}

	public SalesRest() {
		super();
	}

	public SalesRest(int id, int whsId, int artId, LocalDate dayId, double saleQnty, double restQnty, Request request) {
		super();
		this.id = id;
		this.whsId = whsId;
		this.artId = artId;
		this.dayId = dayId;
		this.saleQnty = saleQnty;
		this.restQnty = restQnty;
		this.request = request;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getWhsId() {
		return whsId;
	}

	public void setWhsId(int whsId) {
		this.whsId = whsId;
	}

	public int getArtId() {
		return artId;
	}

	public void setArtId(int artId) {
		this.artId = artId;
	}

	public LocalDate getDayId() {
		return dayId;
	}

	public void setDayId(LocalDate dayId) {
		this.dayId = dayId;
	}

	public double getSaleQnty() {
		return saleQnty;
	}

	public void setSaleQnty(double saleQnty) {
		this.saleQnty = saleQnty;
	}

	public double getRestQnty() {
		return restQnty;
	}

	public void setRestQnty(double restQnty) {
		this.restQnty = restQnty;
	}

	public Request getRequest() {
		return request;
	}

	public void setRequest(Request request) {
		this.request = request;
	}

}
