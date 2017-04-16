package ru.kabor.demand.prediction.utils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.support.rowset.SqlRowSet;

import ru.kabor.demand.prediction.entity.TimeMomentDescription;
import ru.kabor.demand.prediction.entity.TimeSeriesElement;
import ru.kabor.demand.prediction.entity.WhsArtTimeline;

/** It builds WhsArtTimeline from SqlRowSet*/
public class WhsArtTimelineBuilder {
	
	/** build whsArtTimeLine (list) with prices
	 * @param rowSet sql rowSet
	 * @return whsArtTimeLine (list)
	 */
	public static List<WhsArtTimeline> buildWhsArtTimelineListWithPrices(SqlRowSet rowSet) {
		List<WhsArtTimeline> result = new ArrayList<>();
		Integer whsIdColumn = rowSet.findColumn("whs_id");
		Integer artIdColumn = rowSet.findColumn("art_id");
		Integer dayIdColumn = rowSet.findColumn("day_id");
		Integer salesQntyColumn = rowSet.findColumn("sale_qnty");
		Integer restQntyColumn = rowSet.findColumn("rest_qnty");
		Integer priceColumn = rowSet.findColumn("price");

		while (rowSet.next()) {
			Integer whsId = rowSet.getInt(whsIdColumn);
			Integer artId = rowSet.getInt(artIdColumn);
			LocalDate dayId = rowSet.getDate(dayIdColumn).toLocalDate();
			Double salesQnty = rowSet.getDouble(salesQntyColumn);
			Double restQnty = rowSet.getDouble(restQntyColumn);
			Double priceQnty = 0.0;
			if (priceColumn != null && priceColumn > 0) {
				priceQnty = rowSet.getDouble(priceColumn);
			}

			TimeMomentDescription timeMomentDescription = new TimeMomentDescription();
			timeMomentDescription.setTimeMoment(dayId);
			timeMomentDescription.setRest(new TimeSeriesElement(restQnty));
			timeMomentDescription.setSales(new TimeSeriesElement(salesQnty));
			timeMomentDescription.setPriceQnty(priceQnty);

			Boolean isPersist = result.stream().anyMatch(e -> e.getWhsId().equals(whsId) && e.getArtId().equals(artId));
			if (!isPersist) {
				WhsArtTimeline whsArtTimeLine = new WhsArtTimeline(whsId, artId);
				whsArtTimeLine.getTimeMoments().add(timeMomentDescription);
				result.add(whsArtTimeLine);
			} else {
				WhsArtTimeline whsArtTimeLine = result.stream().filter(e -> e.getWhsId().equals(whsId) && e.getArtId().equals(artId)).findFirst().get();
				whsArtTimeLine.getTimeMoments().add(timeMomentDescription);
			}
		}
		return result;
	}
	
	/** build whsArtTimeLine with prices
	 * @param rowSet sql rowSet
	 * @return whsArtTimeLine
	 */
	public static WhsArtTimeline buildWhsArtTimelineWithPrices(SqlRowSet rowSet) {
		WhsArtTimeline result = new WhsArtTimeline();
		Integer whsIdColumn = rowSet.findColumn("whs_id");
		Integer artIdColumn = rowSet.findColumn("art_id");
		Integer dayIdColumn = rowSet.findColumn("day_id");
		Integer salesQntyColumn = rowSet.findColumn("sale_qnty");
		Integer restQntyColumn = rowSet.findColumn("rest_qnty");
		Integer priceColumn = rowSet.findColumn("price");

		Integer whsId = -1;
		Integer artId = -1;
		while (rowSet.next()) {
			whsId = rowSet.getInt(whsIdColumn);
			artId = rowSet.getInt(artIdColumn);
			LocalDate dayId = rowSet.getDate(dayIdColumn).toLocalDate();
			Double salesQnty = rowSet.getDouble(salesQntyColumn);
			Double restQnty = rowSet.getDouble(restQntyColumn);

			Double priceQnty = 0.0;
			if (priceColumn != null && priceColumn > 0) {
				priceQnty = rowSet.getDouble(priceColumn);
			}

			TimeMomentDescription timeMomentDescription = new TimeMomentDescription();
			timeMomentDescription.setTimeMoment(dayId);
			timeMomentDescription.setRest(new TimeSeriesElement(restQnty));
			timeMomentDescription.setSales(new TimeSeriesElement(salesQnty));
			timeMomentDescription.setPriceQnty(priceQnty);

			result.getTimeMoments().add(timeMomentDescription);
		}
		result.setWhsId(whsId);
		result.setArtId(artId);
		return result;
	}
}
