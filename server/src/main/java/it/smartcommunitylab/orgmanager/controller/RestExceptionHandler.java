package it.smartcommunitylab.orgmanager.controller;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.bind.MethodArgumentNotValidException;

import it.smartcommunitylab.orgmanager.common.AmbiguousIdentifierException;
import it.smartcommunitylab.orgmanager.common.IdentityProviderAPIException;
import it.smartcommunitylab.orgmanager.common.InvalidArgumentException;
import it.smartcommunitylab.orgmanager.common.InvalidConfigurationException;
import it.smartcommunitylab.orgmanager.common.NoSuchComponentException;
import it.smartcommunitylab.orgmanager.common.NoSuchOrganizationException;
import it.smartcommunitylab.orgmanager.common.NoSuchSpaceException;
import it.smartcommunitylab.orgmanager.common.NoSuchUserException;
import it.smartcommunitylab.orgmanager.common.SystemException;

/**
 * Handler class for various exceptions
 */
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(NoSuchOrganizationException.class)
    public ResponseEntity<String> noSuchOrganization(NoSuchOrganizationException e) {
        JSONObject error = new JSONObject();
        try {
            error.put("error", "organization_not_found");
            error.put("error_description", e.getMessage());
        } catch (JSONException je) {
        }
        return buildResponseEntity(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(NoSuchSpaceException.class)
    public ResponseEntity<String> noSuchSpace(NoSuchSpaceException e) {
        JSONObject error = new JSONObject();
        try {
            error.put("error", "space_not_found");
            error.put("error_description", e.getMessage());
        } catch (JSONException je) {
        }
        return buildResponseEntity(error, HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(NoSuchComponentException.class)
    public ResponseEntity<String> noSuchComponent(NoSuchComponentException e) {
        JSONObject error = new JSONObject();
        try {
            error.put("error", "component_not_found");
            error.put("error_description", e.getMessage());
        } catch (JSONException je) {
        }
        return buildResponseEntity(error, HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(NoSuchUserException.class)
    public ResponseEntity<String> noSuchUser(NoSuchUserException e) {
        JSONObject error = new JSONObject();
        try {
            error.put("error", "user_not_found");
            error.put("error_description", e.getMessage());
        } catch (JSONException je) {
        }
        return buildResponseEntity(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(SystemException.class)
    public ResponseEntity<String> systemError(SystemException e) {
        JSONObject error = new JSONObject();
        try {
            error.put("error", "system_exception");
            error.put("error_description", e.getMessage());
        } catch (JSONException je) {
        }
        return buildResponseEntity(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidArgumentException.class)
    public ResponseEntity<String> invalidArgument(InvalidArgumentException e) {
        JSONObject error = new JSONObject();
        try {
            error.put("error", "invalid_argument");
            error.put("error_description", e.getMessage());
        } catch (JSONException je) {
        }
        return buildResponseEntity(error, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * One of the fields of the object in the request body has invalid values. The
     * response will also list the constraint violations encountered.
     * 
     * @param e - Exception
     * @return - Response with error and proper status code
     */
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        JSONObject error = new JSONObject();
        try {
            error.put("error", "invalid_field_values");
            error.put("error_description", ex.getMessage());
        } catch (JSONException je) {
        }
      
        String response = null;
        try {
            response = error.toString(2);
        } catch (JSONException je) {
        }
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(response, headers, HttpStatus.BAD_REQUEST);
    }
    

    /**
     * One of the fields of the object in the request body has invalid values. The
     * response will also list the constraint violations encountered.
     * 
     * @param e - Exception
     * @return - Response with error and proper status code
     */
    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<String> handleConstraintViolation(ConstraintViolationException e) {
        JSONObject error = new JSONObject();
        try {
            error.put("error", "invalid_field_values");
            error.put("error_description", "One or more fields have invalid values.");
            int i = 1;
            for (ConstraintViolation cv : e.getConstraintViolations()) {
                error.put("constraint-violation-" + i, cv.getPropertyPath() + " " + cv.getMessage());
                i++;
            }
        } catch (JSONException je) {
        }
        return buildResponseEntity(error, HttpStatus.BAD_REQUEST);
    }

//    /**
//     * The entity, such as an organization or member, could not be found.
//     * 
//     * @param e - Exception
//     * @return - Response with error and proper status code
//     */
//    @ExceptionHandler(EntityNotFoundException.class)
//    protected ResponseEntity<String> handleEntityNotFound(EntityNotFoundException e) {
//        JSONObject error = new JSONObject();
//        try {
//            error.put("error", "entity_not_found");
//            error.put("error_description", e.getMessage());
//        } catch (JSONException je) {
//        }
//        return buildResponseEntity(error, HttpStatus.NOT_FOUND);
//    }

    /**
     * Input is either malformed or contains values that cannot be accepted.
     * 
     * @param e - Exception
     * @return - Response with error and proper status code
     */
    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseEntity<String> handleIllegalArgument(IllegalArgumentException e) {
        JSONObject error = new JSONObject();
        try {
            error.put("error", "invalid_argument");
            error.put("error_description", e.getMessage());
        } catch (JSONException je) {
        }
        return buildResponseEntity(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * An onbject involved in the requested operation is in a state that does not
     * allow such operation.
     * 
     * @param e - Exception
     * @return - Response with error and proper status code
     */
    @ExceptionHandler(IllegalStateException.class)
    protected ResponseEntity<String> handleIllegalState(IllegalStateException e) {
        JSONObject error = new JSONObject();
        try {
            error.put("error", "invalid_state");
            error.put("error_description", e.getMessage());
        } catch (JSONException je) {
        }
        return buildResponseEntity(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * User does not have the necessary privileges to perform the operation.
     * 
     * @param e - Exception
     * @return - Response with error and proper status code
     */
    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<String> handleAccessDenied(AccessDeniedException e) {
        JSONObject error = new JSONObject();
        try {
            error.put("error", "access_denied");
            error.put("error_description", e.getMessage());
        } catch (JSONException je) {
        }
        return buildResponseEntity(error, HttpStatus.FORBIDDEN);
    }

//    /**
//     * Creation of a record violates a constraint. Usually caused by threads running
//     * concurrently and creating records with the same value in a column where they
//     * are supposed to be unique. May be solved simply by repeating the operation.
//     * 
//     * @param e - Exception
//     * @return - Response with error and proper status code
//     */
//    @ExceptionHandler(DataIntegrityViolationException.class)
//    protected ResponseEntity<String> handleDataIntegrityViolation(DataIntegrityViolationException e) {
//        JSONObject error = new JSONObject();
//        try {
//            error.put("error", "data_integrity_error");
//            error.put("error_description", "Unable to execute operation. Try again.");
//        } catch (JSONException je) {
//        }
//        return buildResponseEntity(error, HttpStatus.INTERNAL_SERVER_ERROR);
//    }

    /**
     * Unable to call identity provider's API. Usually happens when creating
     * organizations or updating their configurations, during the phase that calls
     * APIs to create roles.
     * 
     * @param e - Exception
     * @return - Response with error and proper status code
     */
    @ExceptionHandler(IdentityProviderAPIException.class)
    protected ResponseEntity<String> handleIdentityProviderAPICallException(IdentityProviderAPIException e) {
        JSONObject error = new JSONObject();
        try {
            error.put("error", "idp_api_call_failed");
            error.put("error_description", e.getMessage());
        } catch (JSONException je) {
        }
        return buildResponseEntity(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * The identity provider's API was supposed to return only 1 entity, but
     * returned multiple entities instead.
     * 
     * @param e - Exception
     * @return - Response with error and proper status code
     */
    @ExceptionHandler(AmbiguousIdentifierException.class)
    protected ResponseEntity<String> handleAmbiguousIdentifier(AmbiguousIdentifierException e) {
        JSONObject error = new JSONObject();
        try {
            error.put("error", "cannot_identify_entity");
            error.put("error_description", e.getMessage());
        } catch (JSONException je) {
        }
        return buildResponseEntity(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Organization Manager's configuration is invalid.
     * 
     * @param e - Exception
     * @return - Response with error and proper status code
     */
    @ExceptionHandler(InvalidConfigurationException.class)
    protected ResponseEntity<String> handleInvalidConfiguration(InvalidConfigurationException e) {
        JSONObject error = new JSONObject();
        try {
            error.put("error", "invalid_configuration");
            error.put("error_description", e.getMessage());
        } catch (JSONException je) {
        }
        return buildResponseEntity(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Common exception in Java. In case it is thrown somewhere, this handler avoids
     * printing the whole stack trace to the user, and provides a short JSON
     * response instead.
     * 
     * @param e - Exception
     * @return - Response with error and proper status code
     */
    @ExceptionHandler(NullPointerException.class)
    protected ResponseEntity<String> handleNullPointer(NullPointerException e) {
        e.printStackTrace();
        JSONObject error = new JSONObject();
        try {
            error.put("error", "null_pointer");
            error.put("error_description", "Server error: a variable was not properly initialized.");
        } catch (JSONException je) {
        }
        return buildResponseEntity(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Builds the response entity to return when an exception has been handled.
     * 
     * @param status     - Contains the error, or the information message
     * @param statusCode - Status code to return
     * @return - Response with error and proper status code
     */
    private static ResponseEntity<String> buildResponseEntity(JSONObject status, HttpStatus statusCode) {
        String response = null;
        try {
            response = status.toString(2);
        } catch (JSONException je) {
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<String>(response, headers, statusCode);
    }
}
