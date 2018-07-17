package folder_watcher;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class postValidation implements Runnable
{
	String pathString, jobId, clientId;
	
	postValidation(String pathString, String jobId, String clientId)
	{
		this.pathString = pathString;
		this.jobId = jobId;
		this.clientId = clientId;
	}
	
	void postValidationProcess() throws IOException, ParseException
	{
		try 
		{
			String jobFolder = pathString.substring(0, pathString.lastIndexOf("/ERROR"));
	        //String[] strList = folder.list();
	        //listOfFiles.length
	        
	        //System.out.println("Processing already present files in Job folder...\n");
			//consoleLog.log("Processing already present files in Job folder...\n");
			
			job j1 = new job();
			System.out.println("Job status:"+j1.job_status(jobId));
			
         	//boolean jobError =false;
			//job_restart:
			while(j1.job_status(jobId))//while(j1.job_status(jobId))
			{
				File folder = new File(pathString);
		        File[] listOfFiles = folder.listFiles();
		        //System.out.println("Exists:"+folder.exists());
		        
		        boolean folderError = false;
		        
		        if(folder.exists())
		        {
		        	folderError = false;
					for (int i = 0; i < listOfFiles.length; i++)
					{
						//System.out.println("Files List:"+listOfFiles[i].getName());
						if (listOfFiles[i].isFile()) 
						{
							//System.out.println("Extension:"+getFileNameWithoutExtension(listOfFiles[i]));
							//System.out.println("File/Folder Status:"+listOfFiles[i].isFile());
							
							String REGEX = jobId+"_CH\\d\\d|"+jobId+"_FM\\d\\d|"+jobId+"_BM\\d\\d|"+jobId+"_RM\\d\\d|"+jobId+"_PT\\d\\d";
							
							//regex matching
							Pattern p = Pattern.compile(REGEX);
							Matcher m = p.matcher(utilities.getFileNameWithoutExtension(listOfFiles[i]));   // get a matcher object
			    				
							if(m.matches() && (listOfFiles[i].getName().lastIndexOf(".docx") > 0))
							{
								String chName =  utilities.getFileNameWithoutExtension(listOfFiles[i]),inDStyleMap,exportMap;
								
								//System.out.println("(Thread 2)Docx File : " +chName);
								
								String urlParams = "jobId="+URLEncoder.encode(jobId,"UTF-8")+"&clientId="+URLEncoder.encode(clientId,"UTF-8")+"&chapter="+URLEncoder.encode(chName,"UTF-8");
								//consoleLog.log("URL:\"http://"+url_request.serverIp+"/maestro/getValStageDetails?"+urlParams + "\", type:\"GET\"\n");
								//System.out.println("URL:\"http://"+url_request.serverIp+"/maestro/getValStageDetails?"+urlParams + "\", type:\"GET\"\n");
								
								//http://172.16.1.25:8080/maestro/getValStageDetails?jobId=9781482298697_Yu&clientId=TF_HSS&chapter=9781138598928_Willard+Bohn_BM01
						    	String preEditResponse = url_request.urlRequestProcess("http://"+url_request.serverIp+"/maestro/getValStageDetails?"+urlParams,"GET","");        		
						        
						    	//consoleLog.log("preEditResponse(Thread 2):"+preEditResponse+"\n");
						        //System.out.println("preEditResponse(Thread 2):"+preEditResponse+"\n");
						        
						        if((preEditResponse != null) && (!preEditResponse.isEmpty()) && (!preEditResponse.equals("")))
								{
						        	JSONParser parser = new JSONParser();
									Object preEditObj = parser.parse(preEditResponse);
							        JSONObject jo = (JSONObject) preEditObj;
								    inDStyleMap = (String) jo.get("inDStyleMap");
								    exportMap = (String) jo.get("wdExportMap");
								    //System.out.println("preEditStatus : "+preEditStatus);
								    //System.out.println("(Thread 2)inDStyleMap : "+inDStyleMap+"\n");
								    //consoleLog.log("(Thread 2) : "+inDStyleMap+"\n");
								    if((exportMap != null) && (!exportMap.isEmpty()) && (inDStyleMap != null) && (!inDStyleMap.isEmpty()))
							        {
								    	if(exportMap.equals("true"))
								    	{
									    	System.out.println("(Thread 2)Docx File : " +chName);
											consoleLog.log("(Thread 2)Docx File : " +chName);
											System.out.println("(Thread 2)inDStyleMap : "+inDStyleMap+"\n");
										    consoleLog.log("(Thread 2) : "+inDStyleMap+"\n");
											//all parameters of content modelling stage true
											//Move manuscripts to error folder
								        	//File theDir = new File(pathString);
	//							        	if (!theDir.exists()) 
	//										{
	//							        		theDir.mkdir();
	//										}
										    
										    if(new File(jobFolder+"/"+chName+".docx").exists() && new File(jobFolder+"/"+chName+".xlsx").exists())
										    {
										    	//just now export map passed
										    	utilities.fileMove(pathString+"/"+chName+".docx",new File(pathString).getParent()+"/"+chName+".docx");
												if(utilities.fileCheck(pathString +"/"+chName+".xlsx"))
													utilities.fileMove(pathString+"/"+chName+".xlsx",new File(pathString).getParent()+"/"+chName+".xlsx");
										    }
										    else
										    {
										    	//export map passed already
										    	if(inDStyleMap.equals("true") && new File(pathString+"/"+chName+".docx").exists() && new File(pathString+"/"+chName+".xlsx").exists())
										        {
										    		//just now indesign import map passed after post validation
										    		utilities.fileMove(pathString+"/"+chName+".docx",new File(pathString).getParent()+"/"+chName+".docx");
													if(utilities.fileCheck(pathString +"/"+chName+".xlsx"))
														utilities.fileMove(pathString+"/"+chName+".xlsx",new File(pathString).getParent()+"/"+chName+".xlsx");
										        }
//										    	else if((inDStyleMap.equals("false") && new File(pathString+"/"+chName+".docx").exists() && new File(pathString+"/"+chName+".xlsx").exists()))
//										    	{
//										    		
//										    	}
										    }
										}
							        }
								}
											    				
//			    				if(jobFailErrorFun())
//			    				{}
							}
						}
						TimeUnit.SECONDS.sleep(2);
						//if(jobError)
							//continue job_restart;
					}
		        }
		        else
				{
		        	if(!folderError)
		        	{
		        		System.out.println("Error Folder does not exists.");
		        		folderError = true;
		        	}
//		        	if(job.jobFailErrorFun(jobId,clientId))
//					{
//						
//    				}
				}
				TimeUnit.MILLISECONDS.sleep(300);
			}
			System.out.println("Post Validation finished for job :"+jobId+"\n");
			consoleLog.log("Post Validation finished for job :"+jobId+"\n");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		} 
		catch (ParseException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (InterruptedException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() 
	{
		//System.out.println("Error folder.\n");
		try 
		{
			postValidationProcess();
		}
		catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}