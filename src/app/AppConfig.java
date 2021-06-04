package app;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.Semaphore;

import mutex.TokenMutex;
import servent.SimpleServentListener;

/**
 * This class contains all the global application configuration stuff.
 * 
 * @author bmilojkovic
 *
 */
public class AppConfig {

	/**
	 * Convenience access for this servent's information
	 */
	public static ServentInfo myServentInfo;
	public static Semaphore mutex = new Semaphore(1);
	public static SimpleServentListener ssl;
	

	/**
	 * Print a message to stdout with a timestamp
	 * 
	 * @param message
	 *            message to print
	 */
	public static void timestampedStandardPrint(String message) {
		DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		Date now = new Date();

		System.out.println(timeFormat.format(now) + " - " + message);
	}

	/**
	 * Print a message to stderr with a timestamp
	 * 
	 * @param message
	 *            message to print
	 */
	public static void timestampedErrorPrint(String message) {
		DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		Date now = new Date();

		System.err.println(timeFormat.format(now) + " - " + message);
	}

	public static boolean INITIALIZED = false;
	public static int BOOTSTRAP_PORT;
	public static String BOOTSTRAP_IP;
	public static int SERVENT_COUNT;
	public static String STORAGE;
	public static String WORK_ROUTE;
	public static String STORAGE_PATH;
	public static String WORK_ROUTE_PATH;
	public static ChordState chordState;
	public static FileConfig fileConfig = new FileConfig();

	/**
	 * Reads a config file. Should be called once at start of app. The config file
	 * should be of the following format: <br/>
	 * <code><br/>
	 * servent_count=3 			- number of servents in the system <br/>
	 * chord_size=64			- maximum value for Chord keys <br/>
	 * bs.port=2000				- bootstrap server listener port <br/>
	 * servent0.port=1100 		- listener ports for each servent <br/>
	 * servent1.port=1200 <br/>
	 * servent2.port=1300 <br/>
	 * 
	 * </code> <br/>
	 * So in this case, we would have three servents, listening on ports: 1100,
	 * 1200, and 1300. A bootstrap server listening on port 2000, and Chord system
	 * with max 64 keys and 64 nodes.<br/>
	 * 
	 * @param configName
	 *            name of configuration file
	 * @param serventId
	 *            id of the servent, as used in the configuration file
	 */
	public static void readConfig(String configName, int serventId) {

		Properties properties = new Properties();

		try {
			properties.load(new FileInputStream(new File(configName)));

		} catch (IOException e) {
			timestampedErrorPrint("Couldn't open properties file. Exiting...");
			System.exit(0);
		}

		try {
			BOOTSTRAP_PORT = Integer.parseInt(properties.getProperty("bs.port"));
		} catch (NumberFormatException e) {
			timestampedErrorPrint("Problem reading bootstrap_port. Exiting...");
			System.exit(0);
		}

		try {
			BOOTSTRAP_IP = properties.getProperty("bs.ip");
		} catch (NumberFormatException e) {
			timestampedErrorPrint("Problem reading bootstrap_ip. Exiting...");
			System.exit(0);
		}

		try {
			SERVENT_COUNT = Integer.parseInt(properties.getProperty("servent_count"));
		} catch (NumberFormatException e) {
			timestampedErrorPrint("Problem reading servent_count. Exiting...");
			System.exit(0);
		}

		try {
			int chordSize = Integer.parseInt(properties.getProperty("chord_size"));

			ChordState.CHORD_SIZE = chordSize;
			chordState = new ChordState();

		} catch (NumberFormatException e) {
			timestampedErrorPrint("Problem reading chord_size. Must be a number that is a power of 2. Exiting...");
			System.exit(0);
		}

		String portProperty = "servent" + serventId + ".port";
		String ipProperty = "servent" + serventId + ".ip";

		int serventPort = -1;
		String serventIp = "";
		try {
			serventPort = Integer.parseInt(properties.getProperty(portProperty));
			serventIp = properties.getProperty(ipProperty);
		} catch (NumberFormatException e) {
			timestampedErrorPrint("Problem reading " + portProperty + ". Exiting...");
			System.exit(0);
		}

		myServentInfo = new ServentInfo(serventIp, serventPort);

		// Load work and storage
		STORAGE = properties.getProperty("storage.route" + serventId);
		WORK_ROUTE = properties.getProperty("work.route" + serventId);

		STORAGE_PATH = (System.getProperty("user.dir")) + "/chord/" + STORAGE;
		WORK_ROUTE_PATH = (System.getProperty("user.dir")) + "/chord/" + WORK_ROUTE;

		File storageProjectDir = new File(STORAGE_PATH);
		File workProjectDir = new File(WORK_ROUTE_PATH);

		if (!storageProjectDir.exists()) {
			try {
				storageProjectDir.mkdir();
			} catch (SecurityException se) {
				se.printStackTrace();
			}
		}
		if (!workProjectDir.exists()) {
			try {
				workProjectDir.mkdir();
			} catch (SecurityException se) {
				se.printStackTrace();
			}
		}
		
		File test1 = new File(WORK_ROUTE_PATH +  File.separator + "test" + myServentInfo.getChordId());
		fileConfig.setFileContent(test1, "ovo je test");
		
		chordState.getFileVersions().put(test1.getName(), 0);
		
		File test2 = new File(WORK_ROUTE_PATH +  File.separator + "proba" + myServentInfo.getChordId());
		fileConfig.setFileContent(test2, "ovo je proba");
		chordState.getFileVersions().put(test2.getName(), 0);
	}

	public static void releaseBothMutex() {
		TokenMutex.unlock();
		AppConfig.mutex.release();
	}
}
