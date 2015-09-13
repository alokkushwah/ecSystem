package com.alok.ecsystem.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * Loads the internal and external properties and make them available to system.  
 * @author Alok Kushwah (akushwah)
 */
public class PropertyUtil {

	public static final Properties properties = new Properties();

	private static Logger logger  = Logger.getLogger(PropertyUtil.class);
	
	public static String getProperty(String name){
		return properties.getProperty(name);
	}
	
	/**
	 * Load the property file available in classpath.
	 * @param fileName
	 */
	public synchronized static void load(String fileName){
		logger.info("Enter load() fileName=" + fileName);
		try {
			InputStream  in = PropertyUtil.class.getClassLoader().getResourceAsStream(fileName);
			if(in==null) {
				logger.fatal("Property file fileName=" + fileName + " was not found in classpath.");
				throw new FileNotFoundException("Property file '"+  fileName +"' was not found in classpath.");
			}
			properties.load(in);
		} catch (IOException e) {
			logger.fatal("Unable to load properties. Expecting '"+  fileName +"' in classpath.",e);
			throw new RuntimeException("Unable to load properties. Expecting '"+  fileName +"' in classpath.",e);
		}
		logger.info("Exit load()");
	}

	/**
	 * Load property file from file system.
	 * @param file
	 */
	public synchronized static void load(File file) {
		logger.info("Enter load() file=" + file.getAbsolutePath());
		FileInputStream fis  = null;
		try {
			fis = new FileInputStream(file);
			properties.load(fis);
		} catch (IOException e) {
			logger.fatal("Unable to load properties. Expecting '"+  file.getAbsolutePath() +"'.",e);
			throw new RuntimeException("Unable to load properties. Expecting '"+  file.getAbsolutePath() +"'.",e);
		}finally{
			if(fis!=null){
				try {
					fis.close();
				} catch (IOException e) {
					logger.warn("Unable to close properties file '"+  file.getAbsolutePath() +"'",e);
				}
			}
		}
		logger.info("Exit load()");
	}
	
	/**
	 * Utility method to remove all loaded properties. This method is used by Junit test cases.
	 */
	public synchronized static void unload(){
		properties.clear();
	}

}
