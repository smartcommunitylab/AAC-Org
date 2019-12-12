package it.smartcommunitylab.grafanaconnector.dto;

import java.util.HashMap;
import java.util.Map;

public class OrganizationDTO {

	private int id;
	private String name;
	private Map<String,String> address = new HashMap<>();
	
	public OrganizationDTO(String name) {
		this.name = name;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Map<String, String> getAddress() {
		return address;
	}
	public void setAddress(Map<String, String> address) {
		this.address = address;
	}
}
