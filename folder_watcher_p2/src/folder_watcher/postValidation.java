package folder_watcher;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.util.concurrent.atomic.AtomicInteger;

public class postValidation implements Runnable
{
	String pathString, jobId, clientId,templateName, templatePath;
	int errFlag,NoOfManuscripts;
	private AtomicInteger postValCounter;
	postValidation(Map <String,String>jobParams, AtomicInteger postValCounter)
	{
		this.pathString = jobParams.get("Copyediting");
		this.jobId = jobParams.get("jobId");
		this.clientId = jobParams.get("clientId");
		this.postValCounter = postValCounter;
		this.NoOfManuscripts = Integer.parseInt(jobParams.get("manuscripts"));
		this.templateName = jobParams.get("templateName");
		this.templatePath = jobParams.get("Template");
		this.errFlag = 0;// 0 -> error, 1 -> no error, 2 already error
	}
	
	void postValidationProcess() throws IOException, ParseException, NullPointerException
	{
		try
		{
			String jobFolder = pathString;
			pathString = pathString + "ERROR/";
			
			//String[] prevValue = new String[NoOfManuscripts];
			Map<String, List<String>> docParams = new HashMap<String, List<String>>();
			int errFlag = 0; // 0 -> error, 1 -> no error, 2 already error »»»»‚‚⁄⁄⁄··°°°
	        //String[] strList = folder.list();
	        //listOfFiles.length
	        
	        //System.out.println("Processing already present files in Job folder...\n");
			//consoleLog.log("Processing already present files in Job folder...\n");
			
			job j1 = new job();
			System.out.println("Job status(PV):"+j1.job_status(jobId));
			System.out.println("Error folder:"+pathString);
			

			
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
				//System.out.println("pathString : "+pathString);
				//System.out.println("jobFolder : "+jobFolder);
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
			        	//TimeUnit.SECONDS.sleep(3);
			        	
			        	//System.out.println("listOfFiles.length:"+listOfFiles.length);
			        	
			        	
						for (int i = 0; i < listOfFiles.length; i++)
						{
							TimeUnit.SECONDS.sleep(2);
							//System.out.println("Files List:"+listOfFiles[i].getName());
							if (listOfFiles[i].isFile()) 
							{
								String REGEX = jobId+"_CH\\d\\d|"+jobId+"_FM\\d\\d|"+jobId+"_BM\\d\\d|"+jobId+"_RM\\d\\d|"+jobId+"_PT\\d\\d";
								
								//regex matching
								Pattern p = Pattern.compile(REGEX);
								Matcher m = p.matcher(FilenameUtils.getBaseName(listOfFiles[i].getName()));   // get a matcher object
				    			
								//System.out.println("mnName : "+listOfFiles[i].getName());
								//System.out.println("extn : "+FilenameUtils.getExtension(listOfFiles[i].getName()));
								
								if(m.matches() && (FilenameUtils.getExtension(listOfFiles[i].getName()).equals("docx")))
								{
									String chName =  utilities.getFileNameWithoutExtension(listOfFiles[i]);
									String urlParams = "jobId="+URLEncoder.encode(jobId,"UTF-8")+"&clientId="+URLEncoder.encode(clientId,"UTF-8")+"&chapter="+URLEncoder.encode(chName,"UTF-8");
							    	String preEditResponse = url_request.urlRequestProcess("http://"+url_request.serverIp+"/maestro/getValStageDetails?"+urlParams,"GET","");        		
						        	
							    	//System.out.println("chName : "+chName);
							    	//System.out.println("urlParams : "+urlParams);
							    	//System.out.println("preEditResponse : "+preEditResponse);
							    	
							        if((preEditResponse != null) && (!preEditResponse.isEmpty()) && (!preEditResponse.equals("")))
									{
							        	//inddImportMap;//,exportMap,eqnStatus,preeditStatus,styleSheetfileStatus;
							        	//Boolean eqnFolderStatus =  false;
							        	JSONParser parser = new JSONParser();
										Object preEditObj = parser.parse(preEditResponse);
								        JSONObject jo = (JSONObject) preEditObj;
								        String inddImportMap = (String) jo.get("inDStyleMap"),exportMap = (String) jo.get("wdExportMap"),tempFileStatus = "false";
								        
								        //System.out.println("exportMap : "+exportMap);
								        //System.out.println("inddImportMap : "+inddImportMap);
								        
								        List<String> values = new ArrayList<String>();
								        
							        	if(docParams.containsKey(FilenameUtils.getBaseName(listOfFiles[i].getName())))
							        		values = docParams.get(FilenameUtils.getBaseName(listOfFiles[i].getName()));
							        		
							            if((exportMap != null) && (!exportMap.isEmpty()))
									    {
									    	if(exportMap.equals("true"))
									        {
									    		//System.out.println("t1:"+docParams.containsKey(FilenameUtils.getBaseName(listOfFiles[i].getName())));
									    		//System.out.println("size : "+values.size());
									    		//if(values.size() > 0)
									    			//System.out.println("prev value:"+values.get(0));
									    		if(docParams.containsKey(FilenameUtils.getBaseName(listOfFiles[i].getName())) && (values.size() > 0) && (values.get(0).equals("false")))
									    		{
									    			utilities.fileMove(pathString+chName+".docx",jobFolder+chName+".docx");
										        	if(utilities.fileCheck(pathString +chName+".xlsx"))
										        		utilities.fileMove(pathString +chName+".xlsx",jobFolder+chName+".xlsx");
									    		}
									        }
									    	if(docParams.containsKey(FilenameUtils.getBaseName(listOfFiles[i].getName())) && (values.size() == 3))
									    		values.set(0,exportMap);
									    	else
									    		values.add(exportMap);
									    	//System.out.println("prev value:"+values.get(0));
									    }
								        
							            if(exportMap.equals("true"))
							            {
							            	if(templateName.equals(""))
							            	{
							            		File tempDir = new File(templatePath);
							            		
							            		//System.out.println("Template path(Error) : "+templatePath);
							            		
							            		File[] listOfFiles1 = tempDir.listFiles();
							            		for(int j=0; j < listOfFiles1.length; j++)
							            		{
							            			if(listOfFiles1[j].isFile() && (FilenameUtils.getExtension(listOfFiles1[j].toString()).equals("idml")))
							            			{
							            				String mnsSuffix = chName.substring(chName.lastIndexOf('_'),chName.lastIndexOf('_')+3);
							            				String tempFile = FilenameUtils.getBaseName(listOfFiles1[j].toString());
							            				String idmlSuffix = tempFile.substring(tempFile.lastIndexOf('_'),tempFile.lastIndexOf('_')+3);
							            				//String templateName = tempFile.substring(0,tempFile.lastIndexOf('_'));
							            				
							            				//System.out.println("templateName:"+templateName);
							            				
							            				if(mnsSuffix.equals(idmlSuffix))
							            				{
							            					tempFileStatus = "true";
							            				}
							        				}
							            		}
							            	}
							            	else
							            	{
							            		String mnsSuffix = chName.substring(chName.lastIndexOf('_'));
							            		String tempFile = templatePath + templateName+mnsSuffix+".idml";
							            		if(new File(tempFile).exists())
							            			tempFileStatus = "true";
							            	}
							            	if(tempFileStatus.equals("true"))
									        {
									    		if(docParams.containsKey(FilenameUtils.getBaseName(listOfFiles[i].getName())) && (values.size() > 0) && (values.get(1).equals("false")))
									    		{
									    			utilities.fileMove(pathString+chName+".docx",jobFolder+chName+".docx");
										        	if(utilities.fileCheck(pathString +chName+".xlsx"))
										        		utilities.fileMove(pathString +chName+".xlsx",jobFolder+chName+".xlsx");
									    		}
									        }
									    	if(docParams.containsKey(FilenameUtils.getBaseName(listOfFiles[i].getName())) && (values.size() == 3))
									    		values.set(1,tempFileStatus);
									    	else
									    		values.add(tempFileStatus);
							            }
							            
									    if(exportMap.equals("true") && tempFileStatus.equals("true") && (inddImportMap != null) && (!inddImportMap.isEmpty()))
									    {
									    	if(inddImportMap.equals("true"))
									    	{
									    		if(docParams.containsKey(FilenameUtils.getBaseName(listOfFiles[i].getName())) && (values.size() > 0) && (values.get(2).equals("false")))
									    		{
										    		utilities.fileMove(pathString+chName+".docx",jobFolder+chName+".docx");
										    		if(utilities.fileCheck(pathString +chName+".xlsx"))
										    			utilities.fileMove(pathString +chName+".xlsx",jobFolder+chName+".xlsx");
									    		}
									    	}
									    	if(docParams.containsKey(FilenameUtils.getBaseName(listOfFiles[i].getName())) && (values.size() == 3))
									    		values.set(2,inddImportMap);
									    	else
									    		values.add(inddImportMap);
									    	//values.set(1,exportMap);
											docParams.put(FilenameUtils.getBaseName(listOfFiles[i].getName()), values);
									    }
							            docParams.put(FilenameUtils.getBaseName(listOfFiles[i].getName()), values);
								    }
								}
							}
							TimeUnit.SECONDS.sleep(1);
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