package irys.client.services;

/**
 * Callback to be notified on errors or warnings for SIRI request/response call
 * 
 * @author michel
 *
 */
public interface SiriErrorCallback {
	
	/**
	 * set if errors were encountered
	 */
	void setErrors(boolean value);

	/**
	 * set if warnings were encountered
	 */
	void setWarnings(boolean value);
}
