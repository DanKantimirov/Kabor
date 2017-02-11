package ru.kabor.demand.prediction.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class WhsArtTimeline {
	private Integer whsId;
	private Integer artId;
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
		for(TimeMomentDescription timeMomentDescription: timeMoments){		//TODO: work with clonable
			TimeMomentDescription newTimeMomentDescription = new TimeMomentDescription(timeMomentDescription.getTimeMoment(), timeMomentDescription.getSalesQnty(), timeMomentDescription.getRestQnty());
			this.timeMoments.add(newTimeMomentDescription);
		}
	}
	
	public String getRestsSortedByDate(){
		
		Collections.sort(timeMoments, new Comparator<TimeMomentDescription>() {
			@Override
			public int compare(TimeMomentDescription arg0, TimeMomentDescription arg1) {
				return arg0.getTimeMoment().compareTo(arg1.getTimeMoment());
			}
		});
		String moments = timeMoments.stream().map (e -> e.getRestQnty().toString()).collect (Collectors.joining (","));
		return moments;
		
	}
	
	public String getSalesSortedByDate(){
		
		Collections.sort(timeMoments, new Comparator<TimeMomentDescription>() {
			@Override
			public int compare(TimeMomentDescription arg0, TimeMomentDescription arg1) {
				return arg0.getTimeMoment().compareTo(arg1.getTimeMoment());
			}
		});
		String moments = timeMoments.stream().map (e -> e.getSalesQnty().toString()).collect(Collectors.joining (","));
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

	@Override
	public String toString() {
		return "WhsArtTimeline [whsId=" + whsId + ", artId=" + artId + ", timeMoments=" + timeMoments + "]";
	}
	
}
