package ru.kabor.demand.prediction.service;

import ru.kabor.demand.prediction.entity.SalesRest;

import java.util.List;

/** It contains methods for saving sales, rests and prices to database */
public interface SalesRestService {

    void storeBathSalesRest(List<SalesRest> salesRestList);

}
