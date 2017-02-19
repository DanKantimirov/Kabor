package ru.kabor.demand.prediction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ru.kabor.demand.prediction.entity.ForecastParameter;

@Repository
public interface ForecastParameterRepository extends JpaRepository<ForecastParameter, Integer> {

}
