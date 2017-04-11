package ru.kabor.demand.prediction.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ru.kabor.demand.prediction.entity.Request;

/** JpaRepository for Request */
@Repository
public interface RequestRepository extends JpaRepository<Request, Integer> {

    Request findTop1ByStatus(int status);
    Request findTop1ByStatusAndRequestType(int status, String requestType);
}
