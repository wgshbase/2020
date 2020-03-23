package io.renren;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Log4jTestDemo {
	 private static Logger logger = LoggerFactory.getLogger(Log4jTestDemo.class);
	

	    /**
	     * @param args
	     */
	    public static void main(String[] args) {
	        // System.out.println("This is println message.");
	    	
	    	

	        // 记录debug级别的信息
	        logger.debug("This is debug message."+logger.isDebugEnabled());
	        // 记录info级别的信息
	        logger.info("This is info message."+logger.isInfoEnabled());
	        // 记录error级别的信息
	        logger.error("This is error message."+logger.isErrorEnabled());
	    }
}
