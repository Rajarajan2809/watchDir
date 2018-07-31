package folder_watcher;

//https://docs.oracle.com/javase/tutorial/essential/io/examples/WatchDir.java
//https://docs.oracle.com/javase/tutorial/essential/io/notification.html

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

import folder_watcher.consoleLog;
import folder_watcher.job;
import folder_watcher.mail;
import folder_watcher.url_request;
import folder_watcher.utilities;
import folder_watcher.watchDir;
import folder_watcher.webServer;

//https://www.geeksforgeeks.org/parse-json-java/
//simple json jar download :http://www.java2s.com/Code/Jar/j/Downloadjsonsimple11jar.htm
//https://www.javatpoint.com/collections-in-java
public class Main 
{
	/*
	 * public static File[] getExtension(String dirName,final String ext) { File dir
	 * = new File(dirName); return dir.listFiles(new FilenameFilter() { public
	 * boolean accept(File dir, String filename) { return filename.endsWith(ext); }
	 * }); }
	 */
	
	static boolean mountError = false;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void main(String[] args) throws IOException, InterruptedException, ParseException, ConnectException,StringIndexOutOfBoundsException 
	{
		DateFormat dateFormat2 = new SimpleDateFormat("dd-MMM-yy hh:mm:ss aa");
		String dateString2 = dateFormat2.format(new Date()).toString();
		consoleLog.log("--------------------------------------------------------");
		System.out.println("Maestro Queuing System started at " + dateString2 + "...\n");
		consoleLog.log("Maestro Queuing System started at " + dateString2 + "...\n");
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
		
		//web server to find server status
		webServer server = new webServer();
		Thread webServerThread = new Thread(server, "Thread to initiate the web server.");
		webServerThread.start();
		
		consoleLog.log("Polling process started.");

		for (;;) 
		{
			// osscript to mount disks
			// continous polling for getting Jobs with "INPROGRESS" status
			String currentJob = "";
			try
			{
				// store list of paths in
				ArrayList<String> jobId = new ArrayList<String>();
				ArrayList<String> clientId = new ArrayList<String>();
				ArrayList<String> path = new ArrayList<String>();
				ArrayList<String> noOfManuScripts = new ArrayList<String>();
				ArrayList<String> status = new ArrayList<String>();
				ArrayList<Boolean> newJob = new ArrayList<Boolean>();
				ArrayList<String> graphicsPath = new ArrayList<String>();
				ArrayList<String> equationsPath = new ArrayList<String>();

				String response = url_request.urlRequestProcess("http://" + url_request.serverIp + "/maestro/getJobList", "GET", "");
				// url_request url = new
				// url_request("http://localhost:8888/automation1/watchDir.php","GET","");
				// System.out.println("Query response : "+response);

				if ((response != null) && (!response.isEmpty()) && (!response.equals(""))) 
				{
					if(!mailTriggNet)
					{
						//back online
						mailTriggNet = true;
						consoleLog.log("API Server (172.16.1.25:8080) is online.");
						System.out.println("API Server (172.16.1.25:8080) is online.");
					}
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
									jobId.add((String) pair.getValue());

								if (pair.getKey().equals("clientId"))
									clientId.add((String) pair.getValue());

								if (pair.getKey().equals("path"))
									path.add((String) pair.getValue());

								if (pair.getKey().equals("noOfManuScripts"))
									noOfManuScripts.add((String) pair.getValue());

								if (pair.getKey().equals("status"))
									status.add((String) pair.getValue());

								if (pair.getKey().equals("newJob"))
									newJob.add((boolean) pair.getValue());

								if (pair.getKey().equals("graphicsPath"))
									graphicsPath.add((String) pair.getValue());

								if (pair.getKey().equals("equationsPath"))
									equationsPath.add((String) pair.getValue());
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
							String pathFolder = path.get(i);
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
							
							//equations path
							equationsPath.set(i, equationsPath.get(i).replace("\r\n", ""));
							equationsPath.set(i, equationsPath.get(i).replace('\\', '/'));
							equationsPath.set(i, equationsPath.get(i).substring(equationsPath.get(i).indexOf("//") + 2, equationsPath.get(i).length()));
							equationsPath.set(i, equationsPath.get(i).substring(equationsPath.get(i).indexOf('/') + 1, equationsPath.get(i).length()));
							equationsPath.set(i, "/Volumes/" + equationsPath.get(i));

							// System.out.println("Path : "+pathFolder);
							job j1 = new job();
							// if the listPath already has the job, this case filed
							// System.out.println("Job status:"+!j1.job_status(jobId.get(i)));
							if (!j1.job_status(jobId.get(i)))// if(!listPath.contains(Paths.get(path.get(i))))
							{
								String osResp = utilities.serverMount();
								System.out.println("osResp:" + osResp);
								consoleLog.log("Mount response:" + osResp);
								if (osResp.equals("Disk Found")) 
								{
									if(!mailTriggNet)
									{
										mailTriggNet = true;
										consoleLog.log("API Server (172.16.1.25:8080) is online.");
										System.out.println("API Server (172.16.1.25:8080) is online.");
									}
									j1.job_insert(jobId.get(i));

									//consoleLog.log("Poll response : " + response + "\n");
									//System.out.println("Poll response : " + response + "\n");

									consoleLog.log("jobId :" + jobId.get(i));
									System.out.println("jobId :" + jobId.get(i));

									consoleLog.log("clientId :" + clientId.get(i));
									System.out.println("clientId :" + clientId.get(i));

//									consoleLog.log("path :" + path.get(i));
//									System.out.println("path :" + path.get(i));

									consoleLog.log("noOfManuScripts :" + noOfManuScripts.get(i));
									System.out.println("noOfManuScripts :" + noOfManuScripts.get(i));

									consoleLog.log("newJob :" + newJob.get(i));
									System.out.println("newJob :" + newJob.get(i));
									
									consoleLog.log("graphicsPath :" + graphicsPath.get(i));
									System.out.println("graphicsPath :" + graphicsPath.get(i));

									consoleLog.log("equationsPath :" + equationsPath.get(i));
									System.out.println("equationsPath :" + equationsPath.get(i));
									
									consoleLog.log("pathFolder :" + pathFolder + "\n");
									System.out.println("pathFolder :" + pathFolder + "\n");
									
									boolean graphPathError = false, eqnPathError= false;
									
									if(graphicsPath.get(i).equals("") || graphicsPath.get(i).equals("-") || (!utilities.folderCheck(graphicsPath.get(i))
											&& !(graphicsPath.get(i).indexOf(jobId.get(i)) != -1)))
									{
										graphPathError = true;
									}
									
									if(graphicsPath.get(i).equals("") || equationsPath.get(i).equals("-") || (!utilities.folderCheck(equationsPath.get(i))
											&& !(equationsPath.get(i).indexOf(jobId.get(i)) != -1)))
									{
										eqnPathError = true;
									}
									
//									System.out.println("1:"+graphicsPath.get(i).equals("-"));
//									System.out.println("2:"+!utilities.folderCheck(graphicsPath.get(i)));
//									System.out.println("3:"+!(graphicsPath.get(i).indexOf(jobId.get(i)) != -1));
//									
//									System.out.println("4:"+equationsPath.get(i).equals("-"));
//									System.out.println("5:"+!utilities.folderCheck(equationsPath.get(i)));
//									System.out.println("6:"+!(equationsPath.get(i).indexOf(jobId.get(i)) != -1));
									
									System.out.println("eqnPathError:"+eqnPathError);
									System.out.println("graphPathError:"+graphPathError);
									if(eqnPathError)
										break;
									
									//System.out.println("Path : " + pathFolder);
									// watchDir.getDocParam(jobId.get(i),
									// clientId.get(i))watchDir.getDocParam(jobId.get(i), clientId.get(i));
									// path is not present in watch dir list
									// try
									// check job folder exists and jobId and job folder names are same
									String jobParams[] = job.getDocParam(jobId.get(i), clientId.get(i));
									if (utilities.folderCheck(pathFolder) 
											&& (pathFolder.indexOf(jobId.get(i)) != -1)
											&& !jobParams[1].equals("") 
											&& !jobParams[2].equals("")
											&& !jobParams[3].equals("") 
											&& utilities.folderCheck(jobParams[1])
											&& utilities.folderCheck(jobParams[2]) 
											&& utilities.fileCheck(jobParams[3])
											&& !graphPathError
											&& !eqnPathError) 
									{
										System.out.println("newJob:" + newJob.get(i));
										// pathFolder = System.getProperty ("user.home")+"/Desktop/MaestroReady";
										// new job or already failed job
										if (!newJob.get(i)) 
										{
											//utilities.folderMove(pathFolder + "/ERROR", pathFolder);
											//utilities.delete(new File(pathFolder + "/ERROR"));
										}
										// consoleLog.log("Watch thread created.");
										consoleLog.log("Job initiated for Job id : \"" + jobId.get(i) + "\",\nclientId : \"" + clientId.get(i) + "\",\npath : \"" + pathFolder + "\" and \nNo of manuscripts : \"" + noOfManuScripts.get(i) + "\"\n");
										watchDir watchObj = new watchDir(pathFolder, noOfManuScripts.get(i), jobId.get(i), clientId.get(i), shared);
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
										
//										watchObj = new watchDir(pathFolder+"/ERROR/", noOfManuScripts.get(i), jobId.get(i), clientId.get(i), shared);
//										Thread watchThread2 = new Thread(watchObj, "Watch Thread for ERROR folder in parent directory" + Integer.toString(i));
//										watchThread2.start();
										
										listPath.add(Paths.get(path.get(i)));
									}
									else 
									{
										//j1 = new job();
							         	j1.job_update(jobId.get(i));
							         	
										String errParam = "";
										System.out.println("utilities.folderCheck(pathFolder):" + utilities.folderCheck(pathFolder));

										// copy edit path validation
										if (!utilities.folderCheck(pathFolder) || (pathFolder.indexOf(jobId.get(i)) == -1)) 
										{
											// System.out.println("1");
											errParam = "\n* Copyedit Path is invalid";
										}

										// graphics path validation
										if (graphPathError) 
										{
											// System.out.println("2");
											errParam = (errParam.isEmpty() ? "" : errParam + ",\n") + "* Graphics Path is invalid";
											//graphicsErrFlag = true;
										}

										// equations path validation
										if (eqnPathError) 
										{
											// System.out.println("2");
											errParam = (errParam.isEmpty() ? "" : errParam + ",\n") + "* Equations Path is invalid";
											//equationErrFlag = true;
										}

										// template path validation
										if (jobParams[1].equals("") || !utilities.folderCheck(jobParams[1])) 
										{
											// System.out.println("2");
											errParam = (errParam.isEmpty() ? "" : errParam + ",\n")	+ "* Template Path is invalid";
											//equationErrFlag = true;
										}

										// Maestro map path validation
										if (jobParams[2].equals("") || !utilities.folderCheck(jobParams[2])) 
										{
											// System.out.println("3");
											errParam = (errParam.isEmpty() ? "" : errParam + ",\n")	+ "* Maestro Map Path is invalid";
										}

										// Standard style sheet path validation
										if (jobParams[3].equals("") || !utilities.fileCheck(jobParams[3])) 
										{
											// System.out.println("4");
											errParam = (errParam.isEmpty() ? "" : errParam + ",\n") + "* Standard StyleSheet Path is invalid";
										}

										errParam = errParam + ".\n\n";
										JSONObject obj = new JSONObject();
										obj.put("jobId", jobId.get(i));
										obj.put("clientId", clientId.get(i));
										obj.put("status", "FAILED");

										// sample : sendMail("CRC Team", "JOB_FAIL", "9781138556850_Ilyas_CH01", "",
										// errParam);
										// changed from "CRC Team" to "Pre-editing" - rajkannan
										mail mailObj = new mail(URLEncoder.encode("Pre-editing", "UTF-8"), "JOB_FAIL",jobId.get(i), "", errParam);
										Thread mailThread1 = new Thread(mailObj, "Mail Thread for file/directory");
										mailThread1.start();

										if (graphPathError) 
										{
											mailObj = new mail(URLEncoder.encode("Graphics", "UTF-8"), "JOB_FAIL", jobId.get(i), "", errParam);
											Thread mailThread2 = new Thread(mailObj, "Mail Thread for file/directory");
											mailThread2.start();
										}

										if (eqnPathError) 
										{
											mailObj = new mail(URLEncoder.encode("CRC Team", "UTF-8"), "JOB_FAIL", jobId.get(i), "", errParam);
											Thread mailThread3 = new Thread(mailObj, "Mail Thread for file/directory");
											mailThread3.start();
										}

										// mailObj.mailProcess(URLEncoder.encode("CRC Team",
										// "UTF-8"),"DIRECTORY_NOT_EXISTS","DIRECTORY","", errParam);
										// mailObj.mailProcess("Template","JOB_FAIL",jobId.get(i),"",errParam);

										System.out.println("Job finished WITH ERROR at " + dateString2 + " for jobId:" + jobId + ", clientId:" + clientId + " and folder location:" + pathFolder);
										consoleLog.log("Job finished WITH ERROR at \"" + dateString2 + "\" for jobId:\"" + jobId + "\", clientId:\"" + clientId + "\" and folder location:\""  + pathFolder + "\"");

										String jsonText = JSONValue.toJSONString(obj);
										// System.out.println(jsonText);
										// jobStatus update
										consoleLog.log("URL : \"http://" + url_request.serverIp + "/maestro/updateJobStatus\", type : \"PUT\" and content : \"" + jsonText + "\"");
										String webResponse = url_request.urlRequestProcess("http://" + url_request.serverIp + "/maestro/updateJobStatus", "PUT", jsonText);
										System.out.println("URL response : " + webResponse);
										consoleLog.log("URL response : " + webResponse);
									}
								}

								
								else 
								{
									if (mailTriggNet2) // mail trigger once
									{
										// mail to netops
										consoleLog.log("MOUNT ERROR mail sent to group \"netops\"");
										// smaple : sendMail("Net-ops","rajarajan@codemantra.in", "", "MOUNT", "", "");
										mail m = new mail("Net-ops", "ERROR", "MOUNT", "", "");
										// m.mailProcess("Net-ops", "ERROR", "MOUNT", "", "");
										Thread mailThread = new Thread(m, "Mail Thread for Template path mount");
										mailThread.start();
										mailTriggNet2 = false;
									}
									// else
									// mail already triggered
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
					if (mailTriggNet) // mail trigger once
					{
						consoleLog.log("Invalid JSON received from API in \"getJobList\" query.\n");
						System.out.println("Invalid JSON received from API in \"getJobList\" query.\n");
						// mail to netops
						consoleLog.log("MOUNT ERROR mail sent to group \"netops\"");
						// smaple : sendMail("Net-ops","rajarajan@codemantra.in", "", "MOUNT", "", "");
						mail m = new mail("Net-ops", "ERROR", "DB", "", "");
						// m.mailProcess("Net-ops", "ERROR", "MOUNT", "", "");
						Thread mailThread = new Thread(m, "Mail Thread for Template path mount");
						mailThread.start();
						mailTriggNet = false;
						consoleLog.log("API Server (172.16.1.25:8080) is offline.\n");
						System.out.println("API Server (172.16.1.25:8080) is offline.\n");
					}
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
}
