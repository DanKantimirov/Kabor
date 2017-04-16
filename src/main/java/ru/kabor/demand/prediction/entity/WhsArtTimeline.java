package ru.kabor.demand.prediction.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/** It represents information about sales, rests and prices for one shop and one product for all periods of time. */
public class WhsArtTimeline {
	private Integer whsId;
	private Integer artId;
	/** all periods of time*/
	private List<TimeMomentDescription> timeMoments = new ArrayList<>();

	public WhsArtTimeline() {
		super();
	}

	public WhsArtTimeline(Integer whsId, Integer artId) {
		super();
		this.whsId = whsId;
		this.artId = artId;
	}

	public WhsArtTimeline(Integer whsId, Integer artId, List<TimeMomentDescription> timeMoments) {
		super();
		this.whsId = whsId;
		this.artId = artId;
		for(TimeMomentDescription timeMomentDescription: timeMoments){
			TimeMomentDescription newTimeMomentDescription = new TimeMomentDescription();
			newTimeMomentDescription.setTimeMoment(timeMomentDescription.getTimeMoment());
			TimeSeriesElement sales = new TimeSeriesElement(timeMomentDescription.getSales().getActualValue());
			TimeSeriesElement rest = new TimeSeriesElement(timeMomentDescription.getRest().getActualValue());
			
			newTimeMomentDescription.setSales(sales);
			newTimeMomentDescription.setRest(rest);
			newTimeMomentDescription.setPriceQnty(timeMomentDescription.getPriceQnty());
			this.timeMoments.add(newTimeMomentDescription);
		}
	}
	
	@Override
	public String toString() {
		return "WhsArtTimeline [whsId=" + whsId + ", artId=" + artId + ", timeMoments=" + timeMoments + "]";
	}
	
	/** Get rests actual values separated by comma sorted by time moments
	 * @return rests actual values separated by comma sorted by time moments
	 */
	public String getRestsSortedByDate(){
		
		Collections.sort(timeMoments, new Comparator<TimeMomentDescription>() {
			@Override
			public int compare(TimeMomentDescription arg0, TimeMomentDescription arg1) {
				return arg0.getTimeMoment().compareTo(arg1.getTimeMoment());
			}
		});
		String moments = timeMoments.stream().map (e -> e.getRest().getActualValue().toString()).collect (Collectors.joining (","));
		return moments;
		
	}
	
	/** Get sales actual values separated by comma sorted by time moments
	 * @return sales actual values separated by comma sorted by time moments
	 */
	public String getSalesActualSortedByDate(){
		
		Collections.sort(timeMoments, new Comparator<TimeMomentDescription>() {
			@Override
			public int compare(TimeMomentDescription arg0, TimeMomentDescription arg1) {
				return arg0.getTimeMoment().compareTo(arg1.getTimeMoment());
			}
		});
		String moments = timeMoments.stream().map (e -> e.getSales().getActualValue().toString()).collect(Collectors.joining (","));
		return moments;
	}
	
	/** Get sales smoothed values separated by comma sorted by time moments
	 * @return sales smoothed values separated by comma sorted by time moments
	 */
	public String getSalesSmootedSortedByDate(){
		
		Collections.sort(timeMoments, new Comparator<TimeMomentDescription>() {
			@Override
			public int compare(TimeMomentDescription arg0, TimeMomentDescription arg1) {
				return arg0.getTimeMoment().compareTo(arg1.getTimeMoment());
			}
		});
		String moments = timeMoments.stream().map (e -> e.getSales().getSmoothedValue().toString()).collect(Collectors.joining (","));
		return moments;
	}
	
	
	/** Get price values separated by comma sorted by time moments
	 * @return price values separated by comma sorted by time moments
	 */
	public String getPricesSortedByDate(){
		
		Collections.sort(timeMoments, new Comparator<TimeMomentDescription>() {
			@Override
			public int compare(TimeMomentDescription arg0, TimeMomentDescription arg1) {
				return arg0.getTimeMoment().compareTo(arg1.getTimeMoment());
			}
		});
		String moments = timeMoments.stream().map (e -> e.getPriceQnty().toString()).collect(Collectors.joining (","));
		return moments;
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

	public List<TimeMomentDescription> getTimeMoments() {
		return timeMoments;
	}

	public void setTimeMoments(List<TimeMomentDescription> timeMoments) {
		this.timeMoments = timeMoments;
	}
}
