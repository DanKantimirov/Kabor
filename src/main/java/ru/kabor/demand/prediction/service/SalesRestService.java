package ru.kabor.demand.prediction.service;

import ru.kabor.demand.prediction.entity.SalesRest;

import java.util.List;

/**
 * interface for managing sales rest db entities
 */
public interface SalesRestService {

    void storeBathSalesRest(List<SalesRest> salesRestList);

}
