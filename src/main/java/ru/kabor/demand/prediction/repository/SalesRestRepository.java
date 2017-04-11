package ru.kabor.demand.prediction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.kabor.demand.prediction.entity.SalesRest;

/** JpaRepository for SalesRest */
@Repository
public interface SalesRestRepository extends JpaRepository<SalesRest, Integer> {
	
}
