package ru.kabor.demand.prediction.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.kabor.demand.prediction.entity.SalesRest;
import ru.kabor.demand.prediction.repository.SalesRestRepository;

import java.util.List;


@Service
public class SalesRestServiceImplementation implements SalesRestService {

    private static final Logger LOG = LoggerFactory.getLogger(RequestServiceImplementation.class);

    @Autowired
    SalesRestRepository salesRestRepository;

    @Override
    public void storeBathSalesRest(List<SalesRest> salesRestList) {
        salesRestRepository.save(salesRestList);
        salesRestRepository.flush();
        LOG.debug("save and flush salesRest batch list");
    }
}
