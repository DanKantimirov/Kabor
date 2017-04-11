package ru.kabor.demand.prediction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ru.kabor.demand.prediction.entity.ElasticityParameter;

/** JpaRepository for ElasticityParameter */
@Repository
public interface ElasticityParameterRepository extends JpaRepository<ElasticityParameter, Integer> {

}
