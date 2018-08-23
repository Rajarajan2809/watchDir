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
import java.util.concurrent.atomic.AtomicInteger;

public class postValidation implements Runnable
{
	String pathString, jobId, clientId;
	int errFlag;
	private AtomicInteger postValCounter;
	postValidation(String pathString, String jobId, String clientId, AtomicInteger postValCounter)
	{
		this.pathString = pathString;
		this.jobId = jobId;
		this.clientId = clientId;
		this.postValCounter = postValCounter;
		this.errFlag = 0;// 0 -> error, 1 -> no error, 2 already error
	}
	
	void postValidationProcess() throws IOException, ParseException, NullPointerException
	{
		try
		{
			String jobFolder = pathString.substring(0, pathString.lastIndexOf("ERROR"));
			//int errFlag = 0; // 0 -> error, 1 -> no error, 2 already error 
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
				while(postValCounter.get() != 0)
		        {
					TimeUnit.SECONDS.sleep(1);
		        }

				postValCounter.incrementAndGet();
				File folder = new File(pathString);
//		        System.out.println("Exists:"+folder.exists());
//		        System.out.println("errFlag(jobId:"+jobId+"):"+errFlag);
//		        consoleLog.log("errFlag:"+errFlag);
//		        System.out.println("folder:"+folder+"\n\n");
		        //boolean folderError = false;
		        
		        //System.out.println("\"ERROR\" folder does not exist for job : "+jobId+".");
        		//consoleLog.log("\"ERROR\" folder does not exist for job : "+jobId+".");
		        //TimeUnit.SECONDS.sleep(1);
		        //String osResp1 = utilities.mountDisk("172.16.1.2", "Copyediting", "maestroqs@cmpl.in", "M@est0123");
		        //TimeUnit.SECONDS.sleep(1);
        		//String osResp2 = utilities.mountDisk("172.16.1.21", "comp_template", "maestroqs@cmpl.in", "M@est0123");
        		//TimeUnit.SECONDS.sleep(1);
    			//String osResp3 = utilities.mountDisk("172.16.1.21", "COMP", "maestroqs@cmpl.in", "M@est0123");
    			//TimeUnit.SECONDS.sleep(1);
    			
//    			System.out.println("(post validation)(jobId:"+jobId+")osResp1:" + osResp1);
//    			consoleLog.log("(post validation)(jobId:"+jobId+")Mount response1:" + osResp1);
//    			
//    			System.out.println("(post validation)(jobId:"+jobId+")osResp2:" + osResp2);
//    			consoleLog.log("(post validation)Mount response2:" + osResp2);
//    			
//    			System.out.println("(post validation)(jobId:"+jobId+")osResp3:" + osResp3);
//    			consoleLog.log("(post validation)Mount response3:" + osResp3);
    			
    			//if ((osResp1.equals("Disk Found")) && (osResp2.equals("Disk Found")) && (osResp3.equals("Disk Found"))) 
    			//{
    				//TimeUnit.SECONDS.sleep(6);
//    				System.out.println("Disk mounted.");
//	        		consoleLog.log("Disk mounted.");
	        		if(!new File(pathString).exists())
					{
						File theDir = new File(pathString);
			        	if (!theDir.exists()) 
						{
			        		theDir.mkdir();
						}
					}
		        
			        if(folder.exists() && !Main.mountError)
			        {
			        	if(errFlag == 2)
			        	{
//			        		System.out.println("Server mounted.....................");
//			        		mail mailObj = new mail("Net-ops", "SUCCESS", "MOUNT", "", "");
//							Thread mailThread = new Thread(mailObj, "Mail Thread for Mount");
//							mailThread.start();
			        	}
			        	errFlag = 1;
			        	File[] listOfFiles = folder.listFiles();
			        	TimeUnit.SECONDS.sleep(3);
			        	
			        	//System.out.println("listOfFiles.length:"+listOfFiles.length);
			        	
						for (int i = 0; i < listOfFiles.length; i++)
						{
							//TimeUnit.SECONDS.sleep(2);
							//System.out.println("Files List:"+listOfFiles[i].getName());
							if (listOfFiles[i].isFile()) 
							{
								String REGEX = jobId+"_CH\\d\\d|"+jobId+"_FM\\d\\d|"+jobId+"_BM\\d\\d|"+jobId+"_RM\\d\\d|"+jobId+"_PT\\d\\d";
								
								//regex matching
								Pattern p = Pattern.compile(REGEX);
								Matcher m = p.matcher(utilities.getFileNameWithoutExtension(listOfFiles[i]));   // get a matcher object
				    				
								if(m.matches() && (listOfFiles[i].getName().lastIndexOf(".docx") > 0))
								{
									String chName =  utilities.getFileNameWithoutExtension(listOfFiles[i]);
									String urlParams = "jobId="+URLEncoder.encode(jobId,"UTF-8")+"&clientId="+URLEncoder.encode(clientId,"UTF-8")+"&chapter="+URLEncoder.encode(chName,"UTF-8");
							    	String preEditResponse = url_request.urlRequestProcess("http://"+url_request.serverIp+"/maestro/getValStageDetails?"+urlParams,"GET","");        		
						        	
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
									}
								}
							}
							TimeUnit.SECONDS.sleep(2);
						}
			        }
		      /*  }
		        else
				{
		        	if(errFlag == 1)
		        	{
		        			errFlag = 2;
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
//							mail mailObj = new mail("Net-ops", "ERROR", "MOUNT", "", errorShare);
//							Thread mailThread = new Thread(mailObj, "Mail Thread for Mount");
//							mailThread.start();
		        	}
//		        	if(job.jobFailErrorFun(jobId,clientId))
//					{
//						
//    				}
				}*/
		        //System.out.println("loop finished.");
				//TimeUnit.MILLISECONDS.sleep(1000);
		        postValCounter.decrementAndGet();
		        TimeUnit.SECONDS.sleep(5);
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