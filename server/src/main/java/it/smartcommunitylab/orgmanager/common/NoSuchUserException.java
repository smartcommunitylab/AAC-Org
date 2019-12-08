package it.smartcommunitylab.orgmanager.common;

public class NoSuchUserException extends Exception {

    private static final long serialVersionUID = -5437348903730800225L;

    public NoSuchUserException() {
        super();
    }

    public NoSuchUserException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoSuchUserException(String message) {
        super(message);
    }

    public NoSuchUserException(Throwable cause) {
        super(cause);
    }
}