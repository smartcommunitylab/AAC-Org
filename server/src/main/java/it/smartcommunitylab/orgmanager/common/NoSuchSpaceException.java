package it.smartcommunitylab.orgmanager.common;

public class NoSuchSpaceException extends Exception {

    private static final long serialVersionUID = -7725987603340606351L;

    public NoSuchSpaceException() {
        super();
    }

    public NoSuchSpaceException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoSuchSpaceException(String message) {
        super(message);
    }

    public NoSuchSpaceException(Throwable cause) {
        super(cause);
    }
}