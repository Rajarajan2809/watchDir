package graphics_watcher;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

//for watch dirs
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
//import org.json.simple.JSONObject;
import org.json.simple.parser.*;
//import org.json.simple.JSONObject;
//import org.json.simple.JSONValue;

import graphics_watcher.consoleLog;
//import graphics_watcher.job;
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
	
	@SuppressWarnings("rawtypes")
	public static void main(String[] args) throws IOException, InterruptedException, ParseException,StringIndexOutOfBoundsException 
	{
		DateFormat dateFormat2 = new SimpleDateFormat("dd-MMM-yy hh:mm:ss aa");
		String dateString2 = dateFormat2.format(new Date()).toString();
		consoleLog.log("--------------------------------------------------------");
		System.out.println("Graphics folder watcher started at " + dateString2 + "...\n");
		consoleLog.log("Graphics folder watcher started at " + dateString2 + "...\n");
		// utilities U = new utilities();
		AtomicInteger shared = new AtomicInteger(0);
		boolean mailTriggNet = true;
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
			ArrayList<String> name = new ArrayList<String>();
			ArrayList<String> exec_type = new ArrayList<String>();
			ArrayList<String> path = new ArrayList<String>();
			ArrayList<String> jsxFile = new ArrayList<String>();
			
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
			FileReader reader = new FileReader("jobs.json");
			JSONParser jsonParser = new JSONParser();
			Object obj = jsonParser.parse(reader);
			 
            JSONArray response = (JSONArray) obj;
            //System.out.println(response);
             
            //Iterate over employee array
			if ((response != null) && (!response.isEmpty())) 
			{
				if(!mailTriggNet)
				{
					//back online
					
				}
				mailTriggNet = true;
				
				// System.out.println(jsonArr);
				Iterator itr2 = response.iterator();

				while (itr2.hasNext()) 
				{
					Iterator itr1 = ((Map) itr2.next()).entrySet().iterator();
					while (itr1.hasNext()) 
					{
						Map.Entry pair = (Entry) itr1.next();
						//System.out.println(pair.getKey() + ":" + pair.getValue());

						if (pair.getKey().equals("name"))
							name.add((String) pair.getValue());
						
						if (pair.getKey().equals("exec_type"))
							exec_type.add((String) pair.getValue());

						if (pair.getKey().equals("path"))
							path.add((String) pair.getValue());

						if (pair.getKey().equals("jsxFile"))
							jsxFile.add((String) pair.getValue());

					}
				}

				// assigning threads/separate processing to each array in JSON
				for (int i = 0; i < name.size(); i++) 
				{
					currentJob = name.get(i);
					//System.out.println("name :" + name.get(i));
					
						// System.out.println("no of files : "+getExtension(path,".docx").length);
						// String pathFolder = path.get(i); //testing
						// pathFolder =
						// pathFolder.substring(pathFolder.indexOf("//")+1,pathFolder.length());
						String pathFolder = path.get(i);
						// String pathFolder =
						// path.get(i).substring(path.get(i).indexOf("//")+2,path.get(i).length());
						pathFolder = pathFolder.replace("\r\n", "");
						pathFolder = pathFolder.replace('\\', '/');
						pathFolder = pathFolder.substring(pathFolder.indexOf("//") + 2, pathFolder.length());
						pathFolder = pathFolder.substring(pathFolder.indexOf('/') + 1, pathFolder.length());
						pathFolder = "/Volumes/" + pathFolder + "/IN";

						//String graphicsPathFolder = graphicsPath.get(i);
						path.set(i, path.get(i).replace(":", "/"));
						path.set(i, path.get(i).replace("\r\n", ""));
						path.set(i, path.get(i).replace('\\', '/'));
						path.set(i, path.get(i).substring(path.get(i).indexOf("//") + 2, path.get(i).length()));
						path.set(i, path.get(i).substring(path.get(i).indexOf('/') + 1, path.get(i).length()));
						path.set(i, "/Volumes/" + path.get(i));
						
						// System.out.println("Path : "+pathFolder);
						//job j1 = new job();
						// if the listPath already has the job, this case filed
						// System.out.println("Job status:"+!j1.job_status(jobId.get(i)));
						//if (!j1.job_status(name.get(i)))// if(!listPath.contains(Paths.get(path.get(i))))
						//{
								
							mailTriggNet = true;
							//j1.job_insert(jobId.get(i));

							//consoleLog.log("Poll response : " + response + "\n");
							//System.out.println("Poll response : " + response + "\n");

							consoleLog.log("name :" + name.get(i));
							System.out.println("name :" + name.get(i));

							consoleLog.log("exec_type :" + exec_type.get(i));
							System.out.println("exec_type :" + exec_type.get(i));

//							consoleLog.log("path :" + path.get(i));
//							System.out.println("path :" + path.get(i));

							consoleLog.log("pathFolder :" + pathFolder );
							System.out.println("pathFolder :" + pathFolder);
							
							consoleLog.log("jsxFile :" + jsxFile.get(i)+ "\n");
							System.out.println("jsxFile :" + jsxFile.get(i)+ "\n");
							
							//boolean graphPathError = false;
							
							//path validation
//							if(path.get(i).equals("") || path.get(i).equals("-") || (!utilities.folderCheck(path.get(i))
//									&& !(path.get(i).indexOf(name.get(i)) != -1)))
//							{
//								graphPathError = true;
//							}
																
							//System.out.println("graphPathError:"+graphPathError);
							
							//System.out.println("Path : " + pathFolder);
							// watchDir.getDocParam(jobId.get(i),
							// clientId.get(i))watchDir.getDocParam(jobId.get(i), clientId.get(i));
							// path is not present in watch dir list
							// try
							// check job folder exists and jobId and job folder names are same
							/*if (utilities.folderCheck(pathFolder) 
									&& (pathFolder.indexOf(jobId.get(i)) != -1)
									&& !graphPathError) 
							{*/
								//consoleLog.log("Job initiated for Job id : \"" + jobId.get(i) + "\",\nclientId : \"" + clientId.get(i) + "\",\npath : \"" + pathFolder + "\"\n");
							String mountStatus = utilities.mountDisk("172.16.1.2", "OEO", "rajarajan", "test@123");
							
							boolean fileFolderStatus =false;
							
							if(((exec_type.get(i).equals("File") && utilities.fileCheck(pathFolder))) || ((exec_type.get(i).equals("Folder") && utilities.folderCheck(pathFolder))))
								fileFolderStatus = true;
							
//							System.out.println("fileFolderStatus:"+fileFolderStatus);
//							System.out.println("exec_type.equals(\"Folder\"):"+exec_type.equals("Folder"));
//							System.out.println("utilities.folderCheck(pathFolder):"+utilities.folderCheck(pathFolder));
							
							
							if(mountStatus.equals("Disk Found") && fileFolderStatus)
							{
								System.out.println("thread started");
								service watchObj = new service(name.get(i), pathFolder,  exec_type.get(i), shared);
								Thread watchThread1 = new Thread(watchObj, "Watch Thread for Parent folder" + Integer.toString(i));
								watchThread1.start();
							}
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
