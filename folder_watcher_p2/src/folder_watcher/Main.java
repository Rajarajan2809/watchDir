package folder_watcher;

//https://docs.oracle.com/javase/tutorial/essential/io/examples/WatchDir.java
//https://docs.oracle.com/javase/tutorial/essential/io/notification.html

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.ConnectException;
//import java.net.URLEncoder;
//for watch dirs
//import java.nio.file.Path;
//import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

//get file name from folder
//import java.io.File;
//import java.io.FilenameFilter;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

//import javax.script.ScriptEngine;
//import javax.script.ScriptEngineManager;

//for json parsing
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
//import org.json.simple.JSONObject;
//import org.json.simple.JSONValue;
//import org.json.simple.JSONObject;
import org.json.simple.parser.*;
//import org.json.simple.JSONObject;
//import org.json.simple.JSONValue;

//import com.google.common.collect.Iterators;

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
	
	public volatile static boolean mountError = false;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void main(String[] args) throws IOException, InterruptedException, ParseException, ConnectException,StringIndexOutOfBoundsException 
	{
		DateFormat dateFormat2 = new SimpleDateFormat("dd-MMM-yy hh:mm:ss aa");
		String dateString2 = dateFormat2.format(new Date()).toString();
		consoleLog.log("--------------------------------------------------------");
		System.out.println("Maestro Queuing System started at " + dateString2 + "...\n");
		consoleLog.log("Maestro Queuing System started at " + dateString2 + "...\n");
		// utilities U = new utilities();
		//ArrayList<Path> listPath = new ArrayList<Path>();
		AtomicInteger shared = new AtomicInteger(0);
		boolean mailTriggNet = true, mailTriggNet2 = true;
		File file = new File("jobs.txt");
		
		mount_thread m1 = new mount_thread();
		Thread mountThread = new Thread(m1, "Thread to initiate the web server.");
		mountThread.start();
		
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
			try
			{
				String osResp1 = utilities.mountDisk("172.16.1.2", "Copyediting", "maestroqs@cmpl.in", "M@est0123");
				//TimeUnit.SECONDS.sleep(15);
				String osResp2 = utilities.mountDisk("172.16.1.21", "comp_template", "maestroqs@cmpl.in", "M@est0123");
				//TimeUnit.SECONDS.sleep(15);
				String osResp3 = utilities.mountDisk("172.16.1.21", "COMP", "maestroqs@cmpl.in", "M@est0123");
				//TimeUnit.SECONDS.sleep(15);
				
				if ((osResp1.equals("Disk Found")) && (osResp2.equals("Disk Found")) && (osResp3.equals("Disk Found"))) 
				{
					if(!mailTriggNet2)
					{
						//back online
						mailTriggNet2 = true;
						consoleLog.log("API Server (172.16.1.25:8080) is online.");
						System.out.println("API Server (172.16.1.25:8080) is online.");
						
						consoleLog.log("Server mounted.....................");
						System.out.println("Server mounted.....................\n");
		        		
						mail mailObj = new mail("Net-ops", "SUCCESS", "MOUNT", "", "");
						Thread mailThread = new Thread(mailObj, "Mail Thread for Mount");
						mailThread.start();
						mountError = false;
					}
					
					// store list of paths in
					ArrayList<String> serialNo = new ArrayList<String>();
					ArrayList<String> jobId = new ArrayList<String>();
					ArrayList<String> clientId = new ArrayList<String>();
					ArrayList<String> templateType = new ArrayList<String>();
					ArrayList<String> noOfManuScripts = new ArrayList<String>();
					ArrayList<String> status = new ArrayList<String>();
					ArrayList<Boolean> newJob = new ArrayList<Boolean>();
					ArrayList<String> templateName = new ArrayList<String>();
					String response = url_request.urlRequestProcess("http://" + url_request.serverIp + "/maestro/getJobList", "GET", "");
					
					if ((response != null) && (!response.isEmpty()) && (!response.equals(""))) 
					{
						if(!mailTriggNet)
						{
							//back online
							mailTriggNet = true;
							consoleLog.log("API Server (172.16.1.25:8080) is online.");
							System.out.println("API Server (172.16.1.25:8080) is online.");
							
							mail m = new mail("Net-ops", "SUCCESS", "DB", "", "");
							Thread mailThread = new Thread(m, "Mail Thread for Template path mount");
							mailThread.start();
							
						}
						if ((response.charAt(1) != '{') && (response.length() > 5)) 
						{
							JSONParser parser = new JSONParser();
							Object obj = parser.parse(response);
							JSONArray jsonArr = (JSONArray) obj;
							Iterator itr2 = jsonArr.iterator();
	
							while (itr2.hasNext()) 
							{
								Iterator itr1 = ((Map) itr2.next()).entrySet().iterator();
								while (itr1.hasNext()) 
								{
									
									Map.Entry pair = (Entry) itr1.next();
									
									if (pair.getKey().equals("serialNo"))
										serialNo.add((String) pair.getValue());
									
									if (pair.getKey().equals("jobId"))
										jobId.add((String) pair.getValue());
	
									if (pair.getKey().equals("clientId"))
										clientId.add((String) pair.getValue());
	
									if (pair.getKey().equals("templateType"))
										templateType.add((String) pair.getValue());
									
									if (pair.getKey().equals("templateName"))
										templateName.add((String) pair.getValue());
									
									if (pair.getKey().equals("noOfManuScripts"))
										noOfManuScripts.add((String) pair.getValue());
	
									if (pair.getKey().equals("status"))
										status.add((String) pair.getValue());
	
									if (pair.getKey().equals("newJob"))
										newJob.add((boolean) pair.getValue());
								}
							}
						}
						
						// assigning threads/separate processing to each array in JSON
						for (int i = 0; i < jobId.size(); i++)
						{
							if (status.get(i).equals("OK")) 
							{
								job j1 = new job();
								if (!j1.job_status(jobId.get(i)) && (!serialNo.get(i).equals("")) && (!jobId.get(i).equals("")) && (!clientId.get(i).equals("")) && (!templateType.get(i).equals("")))// if(!listPath.contains(Paths.get(path.get(i))))
								{
									Map<String,String> jobMap = new HashMap< String,String>();
									jobMap = getDir(serialNo.get(i), jobId.get(i), clientId.get(i), templateType.get(i), templateName.get(i));
									
									/*String templatePath 	= jobMap.get("Template");
									String compositionPath 	= jobMap.get("Composition");
									String graphicsPath 	= jobMap.get("Graphics");
									String copyEditPath 	= jobMap.get("Copyediting");
									String mapPath		 	= jobMap.get("Map_path");
									String genSSPath 		= jobMap.get("Standard_stylesheet");
									String eqnPath 			= copyEditPath + "EQUATIONS/";*/
									
									dateString2 = dateFormat2.format(new Date()).toString();
									//System.out.println("dateString2 :" + dateString2);
									
									if(!mailTriggNet)
									{
										mailTriggNet = true;
										consoleLog.log("API Server (172.16.1.25:8080) is online at "+dateString2+".");
										System.out.println("API Server (172.16.1.25:8080) is online at "+dateString2+".");
									}
									
									dateString2 = dateFormat2.format(new Date()).toString();
									System.out.println("job start time :" + dateString2);
									
									consoleLog.log("jobId :" + jobId.get(i));
									System.out.println("jobId :" + jobId.get(i));
	
									consoleLog.log("clientId :" + clientId.get(i));
									System.out.println("clientId :" + clientId.get(i));
									
									consoleLog.log("catNo :" + serialNo.get(i));
									System.out.println("catNo :" + serialNo.get(i));
	
									consoleLog.log("noOfManuScripts :" + noOfManuScripts.get(i));
									System.out.println("noOfManuScripts :" + noOfManuScripts.get(i));
									
									consoleLog.log("copyEditPath :" + jobMap.get("Copyediting"));
									System.out.println("copyEditPath :" + jobMap.get("Copyediting"));
									
									consoleLog.log("templateName :" + templateName.get(i));
									System.out.println("templateName :" + templateName.get(i));
									
									consoleLog.log("templateType :" + templateType.get(i));
									System.out.println("templateType :" + templateType.get(i));
									
									consoleLog.log("genSSPath :" + jobMap.get("Standard_stylesheet"));
									System.out.println("genSSPath :" + jobMap.get("Standard_stylesheet"));
									
									consoleLog.log("templatePath :" + jobMap.get("Template"));
									System.out.println("templatePath :" + jobMap.get("Template"));
									
									consoleLog.log("compositionPath :" + jobMap.get("Composition"));
									System.out.println("compositionPath :" + jobMap.get("Composition"));
									
									consoleLog.log("mapPath :" + jobMap.get("Map_path"));
									System.out.println("mapPath :" + jobMap.get("Map_path"));
									
									consoleLog.log("graphicsPath :" + jobMap.get("Graphics"));
									System.out.println("graphicsPath :" + jobMap.get("Graphics"));
									
									//consoleLog.log("newJob :" + newJob.get(i));
									//System.out.println("newJob :" + newJob.get(i));
									
									
									//copy edit path validation
									if((jobMap.get("Copyediting") != null) && (!jobMap.get("Copyediting").equals("")))
									{
										//template path validation
										if((jobMap.get("Template") != null) && (!jobMap.get("Template").equals("")))
										{
											//composition path validation
											if((jobMap.get("Composition") != null) && (!jobMap.get("Composition").equals("")))
											{
												//graphics path validation
												if((jobMap.get("Graphics") != null) && (!jobMap.get("Graphics").equals("")))
												{
													//Map path validation
													if((jobMap.get("Map_path") != null) && (!jobMap.get("Map_path").equals("")))
													{
														//Map path validation
														if((jobMap.get("Standard_stylesheet") != null) && (!jobMap.get("Standard_stylesheet").equals("")))
														{
															jobMap.put("equation",jobMap.get("Copyediting") + "Equations/");							
															
															//copyedit path creation
															recursiveFolderCreate(jobMap.get("Copyediting"));
															
															//Template path creation
															recursiveFolderCreate(jobMap.get("Template"));
															
															//Composition path creation
															recursiveFolderCreate(jobMap.get("Composition"));
															
															//Graphics path creation
															recursiveFolderCreate(jobMap.get("Graphics"));
															
															//equation folder creation
															recursiveFolderCreate(jobMap.get("equation"));
															
									            			consoleLog.log("eqnPath :" + jobMap.get("equation") + "\n");
															System.out.println("eqnPath :" + jobMap.get("equation") + "\n");
															
															//eqn path validation
															if((jobMap.get("equation") != null) && (!jobMap.get("equation").equals("")))
															{
																String REGEX = "[\\s\\S]+[\\/\\\\]+[\\d]{9}_[\\w\\s\\S]+[\\/\\\\]+[\\d]{2}_MaestroReady+[\\/\\\\]{0,1}{1}$|[\\s\\S]+[\\/\\\\]+[\\d]{13}_[\\w\\s\\S]+[\\/\\\\]+[\\d]{2}_MaestroReady+[\\/\\\\]{0,1}{1}$";
																//regex matching
																Pattern p = Pattern.compile(REGEX);
																Matcher m = p.matcher(jobMap.get("Copyediting"));   // get a matcher object
																if(m.matches())
																{
																	j1.job_insert(jobId.get(i));
																	//System.out.println("newJob:" + newJob.get(i));
																	// pathFolder = System.getProperty ("user.home")+"/Desktop/MaestroReady";
																	// new job or already failed job
																	if (!newJob.get(i))
																	{
																		//utilities.folderMove(pathFolder + "/ERROR", pathFolder);
																		//utilities.delete(new File(pathFolder + "/ERROR"));
																	}
																	// consoleLog.log("Watch thread created.");
																	consoleLog.log("Job initiated for Job id : \"" + jobId.get(i) + "\",\nclientId : \"" + clientId.get(i) + "\",\nSerial No. : \"" + serialNo.get(i) + "\" and \nNo of manuscripts : \"" + noOfManuScripts.get(i) + "\"\n");
																	System.out.println("Job initiated for Job id : \"" + jobId.get(i) + "\",\nclientId : \"" + clientId.get(i) + "\",\nSerial No. : \"" + serialNo.get(i) + "\" and \nNo of manuscripts : \"" + noOfManuScripts.get(i) + "\"\n");
																	
			//														mail mailObj = new mail("Net-ops", "ERROR", "MOUNT", "", "");
			//														Thread mailThread = new Thread(mailObj, "Mail Thread for Mount");
			//														mailThread.start();
																	
																	//Map< String,Integer> jobMap = new HashMap< String,Integer>();
																	
																	System.out.println("Home:"+System.getProperty ("user.home")+"\\Desktop\\Maestro_QS");
																	
																	JSONObject pathUpdateJson = new JSONObject();
																	pathUpdateJson.put("jobId", jobId.get(i));
																	pathUpdateJson.put("clientId", clientId.get(i));
																	
																	/*String copyEditPath = jobMap.get("Copyediting");
																	copyEditPath = copyEditPath.replace(System.getProperty ("user.home")+"/Desktop/Maestro_QS", "//172.16.1.2"); //\\Volumes
																	copyEditPath = copyEditPath.replace('/', '\\');
																	pathUpdateJson.put("copyEditPath", copyEditPath);*/
																	
																	pathUpdateJson.put("copyEditPath", jobMap.get("Copyediting"));
																	
																	/*String graphicsPath = jobMap.get("Graphics");
																	graphicsPath = graphicsPath.replace(System.getProperty ("user.home")+"/Desktop/Maestro_QS", "//172.16.1.2"); //\\Volumes
																	graphicsPath = graphicsPath.replace('/', '\\');
																	pathUpdateJson.put("graphicsPath", graphicsPath);*/
																	
																	pathUpdateJson.put("graphicsPath", jobMap.get("Graphics"));
																	
																	String gssPath = jobMap.get("Standard_stylesheet");
																	gssPath = gssPath.replace(System.getProperty ("user.home")+"/Desktop/Maestro_QS", "//172.16.1.21"); //\\Volumes
																	gssPath = gssPath.replace('/', '\\');
																	pathUpdateJson.put("styleSheetPath", gssPath);
																	
																	//pathUpdateJson.put("styleSheetPath", jobMap.get("Standard_stylesheet"));
																	
																	/*String eqnPath = jobMap.get("equation");
																	eqnPath = eqnPath.replace(System.getProperty ("user.home")+"/Desktop/Maestro_QS", "//172.16.1.2"); //\\Volumes
																	eqnPath = eqnPath.replace('/', '\\');
																	pathUpdateJson.put("equationsPath", eqnPath);*/
																	
																	pathUpdateJson.put("equationsPath", jobMap.get("equation"));
																	
																	/*System.out.println("copyEditPath:"+jobMap.get("Copyediting"));
																	System.out.println("graphicsPath:"+jobMap.get("Graphics"));
																	System.out.println("mapPath:"+jobMap.get("Map_path"));
																	System.out.println("genSSPath:"+jobMap.get("Standard_stylesheet"));
																	System.out.println("eqnPath:"+jobMap.get("equation"));*/
																	
																	String mapPath = jobMap.get("Map_path");
																	mapPath = mapPath.replace(System.getProperty ("user.home")+"/Desktop/Maestro_QS", "\\\\172.16.1.21"); //\\Volumes
																	mapPath = mapPath.replace('/', '\\');
																	pathUpdateJson.put("mappingPath", mapPath);
																	//pathUpdateJson.put("mappingPath", jobMap.get("Map_path"));
																	
																	pathUpdateJson.put("templatePath", jobMap.get("Template"));
																	
																	String jsonText = JSONValue.toJSONString(pathUpdateJson);
																	System.out.println("jsonText:\n"+utilities.json_pretty_print(jsonText));
																	//String updateResponse = "";
																	String updateResponse = url_request.urlRequestProcess("http://" + url_request.serverIp + "/maestro/updateJobPath", "PUT", jsonText);
																	System.out.println("updateResponse:"+utilities.json_pretty_print(updateResponse));
																	
																	if ((updateResponse != null) && (!updateResponse.isEmpty()) && (!updateResponse.equals("")))
																	{
																		System.out.println("job params updated.\n\n");
																	}
																	else
																		System.out.println("job params not updated.\n\n");
																	
																	jobMap.put("jobId",jobId.get(i));
																	jobMap.put("clientId",clientId.get(i));
																	jobMap.put("serialNo",serialNo.get(i));
																	jobMap.put("manuscripts",noOfManuScripts.get(i));
																	jobMap.put("templateName",templateName.get(i));
																	jobMap.put("templateType",templateType.get(i));
																	
																	//job start
																	watchDir watchObj = new watchDir(jobMap, shared);
																	Thread watchThread1 = new Thread(watchObj, "Watch Thread for Parent folder" + Integer.toString(i));
																	watchThread1.start();
																	//listPath.add(Paths.get(copyEditPath));
																}
																else
																{
																	consoleLog.log("Job folder format didn't pass REGEX\n");
																	System.out.println("Job folder format didn't pass REGEX\n");
																}
															}
															else
															{
																consoleLog.log("equation path is either NULL or Invalid\n");
																System.out.println("equation path is either NULL or Invalid\n");
															}
														}
														else
														{
															consoleLog.log("Stylesheet path is either NULL or Invalid\n");
															System.out.println("Stylesheet path is either NULL or Invalid\n");
														}
													}
													else
													{
														consoleLog.log("map path is either NULL or Invalid\n");
														System.out.println("map path is either NULL or Invalid\n");
													}
												}
												else
												{
													consoleLog.log("graphics path is either NULL or Invalid\n");
													System.out.println("graphics path is either NULL or Invalid\n");
												}
											}
											else
											{
												consoleLog.log("composition path is either NULL or Invalid\n");
												System.out.println("composition path is either NULL or Invalid\n");
											}
										}
										else
										{
											consoleLog.log("template path is either NULL or Invalid\n");
											System.out.println("template path is either NULL or Invalid\n");
										}
									}
									else
									{
										consoleLog.log("Copyedit path is either NULL or Invalid\n");
										System.out.println("Copyedit path is either NULL or Invalid\n");
									}
								}
							}
						}
					}
					else 
					{
						if (mailTriggNet) // mail trigger once
						{
							dateString2 = dateFormat2.format(new Date()).toString();
							consoleLog.log(dateString2+"Invalid JSON received from API in \"getJobList\" query.\n");
							System.out.println(dateString2+"Invalid JSON received from API in \"getJobList\" query.\n");
							// mail to netops
							consoleLog.log("MOUNT ERROR mail sent to group \"netops\"");
							mail m = new mail("Net-ops", "ERROR", "DB", "", "");
							Thread mailThread = new Thread(m, "Mail Thread for Template path mount");
							mailThread.start();
							mailTriggNet = false;
							consoleLog.log("API Server (172.16.1.25:8080) is offline.\n");
							System.out.println("API Server (172.16.1.25:8080) is offline.\n");
						}
					}
				}
				else 
				{
					if (mailTriggNet2) //mail trigger once
					{
						String errorShare="";
	    				if(!osResp1.equals("Disk Found"))
	    					errorShare = "Copyediting(172.16.1.2)";
	    				
	    				if(!osResp2.equals("Disk Found"))
	    					errorShare = errorShare.isEmpty() ? "comp_template(172.16.1.21)" : errorShare + ",comp_template(172.16.1.21)";
	    				
	    				if(!osResp3.equals("Disk Found"))
	    					errorShare = errorShare.isEmpty() ? "COMP(172.16.1.21)" : errorShare + "and COMP(172.16.1.21)";
	    				
	    				System.out.println("SMB Share Mount error");
						consoleLog.log("SMB Share Mount error");
						// sample : sendMail("Net-ops", "", "MOUNT", "", "");
						mail mailObj = new mail("Net-ops", "ERROR", "MOUNT", "", errorShare);
						Thread mailThread = new Thread(mailObj, "Mail Thread for Mount");
						mailThread.start();
						mailTriggNet2 = false;
						mountError = true;
					}
				}
			}
			catch (Exception e) 
			{
				e.printStackTrace(System.out);
				// System.out.println(e.toString());
				consoleLog.log("Exception caught is :" + e.toString());
				System.out.println("Exception caught is :" + e.toString());
				TimeUnit.SECONDS.sleep(50);
				// String eXcept = e.toString().substring(0,e.toString().indexOf(":"));
				// e.printStackTrace();
				
				if(e.toString().indexOf(":") != -1)
				{
					switch (e.toString().substring(0, e.toString().indexOf(":"))) 
					{
						case "java.nio.file.NoSuchFileException": 
						{
							/*System.out.println("Directory does not exists");
							consoleLog.log("Directory does not exists");
							
							String osResp1 = utilities.mountDisk("172.16.1.2", "Copyediting", "maestroqs@cmpl.in", "M@est0123");
							String osResp2 = utilities.mountDisk("172.16.1.21", "comp_template", "maestroqs@cmpl.in", "M@est0123");
							String osResp3 = utilities.mountDisk("172.16.1.21", "COMP", "maestroqs@cmpl.in", "M@est0123");
							
							//String osResp4 = utilities.mountDisk("172.16.1.21", "COMP", "maestroqs@cmpl.in", "M@est0123");
							//String osResp = utilities.serverMount();
							System.out.println("osResp1:" + osResp1);
							consoleLog.log("Mount response1:" + osResp1);
							
							System.out.println("osResp2:" + osResp2);
							consoleLog.log("Mount response2:" + osResp2);
							
							System.out.println("osResp3:" + osResp3);
							consoleLog.log("Mount response3:" + osResp3);
							
							if ((osResp1.equals("Disk Found")) && (osResp2.equals("Disk Found")) && (osResp3.equals("Disk Found"))) 
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
								//mail mailObj = new mail("Net-ops", "ERROR", "MOUNT", "", "");
								//Thread mailThread = new Thread(mailObj, "Mail Thread for Mount");
								//mailThread.start();
							}*/
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
			}
			// time interval between two url requests
			Thread.sleep(500);
			// break;
		}
	}
	
	public static Map<String, String> getDir(String serialNo, String jobId, String clientId, String templateType,  String templateName)
	{
		Map<String,String> jobMap = new HashMap< String,String>();
		jobMap.put("Copyediting", "");
		jobMap.put("Composition", "");
		jobMap.put("Graphics", "");
		jobMap.put("Map_path", "");
		jobMap.put("Standard_stylesheet", "");
		JSONParser jsonParser = new JSONParser();
		String errParam = "";
		/*System.out.println("serialNo :"+serialNo);
		System.out.println("jobId :"+jobId);
		System.out.println("clientId :"+clientId);
		System.out.println("templateType :"+templateType);
		System.out.println("templateName :"+templateName);*/
		
//		String clientId = "TF_HSS", 
//        templateType = "TF_TS", 
//        serialNo = "223", 
//        jobId = "1234567899876_test qwe",
//        //templateName = "";//
//		templateName = "Standard_A_Sabon";
		List<String> files = new ArrayList<String>();
		files.add("dir.json");
		if(!templateName.equals(""))
			files.add("templ_dir.json");
		else
			jobMap.put("Template", "");
		
		//String[] files = {"dir.json"};
		
		for(String file1 : files)
		{
			try (FileReader reader = new FileReader(file1))
	        {
				switch(file1)
				{
					case "dir.json":
					{
			            //Read JSON file
						JSONObject object = (JSONObject) jsonParser.parse(reader);
						//Map<String,String> inputMap = new HashMap< String,String>();
						
						Set< Map.Entry< String,String> > st = jobMap.entrySet();
						for (Map.Entry< String,String> me:st)
						{
							JSONObject type = (JSONObject)object.get(me.getKey());
				            
				            if(type != null)
				            {
				            	if((me.getKey() == "Map_path") || (me.getKey() == "Standard_stylesheet"))
				            	{
				            		String path = (String) type.get(clientId);
				            		
				            		path = System.getProperty ("user.home") + path.replace("Volumes", "Desktop/Maestro_QS");
				            		
				            		//System.out.println(path);
				            		
				            		if(new File(path).exists())
				            		{
				            			jobMap.put(me.getKey(), path);
				            			//System.out.println(me.getKey()+" : "+path);
				            		}
				            	}
				            	else
				            	{
						            JSONObject title = (JSONObject) type.get(clientId);
						            if(title != null)
						            {
						            	JSONObject client = (JSONObject) title.get(templateType);
						            	if(client != null)
							            {
						            		//String ip = (String) client.get("ip");
						            		String path = (String) client.get("path");
						            		String format = (String) client.get("format");
						            		
						            		path = System.getProperty ("user.home") + path.replace("Volumes", "Desktop/Maestro_QS");
						            		//System.out.println("path:"+path);
						            		
						            		if(new File(path).exists())
						            		{
							            		format = format.replace("SNO",serialNo);
							            		format = format.replace("ISBN_AuthorName",jobId);
							            		path = path + format;
							            		//System.out.println("path:"+path);
							            		/*String tempPath = path;
							            		
							            		List<String> folderList = new ArrayList<>();
							            		
							            		while(!new File(tempPath).exists())
							            		{
							            			String folderName = (new File(tempPath)).getName();
							            			folderList.add(folderName);
							            			//System.out.println("parent:"+folderName);
							            			tempPath = (new File(tempPath).getParentFile()).getPath();
							            			//folderList.add(folderName);
							            		}
							            		
							            		for(int i=folderList.size()-1; i != -1; i--)
							            		{
							            			tempPath = tempPath+"/"+folderList.get(i);
							            			File theDir = new File(tempPath);
							            			//System.out.println("f1:"+theDir.getPath());
							            			while (!theDir.exists()) 
							            			{
							                    		theDir.mkdir();
							            			}
							                    }*/
							            		
							            		//if(new File(path).exists())
							            		{
							            			jobMap.put(me.getKey(), path);
							            			//System.out.println(me.getKey()+" : "+path);
							            		}
						            		}
							            }
						            	else
						            	{
						            		errParam = errParam + "\n* " + templateType+" is invalid.";
						            		System.out.println("client is null.");
							            	break;
						            	}
						            }
						            else
						            {
						            	errParam = errParam + "\n* " + clientId+" is invalid.";
						            	System.out.println("title is null.");
						            	break;
						            }
				            	}
				            }
				            else
				            {
				            	errParam = errParam + "\n* " + me.getKey()+" is invalid.";
				            	System.out.println("type is null.");
				            	break;
				            }
						}
			      	}
					break;
					
					case "templ_dir.json":
					{
						JSONObject object = (JSONObject) jsonParser.parse(reader);
						if(object != null)
						{
							JSONObject tempName = (JSONObject) object.get(clientId);
							if(tempName != null)
							{
								String stdTemplPath = (String) tempName.get(templateName);
								if(stdTemplPath != null)
								{
									stdTemplPath = System.getProperty ("user.home") + stdTemplPath.replace("Volumes", "Desktop/Maestro_QS");
									//System.out.println("stdTemplPath:"+stdTemplPath);
									if(new File(stdTemplPath).exists())
				            		{
										jobMap.put("Template", stdTemplPath);
										//System.out.println("Template : "+jobMap.get("Template"));
				            		}
								}
								else
								{
									errParam = errParam + "\n* " + templateName +" is invalid.";
									System.out.println("stdTemplPath is null.");
					            	break;
								}
							}
							else
							{
								errParam = errParam + "\n* " + clientId +" is invalid.";
								System.out.println("tempName is null.");
				            	break;
							}
						}
						else
						{
							errParam = errParam + "\n* \"templ_dir.json\" is invalid.";
							System.out.println("object is null.");
			            	break;
						}
					}
					break;
				}
	        } 
			catch (FileNotFoundException e) 
			{
	            e.printStackTrace();
	        } 
			catch (IOException e) 
			{
	            e.printStackTrace();
	        } 
			catch (ParseException e) 
			{
	            e.printStackTrace();
	        }
		}
		jobMap.put("error", errParam);
		return jobMap;
	}
	
	private static void recursiveFolderCreate(String path)
	{
		String tempPath = path;
		
		List<String> folderList = new ArrayList<>();
		
		while(!new File(tempPath).exists())
		{
			String folderName = (new File(tempPath)).getName();
			folderList.add(folderName);
			//System.out.println("parent:"+folderName);
			tempPath = (new File(tempPath).getParentFile()).getPath();
			//folderList.add(folderName);
		}
		
		for(int i=folderList.size()-1; i != -1; i--)
		{
			tempPath = tempPath+"/"+folderList.get(i);
			File theDir = new File(tempPath);
			//System.out.println("f1:"+theDir.getPath());
			while (!theDir.exists()) 
			{
        		theDir.mkdir();
			}
        }
	}
}
