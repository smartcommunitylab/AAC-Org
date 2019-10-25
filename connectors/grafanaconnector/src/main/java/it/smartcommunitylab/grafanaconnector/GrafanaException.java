package it.smartcommunitylab.grafanaconnector;

public class GrafanaException extends Exception{

	private static final long serialVersionUID = 1L;

	public GrafanaException() {
		super();
	}
	
	public GrafanaException(String message) {
		super(message);
	}
}
