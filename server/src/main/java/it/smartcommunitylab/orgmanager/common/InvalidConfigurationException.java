package it.smartcommunitylab.orgmanager.common;

/**
 * This exception is to be used when the configuration of the Organization Manager is invalid.
 */
public class InvalidConfigurationException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	public InvalidConfigurationException(String errorMessage) {
		super(errorMessage);
	}
}
