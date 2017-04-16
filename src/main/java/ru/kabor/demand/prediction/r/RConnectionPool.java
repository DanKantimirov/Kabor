package ru.kabor.demand.prediction.r;

import org.rosuda.REngine.Rserve.RserveException;

/** Connection pool to R */
public interface RConnectionPool {

	/** Attach connection pool to R server
	 * @return true if success
	 * @throws RConnectionPoolException
	 * @throws RserveException
	 */
	Boolean attachToRserve() throws RConnectionPoolException, RserveException  ;
	
	/** Detach  connection pool from R server
	 * 
	 */
	void detachFromRserve();
	
	/** Get R connection from pool
	 * @return R connection
	 * @throws RConnectionPoolException
	 */
	RCommonConnection getConnection() throws  RConnectionPoolException;
	
	/** Put R connection to pool
	 * @param connection R connection
	 * @throws RConnectionPoolException
	 */
	void releaseConnection(RCommonConnection connection) throws RConnectionPoolException;
}