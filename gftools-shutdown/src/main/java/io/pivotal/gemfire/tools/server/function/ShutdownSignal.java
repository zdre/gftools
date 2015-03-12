package io.pivotal.gemfire.tools.server.function;

/**
 * This class serves as a signal that shutdown has been initiated for this member.
 * Functions executing on this GemFire member should call ShutdownSignal.getInstance().hasShutdownStarted() prior to 
 * executing in order to avoid partial/duplicate function executions.
 */
public class ShutdownSignal {
	
	 private boolean shutdownFlag = false;
	 
	 /**
	  * We do not want multiple signal objects lying around so this will be a singleton
	  */
	 private ShutdownSignal() {

	 }
	
	 /**
	  * Lazy initializing the singleton instance
	  */
	 private static class SingletonHolder {
	   public static final ShutdownSignal INSTANCE = new ShutdownSignal();
	 }
	
	 /**
	  * Use this method to get a reference to the singleton instance of
	  * {@link ShutdownSignal}
	  * 
	  * @return the singleton instance
	  */
	 public static ShutdownSignal getInstance() {
	  return SingletonHolder.INSTANCE;
	 }

     /**
      * Set the flag indicating that the shutdown sequence has been initiated.
      */
	 public synchronized void setShutdownFlag() {
		 shutdownFlag = true;
	 }

	 /**
	  * Return a boolean indicating if shutdown for this member has been initiated.
	  * @return boolean
	  */
	 public synchronized boolean hasShutdownStarted() {
		 return shutdownFlag;
	 }
	 
}




