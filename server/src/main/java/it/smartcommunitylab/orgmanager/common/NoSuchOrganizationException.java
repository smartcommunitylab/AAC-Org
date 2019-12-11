package it.smartcommunitylab.orgmanager.common;

public class NoSuchOrganizationException extends Exception {

    private static final long serialVersionUID = 2143951241509545513L;

    public NoSuchOrganizationException() {
        super();
    }

    public NoSuchOrganizationException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoSuchOrganizationException(String message) {
        super(message);
    }

    public NoSuchOrganizationException(Throwable cause) {
        super(cause);
    }
}