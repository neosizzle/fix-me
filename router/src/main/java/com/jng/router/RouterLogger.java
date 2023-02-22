package com.jng.router;

public class RouterLogger {
	private static final String RESET = "\033[0m";
	private static final String BLACK = "\033[30m";			  /* Black */
	private static final String RED = "\033[31m";				  /* Red */
	private static final String GREEN = "\033[32m";			  /* Green */
	private static final String YELLOW = "\033[33m";			  /* Yellow */
	private static final String BLUE = "\033[34m";				  /* Blue */
	private static final String MAGENTA = "\033[35m";			  /* Magenta */
	private static final String CYAN = "\033[36m";				  /* Cyan */
	private static final String WHITE = "\033[37m";			  /* White */
	private static final String BOLDBLACK = "\033[1m\033[30m";	  /* Bold Black */
	private static final String BOLDRED = "\033[1m\033[31m";	  /* Bold Red */
	private static final String BOLDGREEN = "\033[1m\033[32m";	  /* Bold Green */
	private static final String BOLDYELLOW = "\033[1m\033[33m";  /* Bold Yellow */
	private static final String BOLDBLUE = "\033[1m\033[34m";	  /* Bold Blue */
	private static final String BOLDMAGENTA = "\033[1m\033[35m"; /* Bold Magenta */
	private static final String BOLDCYAN = "\033[1m\033[36m";	  /* Bold Cyan */
	private static final String BOLDWHITE = "\033[1m\033[37m";	  /* Bold White */

	private static final String PREFIX = "🍆router🍆 ";

	public void logDebug(String str)
	{
		System.out.println(BOLDWHITE + PREFIX + RESET + str);
	}

	public void logInfo(String str)
	{
		System.out.println(BOLDGREEN + PREFIX + RESET + GREEN + str + RESET);
	}

	public void logWarning(String str)
	{
		System.out.println(BOLDYELLOW + PREFIX + RESET + YELLOW + str + RESET);
	}

	public void logErr(String str)
	{
		System.out.println(BOLDRED + PREFIX + RESET + RED + str + RESET);
	}
}
