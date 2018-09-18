package graphics_watcher;

import java.io.FileReader;
import java.io.IOException;

//for watch dirs
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

//for json parsing
import org.json.simple.JSONArray;
import org.json.simple.parser.*;

import graphics_watcher.mount_thread;
import graphics_watcher.consoleLog;
import graphics_watcher.mail;
import graphics_watcher.utilities;
import graphics_watcher.service;

//https://docs.oracle.com/javase/tutorial/essential/io/examples/WatchDir.java
//https://docs.oracle.com/javase/tutorial/essential/io/notification.html

//https://www.geeksforgeeks.org/parse-json-java/
//simple json jar download :http://www.java2s.com/Code/Jar/j/Downloadjsonsimple11jar.htm
//https://www.javatpoint.com/collections-in-java
public class watcher 
{
	static boolean mountError = false;
	
	@SuppressWarnings("rawtypes")
	public static void main(String[] args) throws IOException, InterruptedException, ParseException,StringIndexOutOfBoundsException 
	{
		DateFormat dateFormat2 = new SimpleDateFormat("dd-MMM-yy hh:mm:ss aa");
		String dateString2 = dateFormat2.format(new Date()).toString();
		consoleLog.log("--------------------------------------------------------");
		System.out.println("Graphics folder watcher started at " + dateString2 + "...\n");
		consoleLog.log("Graphics folder watcher started at " + dateString2 + "...\n");
		AtomicInteger shared = new AtomicInteger(0);
		boolean mailTriggNet = true;
		String currentJob = "";
		
		mount_thread m1 = new mount_thread();
		Thread mountThread = new Thread(m1, "Thread to kill ip not connected error.");
		mountThread.start();
		
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
			FileReader reader = new FileReader(System.getProperty ("user.home")+"/graphicsqs/jobs.json");
			JSONParser jsonParser = new JSONParser();
			Object obj = jsonParser.parse(reader);
			 
            JSONArray response = (JSONArray) obj;
             
            //Iterate over employee array
			if ((response != null) && (!response.isEmpty())) 
			{
				if(!mailTriggNet)
				{
					//back online
					
				}
				mailTriggNet = true;
				Iterator itr2 = response.iterator();
				while (itr2.hasNext()) 
				{
					Iterator itr1 = ((Map) itr2.next()).entrySet().iterator();
					while (itr1.hasNext()) 
					{
						Map.Entry pair = (Entry) itr1.next();

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
					String pathFolder = path.get(i);
					
					pathFolder = pathFolder.replace("\r\n", "");
					pathFolder = pathFolder.replace('\\', '/');
					pathFolder = pathFolder.substring(pathFolder.indexOf("//") + 2, pathFolder.length());
					pathFolder = pathFolder.substring(pathFolder.indexOf('/') + 1, pathFolder.length());
					pathFolder = "/Volumes/" + pathFolder ;

					//String graphicsPathFolder = graphicsPath.get(i);
					path.set(i, path.get(i).replace(":", "/"));
					path.set(i, path.get(i).replace("\r\n", ""));
					path.set(i, path.get(i).replace('\\', '/'));
					path.set(i, path.get(i).substring(path.get(i).indexOf("//") + 2, path.get(i).length()));
					path.set(i, path.get(i).substring(path.get(i).indexOf('/') + 1, path.get(i).length()));
					path.set(i, "/Volumes/" + path.get(i));
						
					mailTriggNet = true;
					consoleLog.log("name :" + name.get(i));
					System.out.println("name :" + name.get(i));

					consoleLog.log("exec_type :" + exec_type.get(i));
					System.out.println("exec_type :" + exec_type.get(i));

					consoleLog.log("pathFolder :" + pathFolder );
					System.out.println("pathFolder :" + pathFolder);
							
					consoleLog.log("jsxFile :" + jsxFile.get(i)+ "\n");
					System.out.println("jsxFile :" + jsxFile.get(i)+ "\n");
							
					String mountStatus = utilities.mountDisk("172.16.1.21", "COMP", "graphicsqs", "@Op=0eja");
					boolean fileFolderStatus =false;
							
					if(((exec_type.get(i).equals("File") && utilities.fileCheck(pathFolder))) 
							|| ((exec_type.get(i).equals("Folder") && utilities.folderCheck(pathFolder))))
					{
						fileFolderStatus = true;
					}
					if(mountStatus.equals("Disk Found") && fileFolderStatus)
					{
						System.out.println("thread started");
						service watchObj = new service(name.get(i), pathFolder,  exec_type.get(i), jsxFile.get(i), shared);
						Thread watchThread1 = new Thread(watchObj, "Watch Thread for Parent folder" + Integer.toString(i));
						watchThread1.start();
					}
				}
			}
			else 
			{
				consoleLog.log("Invalid JSON reeceived from API in \"getJobList\" query.\n");
				System.out.println("Invalid JSON reeceived from API in \"getJobList\" query.\n");
			}
		}
		catch (Exception e) 
		{
			consoleLog.log("Exception caught is :" + e.toString());
			
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
						mail mailObj = new mail("rajarajan@codemantra.in", "DIRECTORY", currentJob, "", e.toString().substring(e.toString().lastIndexOf(":")+1));
						Thread mailThread = new Thread(mailObj, "Mail Thread for file/directory");
						mailThread.start();
					} 
					else 
					{
						System.out.println("SMB Share Mount error");
						consoleLog.log("SMB Share Mount error");
						mail mailObj = new mail("rajarajan@codemantra.in", "NETWORK - ERROR", "MOUNT", "", "");
						Thread mailThread = new Thread(mailObj, "Mail Thread for Mount");
						mailThread.start();
					}
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
		TimeUnit.SECONDS.sleep(1);
	}
}
