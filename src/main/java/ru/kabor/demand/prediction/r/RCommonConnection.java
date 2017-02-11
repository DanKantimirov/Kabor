package ru.kabor.demand.prediction.r;

import java.util.UUID;

import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

public class RCommonConnection extends RConnection {
	/** Id of connection*/
	private UUID uuid;
	/** Count of attempt to connect server */
	private Integer countFailsOfOpeningConnection;

	public RCommonConnection() throws RserveException {
		super();
		this.uuid = UUID.randomUUID();
		this.countFailsOfOpeningConnection = 1;
	}

	public RCommonConnection(String paramString) throws RserveException {
		super(paramString);
		this.uuid = UUID.randomUUID();
		this.countFailsOfOpeningConnection = 1;
	}

	public RCommonConnection(String paramString, int paramInt) throws RserveException {
		super(paramString, paramInt);
		this.uuid = UUID.randomUUID();
		this.countFailsOfOpeningConnection = 1;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof RCommonConnection))
			return false;
		RCommonConnection other = (RCommonConnection) obj;
		if (uuid == null) {
			if (other.uuid != null)
				return false;
		} else if (!uuid.equals(other.uuid))
			return false;
		return true;
	}

	public UUID getUuid() {
		return uuid;
	}

	public Integer getCountFailsOfOpeningConnection() {
		return countFailsOfOpeningConnection;
	}

	public void setCountFailsOfOpeningConnection(Integer countFailsOfOpeningConnection) {
		this.countFailsOfOpeningConnection = countFailsOfOpeningConnection;
	}

}
