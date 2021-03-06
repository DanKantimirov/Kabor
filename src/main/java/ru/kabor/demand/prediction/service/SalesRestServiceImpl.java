package ru.kabor.demand.prediction.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.kabor.demand.prediction.entity.SalesRest;
import ru.kabor.demand.prediction.repository.SalesRestRepository;
import java.util.List;

/** Implementation of SalesRestService */
@Service
public class SalesRestServiceImpl implements SalesRestService {

    private static final Logger LOG = LoggerFactory.getLogger(RequestServiceImpl.class);

    @Autowired
    SalesRestRepository salesRestRepository;

    @Override
    public void storeBathSalesRest(List<SalesRest> salesRestList) {
        salesRestRepository.save(salesRestList);
        salesRestRepository.flush();
        LOG.info("save and flush salesRest batch list");
    }
}
