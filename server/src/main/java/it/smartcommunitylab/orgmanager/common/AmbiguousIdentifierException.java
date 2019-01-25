package it.smartcommunitylab.orgmanager.common;

/**
 * This to exception is to be used when, after asking the identity provider to identify
 * an entity based on a specific field, multiple entities are found, instead of just one.
 */
public class AmbiguousIdentifierException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	public AmbiguousIdentifierException(String errorMessage) {
		super(errorMessage);
	}
}
