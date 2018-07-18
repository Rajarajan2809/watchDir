package graphics_watcher;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URLEncoder;
//for watch dirs
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

//get file name from folder
//import java.io.File;
//import java.io.FilenameFilter;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

//import javax.script.ScriptEngine;
//import javax.script.ScriptEngineManager;

//for json parsing
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
//import org.json.simple.JSONObject;
import org.json.simple.parser.*;
//import org.json.simple.JSONObject;
//import org.json.simple.JSONValue;

import graphics_watcher.consoleLog;
import graphics_watcher.job;
import graphics_watcher.mail;
import graphics_watcher.utilities;
import graphics_watcher.service;

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
	
	//@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void main(String[] args) throws IOException, InterruptedException, ParseException, ConnectException,StringIndexOutOfBoundsException 
	{
		DateFormat dateFormat2 = new SimpleDateFormat("dd-MMM-yy hh:mm:ss aa");
		String dateString2 = dateFormat2.format(new Date()).toString();
		consoleLog.log("--------------------------------------------------------");
		System.out.println("Graphics folder watcher started at " + dateString2 + "...\n");
		consoleLog.log("Graphics folder watcher started at " + dateString2 + "...\n");
		// utilities U = new utilities();
		ArrayList<Path> listPath = new ArrayList<Path>();
		AtomicInteger shared = new AtomicInteger(0);
		boolean mailTriggNet = true, mailTriggNet2 = true;
		File file = new File("jobs.txt");
		if (file.exists() && !file.isDirectory()) 
		{
			// do something
			file.delete();
		}
		
		// osscript to mount disks
		// continous polling for getting Jobs with "INPROGRESS" status
		String currentJob = "";
		try 
		{
			// store list of paths in
			ArrayList<String> clientId = new ArrayList<String>();
			ArrayList<String> jobId = new ArrayList<String>();
			ArrayList<String> status = new ArrayList<String>();
			ArrayList<String> graphicsPath = new ArrayList<String>();
			
			String response = "";
			
			/*
			Sample JSON:
			{
				"Jobs": [{
						"name": "crc",
						"exec_type": "file",
						"path": "Macintosh HD:Users:comp:zinio:IN",
						"jsxFile": "Macintosh HD:Application:Adobe:Adobe InDesign CC 2018:Scripts:Scripts Panel:zinio:zinio.jsx"

					},
					{
						"name": "zinio",
						"exec_type": "folder",
						"extn": ".zip",
						"path": "Macintosh HD:Users:comp:zinio:IN",
						"jsxFile": "Macintosh HD:Application:Adobe:Adobe InDesign CC 2018:Scripts:Scripts Panel:zinio:zinio.jsx"
					}
				]
			}
			*/

			if ((response != null) && (!response.isEmpty()) && (!response.equals(""))) 
			{
				if(!mailTriggNet)
				{
					//back online
					
				}
				mailTriggNet = true;
				if (response.charAt(1) != '{') 
				{
					JSONParser parser = new JSONParser();
					Object obj = parser.parse(response);
					JSONArray jsonArr = (JSONArray) obj;
					// System.out.println(jsonArr);
					Iterator itr2 = jsonArr.iterator();

					while (itr2.hasNext()) 
					{
						Iterator itr1 = ((Map) itr2.next()).entrySet().iterator();
						while (itr1.hasNext()) 
						{
							Map.Entry pair = (Entry) itr1.next();
							// System.out.println(pair.getKey() + ":" + pair.getValue());

							if (pair.getKey().equals("jobId"))
								clientId.add((String) pair.getValue());
							
							if (pair.getKey().equals("clientId"))
								clientId.add((String) pair.getValue());

							if (pair.getKey().equals("status"))
								status.add((String) pair.getValue());

							if (pair.getKey().equals("graphicsPath"))
								graphicsPath.add((String) pair.getValue());

						}
					}
				}

				// assigning threads/separate processing to each array in JSON
				for (int i = 0; i < jobId.size(); i++) 
				{
					currentJob = jobId.get(i);
					if (status.get(i).equals("OK")) 
					{
						// System.out.println("no of files : "+getExtension(path,".docx").length);
						// String pathFolder = path.get(i); //testing
						// pathFolder =
						// pathFolder.substring(pathFolder.indexOf("//")+1,pathFolder.length());
						String pathFolder = graphicsPath.get(i);
						// String pathFolder =
						// path.get(i).substring(path.get(i).indexOf("//")+2,path.get(i).length());
						pathFolder = pathFolder.replace("\r\n", "");
						pathFolder = pathFolder.replace('\\', '/');
						pathFolder = pathFolder.substring(pathFolder.indexOf("//") + 2, pathFolder.length());
						pathFolder = pathFolder.substring(pathFolder.indexOf('/') + 1, pathFolder.length());
						pathFolder = "/Volumes/" + pathFolder;

						//String graphicsPathFolder = graphicsPath.get(i);
						graphicsPath.set(i, graphicsPath.get(i).replace("\r\n", ""));
						graphicsPath.set(i, graphicsPath.get(i).replace('\\', '/'));
						graphicsPath.set(i, graphicsPath.get(i).substring(graphicsPath.get(i).indexOf("//") + 2, graphicsPath.get(i).length()));
						graphicsPath.set(i, graphicsPath.get(i).substring(graphicsPath.get(i).indexOf('/') + 1, graphicsPath.get(i).length()));
						graphicsPath.set(i, "/Volumes/" + graphicsPath.get(i));
						
						// System.out.println("Path : "+pathFolder);
						job j1 = new job();
						// if the listPath already has the job, this case filed
						// System.out.println("Job status:"+!j1.job_status(jobId.get(i)));
						if (!j1.job_status(jobId.get(i)))// if(!listPath.contains(Paths.get(path.get(i))))
						{
								
							mailTriggNet = true;
							j1.job_insert(jobId.get(i));

							//consoleLog.log("Poll response : " + response + "\n");
							//System.out.println("Poll response : " + response + "\n");

							consoleLog.log("jobId :" + jobId.get(i));
							System.out.println("jobId :" + jobId.get(i));

							consoleLog.log("clientId :" + clientId.get(i));
							System.out.println("clientId :" + clientId.get(i));

//									consoleLog.log("path :" + path.get(i));
//									System.out.println("path :" + path.get(i));
							
							consoleLog.log("graphicsPath :" + graphicsPath.get(i));
							System.out.println("graphicsPath :" + graphicsPath.get(i));

							consoleLog.log("pathFolder :" + pathFolder + "\n");
							System.out.println("pathFolder :" + pathFolder + "\n");
							
							boolean graphPathError = false;
							
							if(graphicsPath.get(i).equals("") || graphicsPath.get(i).equals("-") || (!utilities.folderCheck(graphicsPath.get(i))
									&& !(graphicsPath.get(i).indexOf(jobId.get(i)) != -1)))
							{
								graphPathError = true;
							}
																
							System.out.println("graphPathError:"+graphPathError);
							
							//System.out.println("Path : " + pathFolder);
							// watchDir.getDocParam(jobId.get(i),
							// clientId.get(i))watchDir.getDocParam(jobId.get(i), clientId.get(i));
							// path is not present in watch dir list
							// try
							// check job folder exists and jobId and job folder names are same
							if (utilities.folderCheck(pathFolder) 
									&& (pathFolder.indexOf(jobId.get(i)) != -1)
									&& !graphPathError) 
							{
								consoleLog.log("Job initiated for Job id : \"" + jobId.get(i) + "\",\nclientId : \"" + clientId.get(i) + "\",\npath : \"" + pathFolder + "\"\n");
								service watchObj = new service(pathFolder, jobId.get(i), clientId.get(i), shared);
								Thread watchThread1 = new Thread(watchObj, "Watch Thread for Parent folder" + Integer.toString(i));
								watchThread1.start();
								
//										if(!new File(pathFolder+"/ERROR/").exists())
//										{
//											File theDir = new File(pathFolder+"/ERROR/");
//								        	if (!theDir.exists()) 
//											{
//								        		theDir.mkdir();
//											}
//										}
								
								listPath.add(Paths.get(graphicsPath.get(i)));
									
							}
							else 
							{
								
							}
						}
					}
				}
				// TimeUnit.SECONDS.sleep(1);
				/*
				 * if(java.lang.Thread.activeCount() == 3) { listPath.clear(); }
				 */
				// break;
			}
			else 
			{
				consoleLog.log("Invalid JSON reeceived from API in \"getJobList\" query.\n");
				System.out.println("Invalid JSON reeceived from API in \"getJobList\" query.\n");
			}
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
					if (utilities.serverMount().equals("Disk Found")) 
					{
						// Directory not exists
						System.out.println(e.toString().substring(e.toString().lastIndexOf(":")+1));
						
						//System.out.println(new File(e.toString().substring(e.toString().lastIndexOf(":")+1)).exists());
						mail mailObj = new mail(URLEncoder.encode("CRC Team", "UTF-8"), "DIRECTORY", currentJob, "", e.toString().substring(e.toString().lastIndexOf(":")+1));
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
						mail mailObj = new mail("Net-ops", "ERROR", "MOUNT", "", "");
						Thread mailThread = new Thread(mailObj, "Mail Thread for Mount");
						mailThread.start();
					}
				}
				break;

				case "java.net.SocketException": 
				{
					System.out.println("socket connection refuse error");
					consoleLog.log("socket connection refuse error");
					// sample : sendMail("Net-ops", "", "DB", "", "");
					mail mailObj = new mail("Net-ops", "ERROR", "DB", "", "");
					Thread mailThread = new Thread(mailObj, "Mail Thread for Socket");
					mailThread.start();
				}
			// break;

				case "java.net.ConnectException": 
				{
					consoleLog.log("connection refuse error\n");
					System.out.println("connection refuse error\n");

					if (mailTriggNet) 
					{
						mail mailObj = new mail("Net-ops", "ERROR", "DB", "", "");
						Thread mailThread = new Thread(mailObj, "Mail Thread for DB Error");
						mailThread.start();
						
						//flag for single mail while offline
						mailTriggNet = false;
						
//							String mailIdJson = utilities.fileRead("maestroqs_support.json");
//							if ((mailIdJson != null) && (!mailIdJson.isEmpty()) && (!mailIdJson.equals(""))) 
//							{
//								JSONParser parser = new JSONParser();
//								Object preEditObj = parser.parse(mailIdJson);
//								JSONObject jo = (JSONObject) preEditObj;
//								String mailIds = (String) jo.get("mail_id");
//	
//								// sample : sendMail("Net-ops", "", "DB", "", "");
//								// mail mailObj = new mail("Net-ops", "", "DB", "", "");
//								ArrayList<String> mail_id = mail.mailIdParse(mailIds);
//								for (int i = 0; i < mail_id.size(); i++) 
//								{
//									mail mailObj = new mail("Net-ops", "ERROR", "DB", "", "");
//									Thread mailThread = new Thread(mailObj, "Mail Thread for DB Error");
//									mailThread.start();
//									//mail.sendMail("Net-ops", "ERROR", "DB", "", "");
//									// Thread mailThread = new Thread(mailObj, "Mail Thread"+Integer.toString(i));
//									// mailThread.start();
//								}
//							} 
//							else 
//							{
//								consoleLog.log("JSON Parse failed for JSON on maestroqs_support.json\n\n");
//								System.out.println("JSON Parse failed for JSON on maestroqs_support.json");
//							}
					}
					// mailObj.mailProcess("Net-ops","ERROR","DB","");
				}
				break;

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
