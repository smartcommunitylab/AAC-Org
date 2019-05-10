package it.smartcommunitylab.orgmanager.componentsmodel.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CommonUtils {

	private static Log log = LogFactory.getLog(CommonUtils.class);
	/**
	 * Returns a message to communicate the result of the operation.
	 * 
	 * @param code - Denotes if the operation was successful, failed, or no operation was performed
	 * @param message - Message
	 * @return - Result of the operation
	 */
	public static String formatResult(String string, int code, String message) {
		String op = CommonConstants.SUCCESS_MSG;
		if (code == 1)
			op = CommonConstants.NO_ACTION;
		else if (code > 1)
			op = CommonConstants.ERROR_MSG;
		String result = string + ": " + op + " - " + message;
		log.info("Inside FormatResult method: " + result);
		return result;
	}
}
