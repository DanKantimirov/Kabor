package ru.kabor.demand.prediction.r;

import org.rosuda.REngine.Rserve.RserveException;

public interface RConnectionPool {

	Boolean attachToRserve() throws RConnectionPoolException, RserveException  ;
	void detachFromRserve();
	RCommonConnection getConnection() throws  RConnectionPoolException;
	void releaseConnection(RCommonConnection connection) throws RConnectionPoolException;
}