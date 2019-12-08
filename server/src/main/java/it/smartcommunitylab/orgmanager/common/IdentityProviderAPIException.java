package it.smartcommunitylab.orgmanager.common;

/**
 * Generic exception for when a call to one of the identity provider's APIs
 * fails. It may be used when the identity provider returns an error due to the
 * request being malformed, or it may be fired before even calling the API, if
 * the conditions to call it are not met.
 */
public class IdentityProviderAPIException extends Exception {

    private static final long serialVersionUID = -3040148200017590170L;

    public IdentityProviderAPIException() {
        super();
    }

    public IdentityProviderAPIException(String message, Throwable cause) {
        super(message, cause);
    }

    public IdentityProviderAPIException(String message) {
        super(message);
    }

    public IdentityProviderAPIException(Throwable cause) {
        super(cause);
    }
}
