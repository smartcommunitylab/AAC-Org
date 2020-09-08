package it.smartcommunitylab.orgmanager.common;

public class NoSuchComponentException extends Exception {

    private static final long serialVersionUID = 8048102042662695913L;

    public NoSuchComponentException() {
        super();
    }

    public NoSuchComponentException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoSuchComponentException(String message) {
        super(message);
    }

    public NoSuchComponentException(Throwable cause) {
        super(cause);
    }
}