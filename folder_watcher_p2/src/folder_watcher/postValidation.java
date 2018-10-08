package folder_watcher;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.Date;
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
	private Map <String,String>jobParams;
	postValidation(Map <String,String>jobParams, AtomicInteger postValCounter)
	{
		this.jobParams = jobParams;
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
				if(jobParams.size() > 0)
				{
					boolean templateStatus = jobParams.get("templateName").equals("");
					Set< Map.Entry< String,String> > st = jobParams.entrySet();
					for (Map.Entry< String,String> me:st)
					{
						//me.getKey();
						//System.out.println(me.getKey()+"\n");
						
						if((me.getKey().equals("Template") && templateStatus) || me.getKey().equals("Composition") || me.getKey().equals("Map_path") || me.getKey().equals("Graphics"))
						{
							//System.out.println(me.getKey() + " : " + me.getValue());
							List<String> folderList = new ArrayList<>();
							String folderThisLoop = me.getValue();
							
							if(!new File(folderThisLoop).exists())
							{
								DateFormat dateFormat2 = new SimpleDateFormat("dd-MMM-yy hh:mm:ss aa");
								String dateString2 = dateFormat2.format(new Date()).toString();
								System.out.println( me.getKey()+" deleted / renamed at "+dateString2 + ".\n\n");
								consoleLog.log( me.getKey()+" folder deleted / renamed at "+dateString2 + ".\n\n");
							}
							
							while(!new File(folderThisLoop).exists())
		            		{
								String folderName = (new File(folderThisLoop)).getName();
		            			folderList.add(folderName);
		            			//System.out.println("parent:"+folderName);
		            			folderThisLoop = (new File(folderThisLoop).getParentFile()).getPath();
		            			//folderList.add(folderName);
		            		}
		            		
		            		for(int i=folderList.size()-1; i != -1; i--)
		            		{
		            			folderThisLoop = folderThisLoop+"/"+folderList.get(i);
		            			File theDir1 = new File(folderThisLoop);
		            			//System.out.println("f1:"+theDir.getPath());
		            			while (!theDir1.exists()) 
		            			{
		                    		theDir1.mkdir();
		            			}
		                    }
						}
					}
				}
				
				while(postValCounter.get() != 0)
		        {
					TimeUnit.SECONDS.sleep(1);
		        }

				postValCounter.incrementAndGet();
				File folder = new File(pathString);
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
							//String REGEX = jobId+"_PT\\d\\d|"+jobId+"_ST\\d\\d|"+jobId+"_FM\\d\\d|"+jobId+"_BM\\d\\d|"+jobId+"_RM\\d\\d|"+jobId+"_CH\\d\\d|"+jobId+"_INTRO|"+jobId+"_CON|"+jobId+"_APP\\d\\d|";
							
							//regex matching
							//Pattern p = Pattern.compile(REGEX);
							//Matcher m = p.matcher(FilenameUtils.getBaseName(listOfFiles[i].getName()));   // get a matcher object
			    			
							//System.out.println("mnName : "+listOfFiles[i].getName());
							//System.out.println("extn : "+FilenameUtils.getExtension(listOfFiles[i].getName()));
							
							if(fileNameRegex(jobId, listOfFiles[i]))
							{
								if((FilenameUtils.getExtension(listOfFiles[i].getName()).equals("docx")))
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
							        		
							            if((exportMap != null) && (!exportMap.isEmpty()) && (inddImportMap != null) && (!inddImportMap.isEmpty()))
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
								        
								            if(exportMap.equals("true"))
								            {
								            	Pattern pattern = Pattern.compile("^"+jobId);
							            		Matcher matcher = pattern.matcher(chName);
							            		String suffix="";
							            		
							            		if(matcher.find())
							            		{
							            			//System.out.println("Found match at: "  + matcher.start() + " to " + matcher.end());
							            			suffix = chName.substring(matcher.end(),chName.length());
							            			if(clientId.equals("TF_HSS"))
							            			{
								            			switch(suffix)
								            			{
									            			case "_BM":
															case "_BM_GLO":
															case "_BM_ACK":
															case "_BM_REF":
															case "_BM_BIB":
															case "_BM_IDX":
															case "_BM_SIDX":
															case "_BM_AIDX":
															case "_BM_NOTE":
															case "_BM_CI":
															case "_BM_AFWD":
															case "_BM_ATA":
															case "_BM_NOC":
															case "_BM_SAMPLE":
															{
										    					suffix = "_BM";
										    				}
										    				break;
										    					
															case "_FM":
															case "_FM_LOC":
															case "_FM_ATA":
															case "_FM_NOC":
															case "_FM_SERS":
															case "_FM_DED":
															case "_FM_ACK":
															//case "_FM_REF":
															case "_FM_INTRO":
															case "_FM_LOF":
															case "_FM_LOT":
															case "_FM_CPY":
															case "_FM_PREF":
															case "_FM_FRWD":
															case "_FM_TOC":
															case "_FM_SAMPLE":
										    					suffix = "_FM";
										    				break;
										    				
															case "_INTRO":
															case "_SAMPLE":
															case "_CON":
																suffix = "_CH";
										    				break;
																
										    				default:
										    				{
										    					if(suffix.matches("^_CH\\d\\d"))
										    						suffix = "_CH";
										    					else if(suffix.matches("^_PT\\d\\d|^_ST\\d\\d"))
										    						suffix = "_PT";
										    					else if(suffix.matches("|^_BM_APP\\d\\d"))
										    						suffix = "_BM";
										    				}
										    				break;
								            			}
							            			}
							            		}
								            	
								            	//System.out.println("export map:"+exportMap);
								            	if(templateName.equals(""))
								            	{
								            		File tempDir = new File(templatePath);
								            		
								            		//System.out.println("Template path(Error) : "+templatePath);
								            		
								            		File[] listOfFiles1 = tempDir.listFiles();
								            		for(int j=0; j < listOfFiles1.length; j++)
								            		{
								            			if(listOfFiles1[j].isFile() && (FilenameUtils.getExtension(listOfFiles1[j].toString()).equals("idml")))
								            			{
								            				//String mnsSuffix = chName.substring(chName.lastIndexOf('_'),chName.lastIndexOf('_')+3);
								            				String tempFile = FilenameUtils.getBaseName(listOfFiles1[j].toString());
								            				String idmlSuffix = tempFile.substring(tempFile.lastIndexOf('_'),tempFile.lastIndexOf('_')+3);
								            				//String templateName = tempFile.substring(0,tempFile.lastIndexOf('_'));
								            				
								            				//System.out.println("templateName:"+templateName);
								            				
								            				if(suffix.equals(idmlSuffix))
								            				{
								            					tempFileStatus = "true";
								            					break;
								            				}
								        				}
								            		}
								            	}
								            	else
								            	{
								            		String tempFile = templatePath + templateName+suffix+".idml";
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
							            
										    if(exportMap.equals("true") && tempFileStatus.equals("true"))
										    {
										    	//System.out.println("tempFileStatus:"+tempFileStatus);
										    	if(inddImportMap.equals("true"))
										    	{
										    		//System.out.println("inddImportMap:"+inddImportMap);
										    		if(docParams.containsKey(FilenameUtils.getBaseName(listOfFiles[i].getName())) && (values.size() > 0))
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
								            //System.out.println();
									    }
								    }
								}
								else
								{
									if(!(FilenameUtils.getExtension(listOfFiles[i].getName()).equals("xlsx")))
									{
										//System.out.println("Invalid extension ("+FilenameUtils.getExtension(listOfFiles[i].getName())+").");
										//consoleLog.log("Invalid extension ("+FilenameUtils.getExtension(listOfFiles[i].getName())+").");
									}
								}
							}
							else
							{
								File theDir1 = new File(jobFolder+"INVALID_FILES/");
								if(!theDir1.exists())
								{
									theDir1.mkdir();
								}
								utilities.fileMove(pathString+listOfFiles[i].getName(),jobFolder+"INVALID_FILES/"+listOfFiles[i].getName());
							}
						}
						TimeUnit.SECONDS.sleep(1);
					}
		        }
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
	
	private boolean fileNameRegex(String jobId, File file)
	{
		boolean status = false;
		String fileName = FilenameUtils.getBaseName(file.toString()),suffix="";
		Pattern pattern = Pattern.compile("^"+jobId);
		Matcher matcher = pattern.matcher(fileName);
		
		if(matcher.find())
		{
			//System.out.println("Found match at: "  + matcher.start() + " to " + matcher.end());
			suffix = fileName.substring(matcher.end(),fileName.length());
			//System.out.println("suffix : "+suffix);

			if(clientId.equals("TF_HSS"))
			{
				switch(suffix)
				{
					case "_BM":
					case "_BM_GLO":
					case "_BM_ACK":
					case "_BM_REF":
					case "_BM_BIB":
					case "_BM_IDX":
					case "_BM_SIDX":
					case "_BM_AIDX":
					case "_BM_NOTE":
					case "_BM_CI":
					case "_BM_AFWD":
					case "_BM_ATA":
					case "_BM_NOC":
					case "_BM_SAMPLE":
					case "_FM":
					case "_FM_LOC":
					case "_FM_ATA":
					case "_FM_NOC":
					case "_FM_SERS":
					case "_FM_DED":
					case "_FM_ACK":
					//case "_FM_REF":
					case "_FM_INTRO":
					case "_FM_LOF":
					case "_FM_LOT":
					case "_FM_CPY":
					case "_FM_PREF":
					case "_FM_FRWD":
					case "_FM_TOC":
					case "_FM_SAMPLE":
					case "_INTRO":
					case "_CON":
					case "_SAMPLE":
						status = true;
						break;
						
					default:
					{
						if(suffix.matches("^_CH\\d\\d|^_BM_APP\\d\\d|^_PT\\d\\d|^_ST\\d\\d"))
							status = true;
						break;
					}
				}
			}
		}
		return status;
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