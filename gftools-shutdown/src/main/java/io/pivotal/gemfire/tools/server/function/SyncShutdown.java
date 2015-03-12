package io.pivotal.gemfire.tools.server.function;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import com.gemstone.gemfire.LogWriter;
import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Declarable;
import com.gemstone.gemfire.cache.execute.Function;
import com.gemstone.gemfire.cache.execute.FunctionContext;
import com.gemstone.gemfire.distributed.DistributedMember;
import com.gemstone.gemfire.management.ManagementService;
import com.gemstone.gemfire.management.MemberMXBean;


public class SyncShutdown implements Function, Declarable {

	  private static final long serialVersionUID = -7755079581436855698L;
	  private LogWriter logger = null;
	  private Cache cache;
	  private ManagementService manager;
	  private DistributedMember member;
	  private String memberName;
	  private MemberMXBean memberBean;
	  private String waitPeriod = "";
	  private long delay = 0L;
	  
	  public static final String ID = "SyncShutdown";
	  
	  
	  /**
	   * Creates an instance that logs all the output to <code>System.out</code>.
	   */
	  public SyncShutdown() {
		  init(null);
	  }
	  
	  
	  /* (non-Javadoc)
	   * @see com.gemstone.gemfire.cache.Declarable#init(java.util.Properties)
	   */
	  public void init(Properties properties) {
		  cache = CacheFactory.getAnyInstance();
		  logger = cache.getLogger();
          manager = ManagementService.getManagementService(cache);
          member = cache.getDistributedSystem().getDistributedMember(); 
          memberName = member.getName();
          memberBean = manager.getMemberMXBean();
	  }
	  
  
	  
	  /* (non-Javadoc)
	   * @see com.gemstone.gemfire.cache.execute.Function#getId()
	   */
	   public String getId() {
		  return ID;
	  }

	  /* (non-Javadoc)
	   * @see com.gemstone.gemfire.cache.execute.Function#optimizeForWrite()
	   */
	  public boolean optimizeForWrite() {
		  return false;
	  }

	  /* (non-Javadoc)
	   * @see com.gemstone.gemfire.cache.execute.Function#isHA()
	   */
	  public boolean isHA() {
		  return false;
	  }

	  /* 
	   * The SynchedShutdown function does not return a result. 
	   * It simply logs the size info as it is being calculated.
	   * @see com.gemstone.gemfire.cache.execute.Function#hasResult()
	   */
	  public boolean hasResult() {
		  return false;
	  }

	  
	  /**
	   * Executes the function.
	   * Expects a single argument - the region name to be sized
	   */
	  public void execute(FunctionContext ctx) {
		  
	      try {
	    	  this.parseArguments(ctx);
              this.setShutdownFlag();
          	  this.initiateShutdown();
	      } catch (Exception x) {
	          log(x.toString());
	      }
	  }

      private void setShutdownFlag() {
    	ShutdownSignal.getInstance().setShutdownFlag();
      }
      
      
      protected void initiateShutdown() {
        manager = ManagementService.getManagementService(cache);
        member = cache.getDistributedSystem().getDistributedMember(); 
        memberName = member.getName();
        memberBean = manager.getMemberMXBean();
        Date dNow = new Date( );
	    SimpleDateFormat ft = new SimpleDateFormat ("E dd-MM-yyyy 'at' hh:mm:ss a zzz");
        
        log("Shutdown initiated on " + ft.format(dNow) + ". Preparing to shutdown after delay of " + delay + " milliseconds.");
		long startTime = System.nanoTime();
        if (delay > 0) {
          try {
            Thread.sleep(delay);
          } catch (Exception x) {
                  log(x.toString());
          }
        }
    	long elapsedTime = System.nanoTime() - startTime;
		float elapsedTimeMs = elapsedTime / 1000000.0f;
    	log("Shutting down member: "+ memberName +" after a sleep of " + elapsedTimeMs +" milliseconds.");
  	   	memberBean.shutDownMember();
      }
      
	  protected void log(String message) {
		String msg= "<<"+ ID + " Function>> " + message;  
		if (logger != null) {
		  logger.info(msg);
		} else {
		  System.out.println(msg);
		}
	  }
	  
      private void parseArguments(FunctionContext ctx) {
    	  int numberOfArgs;
    	  List<?> list = null;
    	  Object[] args = (Object[]) ctx.getArguments();
          if (null == args) {
        	  numberOfArgs = 0;
          } else {
              //Create a list from the Array of arguments provided by FunctionContext
              list = (List<?>) Arrays.asList(args);
              numberOfArgs = list.size();
          }    
          Object arg;
          switch (numberOfArgs) {
              case 0:  delay = 0L;
                       break;
              case 1:  arg = list.get(0);
            	       if (!(arg instanceof String)) {
                           throw new IllegalArgumentException("Function argument should be a String, but contains "
                          + arg.getClass().getName());
                       }
                       waitPeriod = (String) arg;
                       try {
                   	       delay = Long.decode(waitPeriod);
                       } catch (Exception x) {
                       	   log("Unable to parse argument for delay value. Defaulting to 0 milliseconds.");
               	           log(x.toString());
                       }
                       break;
              default: throw new IllegalArgumentException("The SyncShutdown Function takes 0 or 1 arguments - the amount of time in milliseconds the function should wait before initiating the shutdown of the member.");
          }
      }
	  
}

