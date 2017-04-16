package ru.kabor.demand.prediction.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** That class describes result of calculating elasticity for one shop and one product */
public class ResponseElasticity {

	private Integer whsId;
	private Integer artId;
	private String formula;
	/** parameters of the elasticity function*/
	private double[] functionParameters;
	/** error of the model*/
	private Double sigma;
	/** is all right*/
	private Boolean hasError = false;
	/** error message if not all right*/
	private String errorMessage = "";
	/** input information about time moments with calculated trand, seasonality and random*/
	private List<TimeMomentDescription> timeMoments = new ArrayList<>();

	public ResponseElasticity() {
		super();
	}

	public ResponseElasticity(Integer whsId, Integer artId) {
		super();
		this.whsId = whsId;
		this.artId = artId;
	}

	public String getPrettyFormula() {
		if (this.hasError || this.formula == null) {
			return "";
		}
		String[] formulaParts = this.formula.split(",");
		String rightPart = formulaParts[2];
		rightPart = rightPart.replaceAll("xdata", "(Δprice)");
		for (int i = 0; i < functionParameters.length; i++) {
			String argument = "p" + (i + 1);
			Double parameter = functionParameters[i];
			parameter = Math.floor(parameter * 100) / 100;

			rightPart = rightPart.replaceAll(argument, "(" + String.valueOf(parameter) + ")");
		}
		return rightPart;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
		if (this.errorMessage != null && !this.errorMessage.trim().equals("")) {
			this.hasError = true;
		} else {
			this.hasError = false;
		}
	}

	@Override
	public String toString() {
		return "ResponseElasticity [whsId=" + whsId + ", artId=" + artId + ", formula=" + formula + ", functionParameters=" + Arrays.toString(functionParameters) + ", sigma="
				+ sigma + ", hasError=" + hasError + ", errorMessage=" + errorMessage + ", timeMoments=" + timeMoments + "]";
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

	public String getFormula() {
		return formula;
	}

	public void setFormula(String formula) {
		this.formula = formula;
	}

	public double[] getFunctionParameters() {
		return functionParameters;
	}

	public void setFunctionParameters(double[] functionParameters) {
		this.functionParameters = functionParameters;
	}

	public Boolean getHasError() {
		return hasError;
	}

	public void setHasError(Boolean hasError) {
		this.hasError = hasError;
	}

	public Double getSigma() {
		return sigma;
	}

	public void setSigma(Double sigma) {
		this.sigma = sigma;
	}
	
	public List<TimeMomentDescription> getTimeMoments() {
		return timeMoments;
	}

	public void setTimeMoments(List<TimeMomentDescription> timeMoments) {
		this.timeMoments = timeMoments;
	}
}
