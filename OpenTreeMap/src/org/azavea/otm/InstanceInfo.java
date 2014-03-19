package org.azavea.otm;

public class InstanceInfo {

	private int instanceId;
	private String geoRevId;
	private String name;

	public InstanceInfo(int instanceId, String geoRevId, String name) {
		this.instanceId = instanceId;
		this.geoRevId = geoRevId;
		this.name = name;
		
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGeoRevId() {
		return geoRevId;
	}
	
	public void setGeoRevId(String geoRevId) {
		this.geoRevId = geoRevId;
	}
	
	public int getInstanceId() {
		return instanceId;
	}
	
	public void setInstanceId(int instanceId) {
		this.instanceId = instanceId;
	}
}
