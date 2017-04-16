package ru.kabor.demand.prediction.entity;

/** That class describes difference between sales in two time moments with different prices. */
public class SalesAndPriceDeviation {
	/** new_price - old_price (not module) */
	Double priceDeviatin;
	/** new_sales - old_sales (not module)*/
	Double salesDeviation;

	public SalesAndPriceDeviation(Double priceDeviatin, Double salesDeviation) {
		super();
		this.priceDeviatin = priceDeviatin;
		this.salesDeviation = salesDeviation;
	}

	@Override
	public String toString() {
		return "SalesAndPriceDeviation [priceDeviatin=" + priceDeviatin + ", salesDeviation=" + salesDeviation + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((priceDeviatin == null) ? 0 : priceDeviatin.hashCode());
		result = prime * result + ((salesDeviation == null) ? 0 : salesDeviation.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof SalesAndPriceDeviation))
			return false;
		SalesAndPriceDeviation other = (SalesAndPriceDeviation) obj;
		if (priceDeviatin == null) {
			if (other.priceDeviatin != null)
				return false;
		} else if (!priceDeviatin.equals(other.priceDeviatin))
			return false;
		if (salesDeviation == null) {
			if (other.salesDeviation != null)
				return false;
		} else if (!salesDeviation.equals(other.salesDeviation))
			return false;
		return true;
	}

	public Double getPriceDeviatin() {
		return priceDeviatin;
	}

	public void setPriceDeviatin(Double priceDeviatin) {
		this.priceDeviatin = priceDeviatin;
	}

	public Double getSalesDeviation() {
		return salesDeviation;
	}

	public void setSalesDeviation(Double salesDeviation) {
		this.salesDeviation = salesDeviation;
	}
}
