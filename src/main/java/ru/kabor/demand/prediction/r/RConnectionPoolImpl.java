package ru.kabor.demand.prediction.r;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RserveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class RConnectionPoolImpl implements Runnable, RConnectionPool {
	
	//Login settings
	private String host;
	private Integer port;
	
	//Pool settings
	private Integer initialCountConnections;
	private Integer maxCountConnections;
	private Long awaitIfBusyTimeoutMillisecond;
	
	//One connection settings
	/** Executes only once when open new connection */
	private List<String> connectionOpenCommanList = new ArrayList<>();	
	/** Executes each time when get connection from pool */
	private List<String> connectionGetCommandList = new ArrayList<>();	
	/** Executes each time when return connection to pool*/
	private List<String> connectionReleaseCommandList = new ArrayList<>();	
	
	//Connection collections
	/** Map of available connections */
	private Map<UUID, RCommonConnection> availableConnectionsMap;	
	/** Map of busy connections */
	private Map<UUID, RCommonConnection> busyConnectionsMap;	
	
	//Global pool setting
	/** We have to attach pool before getting the first connection from them*/
	private Boolean isPoolAttached = false;
	/** We have to set Login Settings before executing attaching pool*/
	private Boolean isLoginSettngsValid = false;
	/** We have to set Pool Settings before executing attaching pool*/
	private Boolean isPoolSettingsValid = false;
	/** Creating new connection*/
	private boolean connectionPending = false;
	
	private static final Logger LOG = LoggerFactory.getLogger(RConnectionPoolImpl.class);

	public RConnectionPoolImpl() {
		super();
	}
	
	/** Settings for connection to Rserve*/
	public void setLoginSettings(String host, Integer port) {

		if (host == null || host.trim().equals("")) {
			throw new IllegalArgumentException("host can't be empty string");
		}

		if (port < 0) {
			throw new IllegalArgumentException("port has to be greater than 0");
		}

		this.host = host;
		this.port = port;
		this.isLoginSettngsValid = true;
	}
	
	/** Settings for creating pool with users size */
	public void setPoolSettings(Integer initialCountConnections, Integer maxCountConnections, Long awaitIfBusyTimeoutMillisecond) {

		if (initialCountConnections < 0) {
			LOG.error("initialCountConnections has to be greater or equal to 0");
			throw new IllegalArgumentException("initialCountConnections has to be greater or equal to 0");
		}

		if (maxCountConnections <= 0 || maxCountConnections < initialCountConnections) {
			LOG.error("maxCountConnections has to be greater than initialCountConnections");
			throw new IllegalArgumentException("maxCountConnections has to be greater than initialCountConnections");
		}

		this.initialCountConnections = initialCountConnections;
		this.maxCountConnections = maxCountConnections;
		this.awaitIfBusyTimeoutMillisecond = awaitIfBusyTimeoutMillisecond;
		this.isPoolSettingsValid = true;

		// sorry, but only one thread for windows
		String osName = System.getProperty("os.name");
		osName = osName.trim().toLowerCase();
		if (osName.contains("wind")) {
			this.initialCountConnections = 1;
			this.maxCountConnections = 1;
			LOG.info("Only 1 connection for windows");
		}
	}
	
	
	/** Commands when work with connections
	 *  @param connectionOpenCommanList Executes only once when open new connection
	 *  @param connectionGetCommandList Executes each time when get connection from pool
	 *  @param connectionPutCommandList Executes each time when return connection to pool
	 * */
	public void setConnectionLifecycleCommands(List<String> connectionOpenCommanList, List<String> connectionGetCommandList, List<String> connectionReleaseCommandList) {
		if(connectionOpenCommanList!=null){
			this.connectionOpenCommanList = connectionOpenCommanList;
		}
		if(connectionGetCommandList!=null){
			this.connectionGetCommandList = connectionGetCommandList;
		}
		if(connectionReleaseCommandList!=null){
			this.connectionReleaseCommandList = connectionReleaseCommandList;
		}
	}
	
	/** Try to connect to R with current settings
	 * @throws RConnectionPoolException 
	 * @return Result of creating connection pool
	 * @throws RConnectionPoolException 
	 * @throws RserveException */
	public synchronized Boolean attachToRserve() throws  RConnectionPoolException, RserveException {

		if (this.isPoolAttached) {
			LOG.error("Connection pool is already attached");
			throw new RConnectionPoolException("Connection pool is already attached");
		}

		if (this.isLoginSettngsValid == false) {
			LOG.error("Please, set valid login settings by setLoginSettings(...)");
			throw new RConnectionPoolException("Please, set valid login settings by setLoginSettings(...)");
		}

		if (this.isPoolSettingsValid == false) {
			LOG.error("Please, set valid pool settings by setPoolSettings(...)");
			throw new RConnectionPoolException("Please, set valid pool settings by setPoolSettings(...)");
		}

		//Test connection
		RCommonConnection testConnection = null;
		try {
			testConnection = this.createNewConnection();
			REXP expression = testConnection.parseAndEval("myVersion<-R.version.string");
			String version = expression.asString();
			if (version != null && !version.trim().equals("")) {
				this.isPoolAttached = true;
			} else{
				throw new RConnectionPoolException("Can't get R version.");
			}
		} catch (REXPMismatchException | REngineException e) {
			throw new RConnectionPoolException("Can't create test connection. " + e.toString());
		} finally {
			if (testConnection != null) {
				testConnection.close();
			}
		}
		
		//Create free connections
		this.availableConnectionsMap = new ConcurrentHashMap<>(this.initialCountConnections);
		this.busyConnectionsMap = new ConcurrentHashMap<>();
		for(int i=0; i<this.initialCountConnections; i++){
			RCommonConnection connection = createNewConnection();
			this.availableConnectionsMap.put(connection.getUuid(), connection);
		}
		return true;
	}
	
	/** Create new connection to RServe*/
	private RCommonConnection createNewConnection() throws RserveException {
		RCommonConnection connection = null;
		if (this.host != null && this.port != null) {
			connection = new RCommonConnection(this.host, this.port);
		} else if (this.host != null) {
			connection = new RCommonConnection(this.host);
		} else {
			connection = new RCommonConnection();
		}
		this.executeConnectionOpenCommands(connection);
		return connection;
	}
	
	/** Execute commands from this.connectionOpenCommanList when open new connection*/
	private void executeConnectionOpenCommands(RCommonConnection connection) throws RserveException{
		for(String command: this.connectionOpenCommanList){
			connection.voidEval(command);
		}
	}
	
	/** Execute commands from this.connectionGetCommandList when getting free connection from pool*/
	private void executeConnectionGetCommands(RCommonConnection connection) throws RserveException{
		for(String command: this.connectionGetCommandList){
			connection.voidEval(command);
		}
	}
	
	/** Execute commands from this.connectionReleaseCommandList when putting connection to pool*/
	private void executeConnectionReleaseCommands(RCommonConnection connection) throws RserveException {
		for (String command : this.connectionReleaseCommandList) {
			connection.voidEval(command);
		}
	}
	
	/** Getting connection from pool
	 * @throws RConnectionPoolException */
	public synchronized RCommonConnection getConnection() throws RConnectionPoolException  {
        if (!this.availableConnectionsMap.isEmpty()) {
        	Optional<RCommonConnection> optionalConnection = this.availableConnectionsMap.values().stream().findFirst();
        	RCommonConnection connection = optionalConnection.get();
        	this.availableConnectionsMap.values().remove(connection);
            if (!connection.isConnected()) {
                notifyAll();
                return this.getConnection();
            } else {
            	this.busyConnectionsMap.put(connection.getUuid(), connection);
                return connection;
            }
        } else {
        	Integer totalCountConnection = this.availableConnectionsMap.size() + this.busyConnectionsMap.size();
            if (((totalCountConnection < this.maxCountConnections) && !connectionPending)) {
                makeBackgroundConnection();
            } else if (this.awaitIfBusyTimeoutMillisecond<0) {
                throw new RConnectionPoolException("Connection limit reached");
            }
            try {
                wait(this.awaitIfBusyTimeoutMillisecond);
            } catch (InterruptedException ie) {} //nothing bad happened
            // Someone freed up a connection, so try again.
            return this.getConnection();
        }
    }
 
	/** Make connection in separate thread */
    private void makeBackgroundConnection() {
        this.connectionPending = true;
        Thread connectThread = new Thread(this);
        connectThread.start();
    }
 
    public void run() {
        try {
            RCommonConnection connection = createNewConnection();
            synchronized (this) {
            	executeConnectionGetCommands(connection);
                this.availableConnectionsMap.put(connection.getUuid(), connection);
                connectionPending = false;
                notifyAll();
            }
        } catch (RserveException e) {}// It's impossible because only one place can throw exception (executeConnectionGetCommands). But that set of command already executed in attaching connection.
    }
    
    public synchronized void releaseConnection(RCommonConnection connection) throws RConnectionPoolException{
		if (this.isPoolAttached == false) {
			throw new RConnectionPoolException("Pool is not attached to R. Please, execute attachToRserve");
		}
		try {
			executeConnectionReleaseCommands(connection);
		} catch (RserveException e) {
			throw new RConnectionPoolException("Can't execute command:" + e.toString());
		}
		this.busyConnectionsMap.remove(connection.getUuid());
		this.availableConnectionsMap.put(connection.getUuid(), connection);
		notifyAll();
    }

	
	@Override
	/** Detach pool from Rserve and close all connections */
	public synchronized void detachFromRserve() {
		this.busyConnectionsMap.values().stream().forEach((e)->{
			try {
				this.releaseConnection(e);
			} catch (RConnectionPoolException e1) {} //that is sad, but life is life
		});
		this.busyConnectionsMap.clear();
		this.availableConnectionsMap.clear();
		this.isPoolAttached = false;
	}
}
