package it.smartcommunitylab.orgmanager.componentsmodel;

/**
 * Custom exception to use when something goes wrong in a component.
 */
public class ComponentException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public ComponentException(String errorMessage) {
		super(errorMessage);
	}
}
