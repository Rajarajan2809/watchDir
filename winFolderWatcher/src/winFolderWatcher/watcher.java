package winFolderWatcher;

//import java.io.File;
//import java.io.FileReader;
import java.io.IOException;

//for watch dirs
//import java.text.DateFormat;
//import java.text.SimpleDateFormat;

//get file name from folder
//import java.io.File;
//import java.io.FilenameFilter;

//import java.util.ArrayList;
//import java.util.Date;
import java.util.concurrent.TimeUnit;
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.Iterator;
//import java.util.Map;
//import java.util.Map.Entry;

//import javax.script.ScriptEngine;
//import javax.script.ScriptEngineManager;

//for json parsing
//import org.json.simple.JSONArray;
////import org.json.simple.JSONObject;
//import org.json.simple.parser.*;
//import org.json.simple.JSONObject;
//import org.json.simple.JSONValue;

import winFolderWatcher.consoleLog;
//import graphics_watcher.job;
import winFolderWatcher.mail;
import winFolderWatcher.utilities;
import winFolderWatcher.service;

//https://www.geeksforgeeks.org/parse-json-java/
//simple json jar download :http://www.java2s.com/Code/Jar/j/Downloadjsonsimple11jar.htm
//https://www.javatpoint.com/collections-in-java
public class watcher 
{
	/*
	 * public static File[] getExtension(String dirName,final String ext) { File dir
	 * = new File(dirName); return dir.listFiles(new FilenameFilter() { public
	 * boolean accept(File dir, String filename) { return filename.endsWith(ext); }
	 * }); }
	 */
	
	static boolean mountError = false;
	
	//@SuppressWarnings("rawtypes")
	public static void main(String[] args) throws IOException, InterruptedException,StringIndexOutOfBoundsException 
	{
		//DateFormat dateFormat2 = new SimpleDateFormat("dd-MMM-yy hh:mm:ss aa");
		//String dateString2 = dateFormat2.format(new Date()).toString();
		//consoleLog.log("--------------------------------------------------------");
		//System.out.println("Wi folder watcher started at " + dateString2 + "...\n");
		//consoleLog.log("Graphics folder watcher started at " + dateString2 + "...\n");
		// utilities U = new utilities();
		//AtomicInteger shared = new AtomicInteger(0);
		//boolean mailTriggNet = true;
		
		// osscript to mount disks
		// continous polling for getting Jobs with "INPROGRESS" status
		String currentJob = "";
		try 
		{
			String pathFolder = "";
			//boolean fileFolderStatus =false;
			
			//if(utilities.fileCheck(pathFolder))
				//fileFolderStatus = true;
			
//							System.out.println("fileFolderStatus:"+fileFolderStatus);
//							System.out.println("exec_type.equals(\"Folder\"):"+exec_type.equals("Folder"));
//							System.out.println("utilities.folderCheck(pathFolder):"+utilities.folderCheck(pathFolder));
			
				System.out.println("thread started");
				service watchObj = new service(pathFolder);
				Thread watchThread1 = new Thread(watchObj, "Watch Thread for Parent folder 1");
				watchThread1.start();
//										if(!new File(pathFolder+"/ERROR/").exists())
//										{
//											File theDir = new File(pathFolder+"/ERROR/");
//								        	if (!theDir.exists()) 
//											{
//								        		theDir.mkdir();
//											}
//										}
								
								/*listPath.add(Paths.get(graphicsPath.get(i)));
									
							}
							else 
							{
								
							}*/
						//}
					
				//}
				// TimeUnit.SECONDS.sleep(1);
				/*
				 * if(java.lang.Thread.activeCount() == 3) { listPath.clear(); }
				 */
				// break;
			//}
//			else 
//			{
//				consoleLog.log("Invalid JSON reeceived from API in \"getJobList\" query.\n");
//				System.out.println("Invalid JSON reeceived from API in \"getJobList\" query.\n");
//			}
			// TimeUnit.SECONDS.sleep(1);
			// break;
		}
		catch (Exception e) 
		{
			// e.printStackTrace(System.out);
			// System.out.println(e.toString());
			consoleLog.log("Exception caught is :" + e.toString());
			// String eXcept = e.toString().substring(0,e.toString().indexOf(":"));
			// e.printStackTrace();

			switch (e.toString().substring(0, e.toString().indexOf(":"))) 
			{
				case "java.nio.file.NoSuchFileException": 
				{
					System.out.println("Directory does not exists");
					consoleLog.log("Directory does not exists");
					if (utilities.mountDisk("172.16.1.2", "OEO", "rajarajan", "test@123").equals("Disk Found")) 
					{
						// Directory not exists
						System.out.println(e.toString().substring(e.toString().lastIndexOf(":")+1));
						
						//System.out.println(new File(e.toString().substring(e.toString().lastIndexOf(":")+1)).exists());
						mail mailObj = new mail("rajarajan@codemantra.in", "DIRECTORY", currentJob, "", e.toString().substring(e.toString().lastIndexOf(":")+1));
						Thread mailThread = new Thread(mailObj, "Mail Thread for file/directory");
						mailThread.start();
						// mailObj.mailProcess("Template","DIRECTORY_NOT_EXISTS","DIRECTORY","", "");
						// mailObj.mailProcess(URLEncoder.encode("CRC Team",
						// "UTF-8"),"DIRECTORY_NOT_EXISTS","DIRECTORY","",
						// e.toString().substring(e.toString().indexOf(":"),e.toString().indexOf("\n")));
					} 
					else 
					{
						System.out.println("SMB Share Mount error");
						consoleLog.log("SMB Share Mount error");
						// sample : sendMail("Net-ops", "", "MOUNT", "", "");
						mail mailObj = new mail("rajarajan@codemantra.in", "NETWORK - ERROR", "MOUNT", "", "");
						Thread mailThread = new Thread(mailObj, "Mail Thread for Mount");
						mailThread.start();
					}
				}
				break;

				
			// break;

				
					// mailObj.mailProcess("Net-ops","ERROR","DB","");

				case "java.lang.IndexOutOfBoundsException": 
				{
					System.out.println("IndexOutOfBoundsException error");
					consoleLog.log("IndexOutOfBoundsException error");
				}
				break;

				case "java.lang.NullException": 
				{
					System.out.println("null error");
					consoleLog.log("null error");
				}
				break;
			}
		}
		// time interval between two url requests
		TimeUnit.SECONDS.sleep(1);
		// break;
	}
}
