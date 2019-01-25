package it.smartcommunitylab.orgmanager.common;

/**
 * Generic exception for when a call to one of the identity provider's APIs fails.
 * It may be used when the identity provider returns an error due to the request being
 * malformed, or it may be fired before even calling the API, if the conditions to call
 * it are not met.
 */
public class IdentityProviderAPIException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public IdentityProviderAPIException(String errorMessage) {
		super(errorMessage);
	}
}
