package ru.kabor.demand.prediction.utils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.support.rowset.SqlRowSet;

import ru.kabor.demand.prediction.entity.TimeMomentDescription;
import ru.kabor.demand.prediction.entity.WhsArtTimeline;

public class WhsArtTimelineBuilder {
	public static List<WhsArtTimeline> buildWhsArtTimelineList(SqlRowSet rowSet){
		List<WhsArtTimeline> result= new ArrayList<>();
		Integer whsIdColumn =  rowSet.findColumn("whs_id");
		Integer artIdColumn = rowSet.findColumn("art_id");
		Integer dayIdColumn = rowSet.findColumn("day_id");
		Integer salesQntyColumn =  rowSet.findColumn("sale_qnty");
		Integer restQntyColumn = rowSet.findColumn("rest_qnty");
		while(rowSet.next()){
			Integer whsId = rowSet.getInt(whsIdColumn);
			Integer artId = rowSet.getInt(artIdColumn);
			LocalDate dayId = rowSet.getDate(dayIdColumn).toLocalDate();
			Double salesQnty = rowSet.getDouble(salesQntyColumn);
			Double restQnty = rowSet.getDouble(restQntyColumn);
			TimeMomentDescription timeMomentDescription = new TimeMomentDescription(dayId, salesQnty, restQnty);
			
			Boolean isPersist = result.stream().anyMatch(e-> e.getWhsId().equals(whsId) && e.getArtId().equals(artId));
			if(!isPersist){
				WhsArtTimeline whsArtTimeLine = new WhsArtTimeline(whsId, artId);
				whsArtTimeLine.getTimeMoments().add(timeMomentDescription);
				result.add(whsArtTimeLine);
			} else{
				WhsArtTimeline whsArtTimeLine = result.stream().filter(e-> e.getWhsId().equals(whsId) && e.getArtId().equals(artId)).findFirst().get();
				whsArtTimeLine.getTimeMoments().add(timeMomentDescription);
			}
		}
		return result;
	}
	
	public static WhsArtTimeline buildWhsArtTimeline(SqlRowSet rowSet) {
		WhsArtTimeline result = new WhsArtTimeline();
		Integer whsIdColumn = rowSet.findColumn("whs_id");
		Integer artIdColumn = rowSet.findColumn("art_id");
		Integer dayIdColumn = rowSet.findColumn("day_id");
		Integer salesQntyColumn = rowSet.findColumn("sale_qnty");
		Integer restQntyColumn = rowSet.findColumn("rest_qnty");

		Integer whsId = -1;
		Integer artId = -1;
		while (rowSet.next()) {
			whsId = rowSet.getInt(whsIdColumn);
			artId = rowSet.getInt(artIdColumn);
			LocalDate dayId = rowSet.getDate(dayIdColumn).toLocalDate();
			Double salesQnty = rowSet.getDouble(salesQntyColumn);
			Double restQnty = rowSet.getDouble(restQntyColumn);
			TimeMomentDescription timeMomentDescription = new TimeMomentDescription(dayId, salesQnty, restQnty);
			result.getTimeMoments().add(timeMomentDescription);
		}
		result.setWhsId(whsId);
		result.setArtId(artId);
		return result;
	}
}
