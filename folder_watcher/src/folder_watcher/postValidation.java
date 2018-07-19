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
			String jobFolder = pathString.substring(0, pathString.lastIndexOf("ERROR"));
	        //String[] strList = folder.list();
	        //listOfFiles.length
	        
	        //System.out.println("Processing already present files in Job folder...\n");
			//consoleLog.log("Processing already present files in Job folder...\n");
			
			job j1 = new job();
			System.out.println("Job status(PV):"+j1.job_status(jobId));
			System.out.println("pathString:"+pathString);
			

			
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
						//TimeUnit.SECONDS.sleep(2);
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
								String chName =  utilities.getFileNameWithoutExtension(listOfFiles[i]);
								
								//System.out.println("(Thread 2)Docx File : " +chName);
								
								String urlParams = "jobId="+URLEncoder.encode(jobId,"UTF-8")+"&clientId="+URLEncoder.encode(clientId,"UTF-8")+"&chapter="+URLEncoder.encode(chName,"UTF-8");
								//consoleLog.log("URL:\"http://"+url_request.serverIp+"/maestro/getValStageDetails?"+urlParams + "\", type:\"GET\"\n");
								//System.out.println("URL:\"http://"+url_request.serverIp+"/maestro/getValStageDetails?"+urlParams + "\", type:\"GET\"\n");
								
								//http://172.16.1.25:8080/maestro/getValStageDetails?jobId=9781482298697_Yu&clientId=TF_HSS&chapter=9781138598928_Willard+Bohn_BM01
						    	String preEditResponse = url_request.urlRequestProcess("http://"+url_request.serverIp+"/maestro/getValStageDetails?"+urlParams,"GET","");        		
						        
//						    	consoleLog.log("URL:\"http://"+url_request.serverIp+"/maestro/getChapterEquation?"+urlParams + "\", type:\"GET\"\n");
//					    		System.out.println("URL:\"http://"+url_request.serverIp+"/maestro/getChapterEquation?"+urlParams + "\", type:\"GET\"\n");
					    		
					        	//url_request.urlRequestProcess("http://"+url_request.serverIp+"/maestro/getChapterEquation?"+urlParams,"GET","");        		
					        	//String eqnResponse = url_request.urlRequestProcess("http://"+url_request.serverIp+"/maestro/getChapterEquation?"+urlParams,"GET","");
					        	//System.out.println("eqnResponse(PV):"+json_pretty_print(eqnResponse)+"\n");
					        	//consoleLog.log("eqnResponse(PV):"+json_pretty_print(eqnResponse)+"\n");
						        
//					        	consoleLog.log("preEditResponse(PV):"+json_pretty_print(preEditResponse)+"\n");
//						        System.out.println("preEditResponse(PV):"+json_pretty_print(preEditResponse)+"\n");
					        	
						        if((preEditResponse != null) && (!preEditResponse.isEmpty()) && (!preEditResponse.equals("")))
								{
						        	String inddImportMap;//,exportMap,eqnStatus,preeditStatus,styleSheetfileStatus;
						        	//Boolean eqnFolderStatus =  false;
						        	JSONParser parser = new JSONParser();
									Object preEditObj = parser.parse(preEditResponse);
							        JSONObject jo = (JSONObject) preEditObj;
								    inddImportMap = (String) jo.get("inDStyleMap");
								    
								    if((inddImportMap != null) && (!inddImportMap.isEmpty()) && inddImportMap.equals("true"))
								    {
								    	utilities.fileMove(pathString+chName+".docx",jobFolder+chName+".docx");
										if(utilities.fileCheck(pathString +chName+".xlsx"))
											utilities.fileMove(pathString +chName+".xlsx",jobFolder+chName+".xlsx");
								    }
								    
								    
//								    exportMap = (String) jo.get("wdExportMap");
//								    preeditStatus = (String) jo.get("status");
								    
								    //Equation validation
//								    JSONParser parser1 = new JSONParser();
//									Object eqnObj = parser1.parse(eqnResponse);
//							        JSONObject jo1 = (JSONObject) eqnObj;
//								    eqnStatus = (String) jo1.get("isEquationExists");
//								    
//								    System.out.println("eqnStatus:"+eqnStatus);
								    
//								    if(eqnStatus.equals("true"))
//								    {
//								    	if(new File(pathString+"/Equations/"+chName).isDirectory())
//								    	{
//								    		consoleLog.log("(PV)This chapter has \"Equations\" and equations are present in "+pathString+"/Equations/"+chName+"\n");
//								    		System.out.println("(PV)This chapter has \"Equations\" and equations are present in "+pathString+"/Equations/"+chName+"\n");
//								    		eqnFolderStatus = true;
//								    	}
//								    	else
//								    	{
//								    		consoleLog.log("(PV)This chapter has \"Equations\" but equations are not present in "+pathString+"/Equations/"+chName+"\n");
//								    		System.out.println("(PV)This chapter has \"Equations\" and equations are not present in "+pathString+"/Equations/"+chName+"\n");
//								    		eqnFolderStatus = false;
//								    		
//								    	}
//								    }
//								    else if(eqnStatus.equals("false"))
//								    {
//								    	consoleLog.log("(PV)This chapter does not have \"Equations\"\n");
//							    		System.out.println("(PV)This chapter does not have \"Equations\"\n");
//							    		eqnFolderStatus = true;
//								    }
								    
								    
								    //System.out.println("preEditStatus : "+preEditStatus);
								    //System.out.println("(Thread 2)inDStyleMap : "+inDStyleMap+"\n");
								    //consoleLog.log("(Thread 2) : "+inDStyleMap+"\n");
//								    if((exportMap != null) && (!exportMap.isEmpty()) && (inDStyleMap != null) && (!inDStyleMap.isEmpty())
//								    	&& (preeditStatus != null) && (!preeditStatus.isEmpty()) && eqnFolderStatus && (true))
//							        {
//								    	if(exportMap.equals("true"))
//								    	{
//									    	System.out.println("(Thread 2)Docx File : " +chName);
//											consoleLog.log("(Thread 2)Docx File : " +chName);
//											System.out.println("(Thread 2)inDStyleMap : "+inDStyleMap+"\n");
//										    consoleLog.log("(Thread 2) : "+inDStyleMap+"\n");
//											//all parameters of content modelling stage true
//											//Move manuscripts to error folder
//								        	//File theDir = new File(pathString);
//	//							        	if (!theDir.exists()) 
//	//										{
//	//							        		theDir.mkdir();
//	//										}
//										    
//										    //char arr["hello"];
//										    
//										    if(new File(jobFolder+"/"+chName+".docx").exists() && new File(jobFolder+"/"+chName+".xlsx").exists())
//										    {
//										    	//just now export map passed
//										    	utilities.fileMove(pathString+"/"+chName+".docx",jobFolder+"/"+chName+".docx");
//												if(utilities.fileCheck(pathString +"/"+chName+".xlsx"))
//													utilities.fileMove(pathString+"/"+chName+".xlsx",jobFolder+"/"+chName+".xlsx");
////										    	utilities.fileMove(pathString+"/"+chName+".docx",new File(pathString).getParent()+"/"+chName+".docx");
////												if(utilities.fileCheck(pathString +"/"+chName+".xlsx"))
////													utilities.fileMove(pathString+"/"+chName+".xlsx",new File(pathString).getParent()+"/"+chName+".xlsx");
//										    }
//										    else
//										    {
//										    	//export map passed already
//										    	if(inDStyleMap.equals("true") && new File(pathString+"/"+chName+".docx").exists() && new File(pathString+"/"+chName+".xlsx").exists())
//										        {
//										    		//just now indesign import map passed after post validation
////										    		utilities.fileMove(pathString+"/"+chName+".docx",new File(pathString).getParent()+"/"+chName+".docx");
////													if(utilities.fileCheck(pathString +"/"+chName+".xlsx"))
////														utilities.fileMove(pathString+"/"+chName+".xlsx",new File(pathString).getParent()+"/"+chName+".xlsx");
//										    		utilities.fileMove(pathString+"/"+chName+".docx",jobFolder+"/"+chName+".docx");
//													if(utilities.fileCheck(pathString +"/"+chName+".xlsx"))
//														utilities.fileMove(pathString+"/"+chName+".xlsx",jobFolder+"/"+chName+".xlsx");
//										        }
////										    	else if((inDStyleMap.equals("false") && new File(pathString+"/"+chName+".docx").exists() && new File(pathString+"/"+chName+".xlsx").exists()))
////										    	{
////										    		
////										    	}
//										    }
//										}
//							        }
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
		        //System.out.println("loop finished.");
				//TimeUnit.MILLISECONDS.sleep(1000);
		        TimeUnit.SECONDS.sleep(1);
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
	
//	private static String json_pretty_print(String json)
//	{
//		if(!json.isEmpty())
//		{
//			json = json.replace("{","{\n    ");
//			json = json.replace(",",",\n    ");
//			json = json.replace("}","\n}");
//			//System.out.println("JSON:"+json);
//			return json;
//		}
//		else
//			return "";
//	}
}