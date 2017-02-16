package ru.kabor.demand.prediction.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.kabor.demand.prediction.model.Request;

@Repository
public interface RequestRepository extends JpaRepository<Request, Integer> {
}
