package folder_watcher;

/*
 * Copyright (c) 2008, 2010, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
//https://docs.oracle.com/javase/tutorial/essential/io/notification.html
import java.nio.file.*;
import static java.nio.file.StandardWatchEventKinds.*;
//import static java.nio.file.LinkOption.*;
//import java.nio.file.attribute.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

//import javax.script.ScriptEngine;
//import javax.script.ScriptEngineManager;

//import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.*;

import folder_watcher.mail;
import folder_watcher.url_request;

/*
 * try
	mount volume "smb://rajarajan:Amma2809appa_@172.16.1.2/OEO/"
end try

	try
	
	mount volume "smb://172.16.1.2/Copyediting/" as user name "maestroqs@cmpl.in" with password "M@est0123"
	
end try

 * 
 */
/**
 * Example to watch a directory (or tree) for changes to files.
 */
public class watchDir implements Runnable
{
    private final WatchService watcher;
    private final Map<WatchKey,Path> keys;
    private final Map<String,String> jobParam;
    private boolean trace = false;
    private final AtomicInteger counter;
    private int processedFiles = 0;
    private String pathString, noOfManuScripts, jobId, clientId, templatePath, styleSheetPath, importMapPath, templateName;
    private ArrayList<String> manuScripts;
    mail mailObj;
    job j1;
    boolean suspended = false;
    
    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) 
    {
        return (WatchEvent<T>)event;
    }

    private void register(Path dir) throws IOException 
    {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        if (trace) 
        {
            Path prev = keys.get(key);
            if (prev == null)
            {
                System.out.format("register: %s\n", dir);
            }
            else 
            {
                if (!dir.equals(prev)) 
                {
                    System.out.format("update: %s -> %s\n", prev, dir);
                }
            }
        }
        keys.put(key, dir);
    }

    watchDir(Map <String,String> jobParam,AtomicInteger counter) throws IOException, ParseException, NumberFormatException 
    {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<WatchKey,Path>();
        this.jobParam = jobParam;
        pathString = jobParam.get("Copyediting");
        clientId = jobParam.get("clientId");
        jobId = jobParam.get("jobId");
        noOfManuScripts = jobParam.get("manuscripts");
        templatePath = jobParam.get("Template");
        styleSheetPath = jobParam.get("Standard_stylesheet");
        importMapPath = jobParam.get("Map_path");
        this.counter = counter;
        Path listPath;
        manuScripts = new ArrayList<String>();
        listPath = Paths.get(pathString);
        register(listPath);
        this.trace = true;
    }

    private void initProcessing() throws FileNotFoundException, IOException, ParseException, InterruptedException
    {
    	File folder = new File(pathString);
        File[] listOfFiles = folder.listFiles();
        
        System.out.println("Processing already present files in Job folder...\n");
		consoleLog.log("Processing already present files in Job folder...\n");
		if(folder.exists())
		{
			for (int i = 0; i < listOfFiles.length; i++) 
			{			
				if (listOfFiles[i].isFile()) 
				{
					System.out.println("Extension:"+utilities.getFileExtension(listOfFiles[i]));
					System.out.println("File/Folder Status:"+listOfFiles[i].isFile());
					
					String REGEX = jobId+"_CH\\d\\d|"+jobId+"_FM\\d\\d|"+jobId+"_BM\\d\\d|"+jobId+"_RM\\d\\d|"+jobId+"_PT\\d\\d";
					
					//regex matching
					Pattern p = Pattern.compile(REGEX);
					Matcher m = p.matcher(utilities.getFileNameWithoutExtension(listOfFiles[i]));   // get a matcher object
	    				
					if(m.matches() && (listOfFiles[i].getName().lastIndexOf(".docx") > 0))
					{
						//System.out.println("File " + listOfFiles[i].getName());
						
						if(!manuScripts.contains(listOfFiles[i].getName()))
						{
							try 
			        		{
								while(!isCompletelyWritten(listOfFiles[i]))
								{}
							}
			        		catch (InterruptedException e) 
			        		{}
							
							functionalityCheck(listOfFiles[i].getName());
						}
					}
					else if(m.matches() && (listOfFiles[i].getName().lastIndexOf(".xlsx") > 0))
					{
						System.out.println("xlsx file created at "+listOfFiles[i].getName());
	    				consoleLog.log("xlsx file created at "+listOfFiles[i].getName()+"\n");
	    				
	    				if(!utilities.fileCheck(pathString +listOfFiles[i].getName()))
	    				{
	    					System.out.println("Manuscript not found, hence moved to \"ERROR\" folder");
	        				consoleLog.log("Manuscript not found, hence moved to \"ERROR\" folder");
	        				
	        				utilities.fileMove(pathString+listOfFiles[i].getName(),pathString+"ERROR/"+listOfFiles[i].getName());
	    				}
					}
					else if(!listOfFiles[i].getName().equals(".DS_Store"))
					{
						//invalid other format files
			        	File theDir = new File(pathString+"INVALID_FILES/");
			        	if (!theDir.exists()) 
						{
			        		theDir.mkdir();
						}
			        	utilities.fileMove(pathString+listOfFiles[i].getName(),pathString+"INVALID_FILES/"+listOfFiles[i].getName());
						
			        	mailObj = new mail("Pre-editing", "INVALID", jobId, "", listOfFiles[i].getName());
						Thread mailThread6 = new Thread(mailObj, "Mail Thread for Pre editing Team");
			        	mailThread6.start();
			        	
						//xlsx and docx files does not match regex
						System.out.println("Waiting for more files1...");
						consoleLog.log("Waiting for more files1...");
					}
				}
				else if (listOfFiles[i].isDirectory()) 
				{
					//Move manuscripts to error folder
					if(!(listOfFiles[i].getName().compareToIgnoreCase("ERROR") == 0) && !(listOfFiles[i].getName().compareToIgnoreCase("INVALID_FILES") == 0) && !(listOfFiles[i].getName().compareToIgnoreCase("Equations") == 0))
		        	{
						File theDir = new File(pathString+"INVALID_FILES/");
		        	
			        	if (!theDir.exists()) 
						{
			        		theDir.mkdir();
						}
			        	utilities.recurMove(new File(pathString+listOfFiles[i].getName()), new File (pathString+"INVALID_FILES/"+listOfFiles[i].getName()));
						
			        	mailObj = new mail("Pre-editing", "INVALID", jobId, "", listOfFiles[i].getName());
						Thread mailThread6 = new Thread(mailObj, "Mail Thread for Pre editing Team");
			        	mailThread6.start();
			        	
			        	System.out.println("Waiting for more files2...");
						consoleLog.log("Waiting for more files2...");
		        	}
					//xlsx and docx files does not match regex
				}
			}
		}
    }
    
    /**
     * Process all events for keys queued to the watcher
     * @throws IOException 
     * @throws ParseException 
     */
    int processEvents() throws IOException, ParseException, InterruptedException, NumberFormatException
    {
    	boolean initFlag = false;
    	
		for (;;) 
        {
        	if(initFlag == false)
        	{
        		try 
        		{
					initProcessing();
				}
        		catch (InterruptedException e) 
        		{
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
        		initFlag = true;
        		
        		//template path or stylesheet path or map path is missing
        		System.out.println("jobId:"+jobId);
        		System.out.println("clientId:"+clientId);
        		/*if(jobFailErrorFun())
				{
					return 1;
				}*/
        		
        		if(processedFiles == Integer.parseInt(noOfManuScripts))
	            {
					//if other docx files present
	            	if(manuScripts.size() == Integer.parseInt(noOfManuScripts))
	            	{
	            		System.out.println("Job finished.\n");
    					consoleLog.log("Job finished.\n");
	            		//System.out.println("Job terminated due to TemplatePath or mapPath or styleSheetPath location missing or incorrect.");
	            		//consoleLog.log("Job terminated due to TemplatePath or mapPath or styleSheetPath location missing or incorrect.");
	            		return 2;
	            	}
	            }
        		
        		//this prints when job started watching folder
        		//System.out.println("Watch Folder initiated......\n");
				//consoleLog.log("Watch Folder initiated......\n");
        	}
        	//pre change check
        	if(processedFiles == Integer.parseInt(noOfManuScripts))
            {
        		return 3;
        		//if(getExtension(pathString,".docx").length == Integer.parseInt(noOfManuScripts))
        			//break;
            }
            // wait for key to be signaled
            WatchKey key;
            try 
            {
                key = watcher.take();
            }
            catch (InterruptedException x) 
            {
                return 4;
            }

            Path dir = keys.get(key);
            if (dir == null) 
            {
                System.err.println("WatchKey not recognized!!");
                continue;
            }
            
            for (WatchEvent<?> event: key.pollEvents()) 
            {
            	//System.out.println("new element:"+listA.size());
            	//if(listA.size() == 3)
            		//System.out.println("new element:"+listA.get(2));
            	
                @SuppressWarnings("rawtypes")
				WatchEvent.Kind kind = event.kind();

                // TBD - provide example of how OVERFLOW event is handled
                if (kind == OVERFLOW) 
                {
                	continue;
                }

                // Context for directory entry event is the file name of entry
                WatchEvent<Path> ev = cast(event);
                Path name = ev.context();
                Path child = dir.resolve(name);
                
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) 
                {
                    /*for (Path file: stream) 
                    {
                        System.out.println(file.getFileName());
                    }*/
                } catch (IOException | DirectoryIteratorException x) {
                    // IOException can never be thrown by the iteration.
                    // In this snippet, it can only be thrown by newDirectoryStream.
                    System.err.println(x);
                }
                
                // print out event
                if((kind == ENTRY_CREATE) || (kind == ENTRY_DELETE) || (kind == ENTRY_MODIFY))
                {
                	//int i = child.getFileName().toString().lastIndexOf('.');
                	//String extension="";
                	//if (i > 0) 
                	//{
                		//extension = child.getFileName().toString().substring(i+1);
                	//}
                	
                	//System.out.println(child.getFileName().toString());
                		
            		if(kind == ENTRY_CREATE)
            		{
            			File createdFile = child.toFile();
            			//System.out.println(pathString + "/" + child.getFileName().toString());
//            			System.out.println("Extension:"+utilities.getFileExtension(createdFile));
//            			System.out.println("File/Folder Status:"+createdFile.isFile());
            			
            			if(createdFile.isFile())
            			{
            				String fileNameWoExtn = utilities.getFileNameWithoutExtension(createdFile);
                			//System.out.println(child.getFileName().toString()+" is a file.");
                			//System.out.println("Extension: "+extension);
            				String REGEX = jobId+"_CH\\d\\d|"+jobId+"_FM\\d\\d|"+jobId+"_BM\\d\\d|"+jobId+"_RM\\d\\d|"+jobId+"_PT\\d\\d";
            				
            				//regex matching
            				Pattern p = Pattern.compile(REGEX);
            				Matcher m = p.matcher(fileNameWoExtn);   // get a matcher object
            				
            				System.out.println("Manuscript name match:"+m.matches());
            				consoleLog.log("Manuscript name match:"+m.matches()+"\n");
            				
            				try 
			        		{
								while(!isCompletelyWritten(createdFile))
								{}
							}
			        		catch (InterruptedException e) 
			        		{
								// TODO Auto-generated catch block
								//e.printStackTrace();
							}
            				
	            			if(m.matches() && (utilities.getFileExtension(createdFile).equals("docx") || utilities.getFileExtension(createdFile).equals("xlsx")))
	                    	{
	            				System.out.println("chap_name:"+fileNameWoExtn);
	            				consoleLog.log("chap_name:"+fileNameWoExtn);
	            				
	            				if(m.matches() && utilities.getFileExtension(createdFile).equals("docx"))
	            				{
		            				//System.out.print("Docx file created at ");
		            				//System.out.format("%s\n", child);
		            				//consoleLog.log("Docx file created at "+child+"\n");
		            				
		            				//process the manuscript
		            				if(!manuScripts.contains(child.getFileName().toString()))
		        					{
		        						//manuScripts.add(child.getFileName().toString());
		        						functionalityCheck(child.getFileName().toString());
		        					}
		            				else
		            				{
		            					System.out.println("Duplicate Manuscripts file present in Job Folder.\n");
		            					consoleLog.log("Duplicate Manuscripts file present in Job Folder.\n");
		            				}
		            				//System.out.println("jobFailError : "+jobFailErrorFun());
		            				
		            				//template path or stylesheet path or map path is missing
		            				if(processedFiles == Integer.parseInt(noOfManuScripts))
		            	            {
		            					//if other docx files present
		            	            	if(manuScripts.size() == Integer.parseInt(noOfManuScripts))
		            	            	{
		            	            		System.out.println("Job finished.\n");
			            					consoleLog.log("Job finished.\n");
		            	            		//System.out.println("Job terminated due to TemplatePath or mapPath or styleSheetPath location missing or incorrect.");
		            	            		//consoleLog.log("Job terminated due to TemplatePath or mapPath or styleSheetPath location missing or incorrect.");
		            	            		return 6;
		            	            	}
		            	            }
		            			}
	            				else if(m.matches() && utilities.getFileExtension(createdFile).equals("xlsx"))
	            				{
	            					System.out.print("xlsx file created at ");
		            				System.out.format("%s\n", child);
		            				consoleLog.log("xlsx file created at "+child+"\n");
		            				
		            				if(!utilities.fileCheck(pathString +child.getFileName().toString()))
		            				{
		            					System.out.println("Manuscript not found, hence moved to \"ERROR\" folder");
		                				consoleLog.log("Manuscript not found, hence moved to \"ERROR\" folder");
		                				
		                				utilities.fileMove(pathString+child.getFileName().toString(),pathString+"ERROR/"+child.getFileName().toString());
		            				}
	            				}
	                    	}
	            			else if(!createdFile.getName().equals(".DS_Store"))
            				{
	            				System.out.println(child.getFileName().toString()+" is a Invalid file.");
            					//Move manuscripts to error folder
            		        	File theDir = new File(pathString+"INVALID_FILES/");
            		        	if (!theDir.exists()) 
            					{
            		        		theDir.mkdir();
            					}
            		        	utilities.fileMove(pathString+child.getFileName().toString(),pathString+"/INVALID_FILES/"+child.getFileName().toString());
            		        	
            		        	mailObj = new mail("Pre-editing", "INVALID", jobId, "", child.getFileName().toString());
            					Thread mailThread6 = new Thread(mailObj, "Mail Thread for Pre editing Team");
            		        	mailThread6.start();
            		        	
            		        	//xlsx and docx files does not match regex
            					System.out.println("Waiting for more files3...");
            					consoleLog.log("Waiting for more files3...");
            				}
            			}
            			else if(new File(pathString + "/" + child.getFileName().toString()).isDirectory())
            			{
            				if(!(child.getFileName().toString().compareToIgnoreCase("ERROR") == 0 )  && !(child.getFileName().toString().compareToIgnoreCase("INVALID_FILES") == 0) && !(child.getFileName().toString().compareToIgnoreCase("Equations") == 0))
            	        	{
	            				//Move manuscripts to error folder
	        		        	File theDir = new File(pathString+"/INVALID_FILES/");
	        		        	if (!theDir.exists()) 
	        					{
	        		        		theDir.mkdir();
	        					}
	        		        	
	        		        	utilities.recurMove(new File(pathString+"/"+child.getFileName().toString()), new File(pathString+"/INVALID_FILES/"+child.getFileName().toString()));
	        		        	
	        		        	mailObj = new mail("Pre-editing", "INVALID", jobId, "", child.getFileName().toString());
	        					Thread mailThread6 = new Thread(mailObj, "Mail Thread for Pre editing Team");
	        		        	mailThread6.start();
	        		        	
	        		        	//xlsx and docx files does not match regex
	        					System.out.println("Waiting for more files4...");
	        					consoleLog.log("Waiting for more files4...");
            	        	}
            			}
            		}
            		else if(kind == ENTRY_DELETE)
            		{
            			//System.out.print("File deleted at ");
            			//System.out.format("%s\n", child);
            		}
            		else if(kind == ENTRY_MODIFY)
            		{
            			//System.out.print("File modified at ");
            			//System.out.format("%s\n", child);
            		}
            		//System.out.format("Event:%s\nactivity in	:%s\n", event.kind().name(), child);
                	//String folder = child.toString().substring(0,child.toString().lastIndexOf('/')+1);
                	//System.out.println("folder:"+folder);
                	//System.out.println("folder:"+child.getParent().toString());
                	//System.out.println("file:"+child.getFileName().toString());
                	//System.out.println("file extension:"+extension);
                }
                // if directory is created, and watching recursively, then
                // register it and its sub-directories
//                if (recursive && (kind == ENTRY_CREATE)) 
//                {
//                    try 
//                    {
//                        if (Files.isDirectory(child, NOFOLLOW_LINKS))
//                        {
//                            registerAll(child);
//                        }
//                    } 
//                    catch (IOException x) 
//                    {
//                        // ignore to keep sample readbale
//                    }
//                }
            }
            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) 
            {
                keys.remove(key);

                // all directories are inaccessible
                if (keys.isEmpty()) 
                {
                    break;
                }
            }
            
            //System.out.println("processedFiles:"+processedFiles);
            //System.out.println("manuscripts:"+noOfManuScripts);
            //post change check 
//            //if(processedFiles == Integer.parseInt(noOfManuScripts))
//            {
//            	//return;
//            	if(getExtension(pathString,".docx").length == Integer.parseInt(noOfManuScripts))
//            		continue;
//            }
        }
        return 7;
    }

	@SuppressWarnings("unchecked")
	@Override
	public void run()
	{
		//boolean procStatus = false;
    	try 
    	{
//    		if(pathString.indexOf("ERROR") != -1)
//    		{
//    			//TimeUnit.SECONDS.sleep(2);
//    			System.out.println("pathString:"+pathString);
//    			//postValidation(pathString);
//    		}
//    		else
//    		{
    			int processStatus;
    			//watch thread job
	    	    //this.jobId = jobId;
	            //this.noOfManuScripts = noOfManuScripts;
	            //this.counter = counter;
	           // Path listPath;
    			//int processStatus;
    			
    			File theDir = new File(pathString+"ERROR/");
    			
    			System.out.println("ERROR folder : "+theDir.toString());
    			
	        	while (!theDir.exists()) 
				{
	        		theDir.mkdir();
				}
				
        		postValidation postVal = new postValidation(jobParam,counter);
				Thread postValThread = new Thread(postVal, "Watch Thread for \"ERROR\" folder.");
				postValThread.start();
    			
    			job_continue:
    				//infinite loop to connect to disk
    			while(true)
    			{
    					processStatus = processEvents();
	    				System.out.println("processStatus:" + processStatus);
						consoleLog.log("processStatus:" + processStatus);
						//mount error
	    				if(processStatus == 7)
	    				{
	    					suspend();
	    					//TimeUnit.SECONDS.sleep(5);
	    		            //synchronized(this) 
	    		            //{
	    					System.out.println("Waiting to connect.....\n");
	    					consoleLog.log("Waiting to connect.....\n");
							while(suspended) 
							{
								//wait();
								if(!Main.mountError)
								{
									resume();
								}
								TimeUnit.SECONDS.sleep(1);
							}
							System.out.println("pathString status:"+!new File(pathString).exists());
							while(!new File(pathString).exists())
							{
								//System.out.println("Waiting to connect.....");
								TimeUnit.SECONDS.sleep(5);
							}
							register(Paths.get(pathString));
							System.out.println("Register:"+pathString);
							continue job_continue;
	    		            //}
			    			//System.out.println("Process status:"+processStatus);
			    			//consoleLog.log("Process status:"+processStatus);
			    			/*String osResp1 = utilities.mountDisk("172.16.1.2", "Copyediting", "maestroqs@cmpl.in", "M@est0123");
			    			String osResp2 = utilities.mountDisk("172.16.1.21", "comp_template", "maestroqs@cmpl.in", "M@est0123");
			    			String osResp3 = utilities.mountDisk("172.16.1.21", "COMP", "maestroqs@cmpl.in", "M@est0123");
			    			
			    			//String osResp4 = utilities.mountDisk("172.16.1.21", "COMP", "maestroqs@cmpl.in", "M@est0123");
			    			//String osResp = utilities.serverMount();
			    			System.out.println("(job restart validation)osResp1:" + osResp1);
			    			consoleLog.log("(job restart validation)Mount response1:" + osResp1);
			    			
			    			System.out.println("(job restart validation)osResp2:" + osResp2);
			    			consoleLog.log("(job restart validation)Mount response2:" + osResp2);
			    			
			    			System.out.println("(job restart validation)osResp3:" + osResp3);
			    			consoleLog.log("(job restart validation)Mount response3:" + osResp3);
			    			
			    			if ((osResp1.equals("Disk Found")) && (osResp2.equals("Disk Found")) && (osResp3.equals("Disk Found"))) 
			    			{*/
//								register(Paths.get(pathString));
//								System.out.println("Register:"+pathString);
//								continue job_continue;
							/*}
							else
							{
								// mail to netops
								consoleLog.log("MOUNT ERROR mail sent to group \"netops\"");
								// smaple : sendMail("Net-ops","rajarajan@codemantra.in", "", "MOUNT", "", "");
								//mail m = new mail("Net-ops", "ERROR", "MOUNT", "", "");
								// m.mailProcess("Net-ops", "ERROR", "MOUNT", "", "");
								//Thread mailThread = new Thread(m, "Mail Thread for Template path mount");
								//mailThread.start();
							}*/
	    				}
	    				//successful job finish
	    				else
	    					break;
    				
    				
    				
    			}
        		
    			JSONObject obj=new JSONObject();
	    		obj.put("jobId",jobId);
	    		obj.put("clientId",clientId);
	    		
	    		//System.out.println("processError for "+jobId+" :"+processError);
	         	
	         	DateFormat dateFormat2 = new SimpleDateFormat("dd-MMM-yy hh:mm:ss aa");
	        	String dateString2 = dateFormat2.format(new Date()).toString();
	         	
	         	if((processStatus == 7) && (manuScripts.size() == Integer.parseInt(noOfManuScripts)) && (processedFiles == Integer.parseInt(noOfManuScripts)))
	    		{
	         		obj.put("status","COMPLETED");
	    			//JOB successfully finished
	    			//sample : sendMail("Template", "JOB_SUCCESS", "9781138556850_Ilyas", "", noOfManuScripts);
	         		mailObj = new mail("Template","JOB_SUCCESS",jobId,"", noOfManuScripts);
	    			//mailObj.mailProcess("Template","JOB_SUCCESS","jobId","", "");
	    			Thread mailThread = new Thread(mailObj, "Mail Thread for Job success");
					mailThread.start();
	    			
	    			System.out.println("Job finished with SUCCESS at "+dateString2+" for jobId:"+jobId+", clientId:"+clientId+" and folder location:"+pathString);
	             	consoleLog.log("Job finished with success at "+dateString2+" for jobId:\""+jobId+"\", clientId:\""+clientId+"\" and folder location:\""+pathString+"\"");
	             	
//	             	deleting "ERROR" & "INVALID" folder
//	             	utilities.delete(new File(pathString+"/ERROR/"));
//	             	utilities.delete(new File(pathString+"/INVALID/"));
	    		}
	    		/*else
	    		{
	    			//Job failed due to Template path or stylesheet path or map path
	    			obj.put("status","FAILED");
	    			System.out.println("Job finished WITH ERROR at "+dateString2+" for jobId:"+jobId+", clientId:"+clientId+" and folder location:"+pathString);
	             	consoleLog.log("Job finished WITH ERROR at \""+dateString2+"\" for jobId:\""+jobId+"\", clientId:\""+clientId+"\" and folder location:\""+pathString+"\"");
	             	//String jsonText = JSONValue.toJSONString(obj);  
	        		
	//             	consoleLog.log("URL : \"http://"+url_request.serverIp+"/maestro/updateJobStatus\", type : \"PUT\" and content : \""+jsonText+"\"");
	//        		String webResponse = url_request.urlRequestProcess("http://"+url_request.serverIp+"/maestro/updateJobStatus","PUT",jsonText);
	             	
	//        		System.out.println("URL response : "+webResponse);
	//             	consoleLog.log("URL response : "+webResponse);
	    		}*/
	         	
	         	String jsonText = JSONValue.toJSONString(obj);
	    		//System.out.println(jsonText);
	         	//jobStatus update
	         	System.out.println("Job status URL:\"http://"+url_request.serverIp+"/maestro/updateJobStatus\", type:\"PUT\" and content:\""+jsonText+"\"");
	         	consoleLog.log("Job status URL:\"http://"+url_request.serverIp+"/maestro/updateJobStatus\", type:\"PUT\" and content:\""+jsonText+"\"");   		
	    		String jobResponse = url_request.urlRequestProcess("http://"+url_request.serverIp+"/maestro/updateJobStatus","PUT",jsonText);
	    		
	    		System.out.println("Job status response:"+jobResponse);
	    		consoleLog.log("Job status response:"+jobResponse);
	    		
	         	job j1 = new job();
	         	j1.job_update(jobId);
	         	
	         	if((processStatus == 7) && (manuScripts.size() == Integer.parseInt(noOfManuScripts)) && (processedFiles == Integer.parseInt(noOfManuScripts)))
	    		{
		         	//deleting "ERROR" folder
	             	//utilities.delete(new File(pathString+"/ERROR/"));
	             	//utilities.delete(new File(pathString+"/INVALID_FILES/"));
	    		}
			//}
    	}
    	catch (Exception e) 
    	{
			e.printStackTrace();
//			try 
//			{
//				//consoleLog.log(e.toString());
//			} 
//			catch (IOException e1) 
//			{
//				//e1.printStackTrace();
//			}
		}
    	return;
	}
	
	private boolean functionalityCheck(String file) throws IOException,FileNotFoundException, ParseException, InterruptedException
	{
		boolean styleSheetfileStatus = false, contentModellingStatus = false, jsxProcessStatus = false, eqnFolderStatus = false, tempFileStatus = false;// 0 > failed,0 = success
		String chName = file,eqnStatus,preEditStatus,stageCleanUp,docVal,structuringVal,postVal,postConv,inDStyleMap = "",wdExportMap="";
		chName = chName.substring(0,chName.indexOf(".docx"));
		
		//stylesheet for manuscript checking
		if(utilities.fileCheck(pathString +"/"+chName+".xlsx"))
		{
			//style sheet present
			System.out.println("Styles file exist for "+chName);
			consoleLog.log("Styles file exist for "+chName);
			styleSheetfileStatus = true;
		}
		else	
		{
			//style sheet missing
			System.out.println("Styles file does not exist for "+file);
			consoleLog.log("Styles file does not exist for "+file);
			
			//sample : sendMail("Pre-editing", "STYLESHEET", "9781138556850_Ilyas_CH01", "", "");
			mailObj = new mail("Pre-editing", "STYLESHEET", chName, "", "");
			Thread mailThread6 = new Thread(mailObj, "Mail Thread for CRC Team");
        	mailThread6.start();
		}
		
		String urlParams = "jobId="+URLEncoder.encode(jobId,"UTF-8")+"&clientId="+URLEncoder.encode(clientId,"UTF-8")+"&chapter="+URLEncoder.encode(chName,"UTF-8");
		consoleLog.log("URL:\"http://"+url_request.serverIp+"/maestro/getValStageDetails?"+urlParams + "\", type:\"GET\"\n");
		System.out.println("URL:\"http://"+url_request.serverIp+"/maestro/getValStageDetails?"+urlParams + "\", type:\"GET\"\n");
		
		//http://172.16.1.25:8080/maestro/getValStageDetails?jobId=9781482298697_Yu&clientId=TF_HSS&chapter=9781138598928_Willard+Bohn_BM01
    	String preEditResponse = url_request.urlRequestProcess("http://"+url_request.serverIp+"/maestro/getValStageDetails?"+urlParams,"GET","");        		
        
    	consoleLog.log("preEditResponse:"+utilities.json_pretty_print(preEditResponse)+"\n");
        System.out.println("preEditResponse:"+utilities.json_pretty_print(preEditResponse)+"\n");
        
        if((preEditResponse != null) && (!preEditResponse.isEmpty()) && (!preEditResponse.equals("")))
		{
        	JSONParser parser = new JSONParser();
			Object preEditObj = parser.parse(preEditResponse);
	        JSONObject jo = (JSONObject) preEditObj;
		    preEditStatus = (String) jo.get("status");
		    stageCleanUp = (String) jo.get("stageCleanUp");
		    docVal = (String) jo.get("docVal");
		    structuringVal = (String) jo.get("structuringVal");
		    postVal = (String) jo.get("postVal");
		    postConv = (String) jo.get("postConv");
		    inDStyleMap = (String) jo.get("inDStyleMap");
		    wdExportMap = (String) jo.get("wdExportMap");
		    
		    //System.out.println("preEditStatus : "+preEditStatus);
		    System.out.println("stageCleanUp : "+stageCleanUp);
		    System.out.println("docVal : "+docVal);
		    System.out.println("structuringVal : "+structuringVal);
		    System.out.println("postVal : "+postVal);
		    System.out.println("postConv : "+postConv);
		    System.out.println("inDStyleMap : "+inDStyleMap);
		    System.out.println("wdExportMap : "+wdExportMap);
		    System.out.println("preEditStatus:"+preEditStatus+"\n"); //+", processError:"+processError
		    
		    consoleLog.log("stageCleanUp : "+stageCleanUp);
		    consoleLog.log("docVal : "+docVal);
		    consoleLog.log("structuringVal : "+structuringVal); 
		    consoleLog.log("postVal : "+postVal);
		    consoleLog.log("postConv : "+postConv);
		    consoleLog.log("inDStyleMap : "+inDStyleMap);
		    consoleLog.log("wdExportMap : "+wdExportMap);
		    consoleLog.log("preEditStatus:"+preEditStatus+"\n"); //+", processError:"+processError
			
			if((stageCleanUp != null) && (!stageCleanUp.isEmpty()) && (stageCleanUp.equals("true")) && 
				(docVal != null) && (!docVal.isEmpty()) && (docVal.equals("true"))  && 
				(structuringVal != null) && (!structuringVal.isEmpty()) && (structuringVal.equals("true"))  && 
				(postVal != null) && (!postVal.isEmpty()) && (postVal.equals("true"))  && 
				(postConv != null) && (!postConv.isEmpty()) && (postConv.equals("true")) && 
				//(inDStyleMap != null) && (!inDStyleMap.isEmpty()) && (inDStyleMap.equals("true")) && 
				(wdExportMap != null) && (!wdExportMap.isEmpty()) && (wdExportMap.equals("true")))
	        {
				//all parameters of content modelling stage true
				contentModellingStatus = true;
	        }
			else
			{
				consoleLog.log("Failed in ContentModelling stage.\n");
	    		System.out.println("Failed in ContentModelling stage.\n");
	    		
	    		//content modelling error
	    		mailObj = new mail("Pre-editing", "API", chName, "", "");
				Thread mailThread6 = new Thread(mailObj, "Mail Thread for CRC Team");
	        	mailThread6.start();
			}
		}
        
        if(styleSheetfileStatus && contentModellingStatus )
        {
    		//172.16.4.112:8088/maestro/getChapterEquation?jobId=9781138556850_Ilyas&clientId=TF_STEM&chapter=9781138556850_Ilyas_CH11
        	urlParams = "jobId="+URLEncoder.encode(jobId,"UTF-8")+"&clientId="+URLEncoder.encode(clientId,"UTF-8")+"&chapter="+URLEncoder.encode(chName,"UTF-8");
    		consoleLog.log("URL:\"http://"+url_request.serverIp+"/maestro/getChapterEquation?"+urlParams + "\", type:\"GET\"\n");
    		System.out.println("URL:\"http://"+url_request.serverIp+"/maestro/getChapterEquation?"+urlParams + "\", type:\"GET\"\n");
    		
        	String eqnResponse = url_request.urlRequestProcess("http://"+url_request.serverIp+"/maestro/getChapterEquation?"+urlParams,"GET","");        		
            
        	System.out.println("eqnResponse:"+utilities.json_pretty_print(eqnResponse)+"\n");
        	consoleLog.log("eqnResponse:"+utilities.json_pretty_print(eqnResponse)+"\n");
            
            if((eqnResponse != null) && (!eqnResponse.isEmpty()) && (!eqnResponse.equals("")))
    		{
	            JSONParser parser = new JSONParser();
				Object eqnObj = parser.parse(eqnResponse);
		        JSONObject jo = (JSONObject) eqnObj;
			    eqnStatus = (String) jo.get("isEquationExists");
			    
			    System.out.println("eqnStatus:"+eqnStatus);
			    
			    if(eqnStatus.equals("true"))
			    {
			    	if(new File(pathString+"EQUATIONS/"+chName).isDirectory())
			    	{
			    		consoleLog.log("This chapter has \"Equations\" and equations are present in "+pathString+"/EQUATIONS/"+chName+"\n");
			    		System.out.println("This chapter has \"Equations\" and equations are present in "+pathString+"/EQUATIONS/"+chName+"\n");
			    		eqnFolderStatus = true;
			    	}
			    	else
			    	{
			    		consoleLog.log("This chapter has \"Equations\" but equations are not present in "+pathString+"/EQUATIONS/"+chName+"\n");
			    		System.out.println("This chapter has \"Equations\" and equations are not present in "+pathString+"/EQUATIONS/"+chName+"\n");
			    		eqnFolderStatus = false;
			    		
			    		//Equatons missing
			    		mailObj = new mail("Pre-editing", "DIRECTORY", chName, "", "");
						Thread mailThread6 = new Thread(mailObj, "Mail Thread for CRC Team");
			        	mailThread6.start();
			    	}
			    }
			    else if(eqnStatus.equals("false"))
			    {
			    	consoleLog.log("This chapter does not have \"EQUATIONS\"\n");
		    		System.out.println("This chapter does not have \"EQUATIONS\"\n");
		    		eqnFolderStatus = true;
			    }
    		}
        }
        
        //check template exists for the manuscripts
        if(styleSheetfileStatus && contentModellingStatus && eqnFolderStatus)
        {
        	if(jobParam.get("templateName").equals(""))
        	{
        		File tempDir = new File(jobParam.get("Template"));
        		File[] listOfFiles = tempDir.listFiles();
        		for(int i=0; i < listOfFiles.length; i++)
        		{
        			if(listOfFiles[i].isFile() && (FilenameUtils.getExtension(listOfFiles[i].toString()).equals("idml")))
        			{
        				String mnsSuffix = chName.substring(chName.lastIndexOf('_'),chName.lastIndexOf('_')+3);
        				String tempFile = FilenameUtils.getBaseName(listOfFiles[i].toString());
        				String idmlSuffix = tempFile.substring(tempFile.lastIndexOf('_'),tempFile.lastIndexOf('_')+3);
        				templateName = tempFile.substring(0,tempFile.lastIndexOf('_'));
        				
        				System.out.println("templateName:"+templateName);
        				
        				if(mnsSuffix.equals(idmlSuffix))
	        				tempFileStatus = true;
    				}
        		}
        	}
        	else
        	{
        		String mnsSuffix = chName.substring(chName.lastIndexOf('_'),3);
        		String tempFile = jobParam.get("Template") + jobParam.get("templateName")+mnsSuffix+".idml";
        		if(new File(tempFile).exists())
        			tempFileStatus = true;
        	}
        }
        
        //condition for inDesign to initiate or not
        if(styleSheetfileStatus && contentModellingStatus && eqnFolderStatus && tempFileStatus)
        {
	        if(inDStyleMap.equals("true"))
	        {
	        	consoleLog.log("InDesign style map process already finished, StyleMapping function skipped.");
				System.out.println("InDesign style map process already finished, StyleMapping function skipped.");
	        }
	        else//if(styleSheetfileStatus && contentModellingStatus && eqnFolderStatus && !inDStyleMap.equals("true"))
			{
				//stylesheet file found and contentModelling stage passed
				consoleLog.log("Stylesheet file present and PreEditStatus stage passed.\n");
				System.out.println("Stylesheet file present and PreEditStatus stage passed.\n");
				
				
				while(counter.get() != 0)
		        {
		        	TimeUnit.SECONDS.sleep(1);
		        }

		        counter.incrementAndGet();
	        	utilities.delete(new File(System.getProperty ("user.home")+"/Desktop/Maestro_QS/"+chName+"_InDTReport.xls"));
	        	
	        	//template path, mapping path and stylesheet path 
	        	//String jobParams[] = job.getDocParam(jobId, clientId);
	        	if(JavaApplescriptTest(chName,pathString+"/"+chName+".xlsx"))
	        	{
	        		//jsx process passed
	        		jsxProcessStatus = true;
	        	}
	        	else
	        	{
	        		consoleLog.log("Failed in InDesign style mapping stage.\n");
		    		System.out.println("Failed in InDesign style mapping stage.\n");
	        	}
	        	counter.decrementAndGet();
			}
        }
	    
		if(styleSheetfileStatus && contentModellingStatus && eqnFolderStatus && (jsxProcessStatus || inDStyleMap.equals("true")))
		{
			manuScripts.add(file);
			processedFiles++;
		}
		else
		{
			//Move manuscripts to error folder
        	File theDir = new File(pathString+"ERROR/");
        	while (!theDir.exists()) 
			{
        		theDir.mkdir();
			}
        	utilities.fileMove(pathString+"/"+chName+".docx",pathString+"ERROR/"+chName+".docx");
			if(utilities.fileCheck(pathString +"/"+chName+".xlsx"))
				utilities.fileMove(pathString+"/"+chName+".xlsx",pathString+"ERROR/"+chName+".xlsx");
			//processedFiles--;
		}
		
		System.out.println("No of manuscripts : "+noOfManuScripts);
		System.out.println("No of processedFiles : "+processedFiles+"\n");
		
		consoleLog.log("No of manuscripts : "+noOfManuScripts);
		consoleLog.log("No of processedFiles : "+processedFiles+"\n");
		return (styleSheetfileStatus && contentModellingStatus && jsxProcessStatus);
	}
	 
	@SuppressWarnings("unchecked")
	public boolean JavaApplescriptTest(String chName,String stylePath) throws IOException
	 {
		boolean appleScriptStatus = false;
		String result = "";
		
		//System.out.println("jobId:"+jobId);
		//System.out.println("clientId:"+clientId);
		//System.out.println("chName:"+chName);
		System.out.println("templateName : "+templateName);
		System.out.println("templatePath:"+templatePath);
		System.out.println("styleSheetPath:"+styleSheetPath);
		System.out.println("mapPath:"+importMapPath+"\n");
		
		consoleLog.log("templateName : "+templateName);
		consoleLog.log("templatePath : "+templatePath);
		consoleLog.log("styleSheetPath : "+styleSheetPath);
		consoleLog.log("mapPath : "+importMapPath+"\n");
		//ScriptEngineManager manager = new ScriptEngineManager();
		//ScriptEngine engine = manager.getEngineByName("AppleScript");
		//String pathFolder = path.get(i).substring(path.get(i).indexOf("//")+2,path.get(i).length());
		
		try 
		{
			String command = "set aScriptPath to \"Users:"+System.getProperty ("user.name")+":Library:Preferences:Adobe InDesign:Version 10.0:en_US:Scripts:Scripts Panel:Maestro_Styles_Validations_v1.0.jsx\"\n" +
			"set myParameters to {\""+ clientId +"\",\"" + jobId +"\", \"" + chName +".docx\", \"" + stylePath +"\",\"" + templateName +"\",\"" + templatePath +"\", \"" + importMapPath +"\",\"" + styleSheetPath +"\"}\n" +
			"tell application \"Adobe InDesign CC 2014\"\n" +
			"with timeout of 600 seconds\n" +
			"Activate\n" +
			"do script aScriptPath language javascript with arguments myParameters\n"+ 
			"tell script args \n" +
			"set myScriptArgumentA to get value name \"ScriptArgumentB\"\n" +
			"end tell\n" +
			"end timeout\n" +
			"return myScriptArgumentA \n" +
			"end tell\n";
			
			//System.out.println("command:"+command);
			
			System.out.println("InDesign Processing.....");
			consoleLog.log("InDesign Processing.....");
			
			result = utilities.osascript_call(command);
			System.out.println("Applescript response:"+utilities.json_pretty_print(result));
			consoleLog.log("Applescript response:"+utilities.json_pretty_print(result));
			if((result != null) && (!result.isEmpty()) && (!result.equals("")))
			{
				JSONParser parser = new JSONParser();
				if(((String) result).charAt(0) == '{' )
				{
					Object appleScrObj = parser.parse((String) result);
					JSONObject jo = (JSONObject) appleScrObj;
			        String response = (String) jo.get("response");
			        if(response.equals("200") || response.equals("404"))
			        {
			        	if(response.equals("200"))
			        	{
			        		appleScriptStatus = true;
			        		//appleScriptStatus  = 1;
			        		//url call
			        		JSONObject obj=new JSONObject();
			        		obj.put("jobId",jobId);
			        		obj.put("clientId",clientId);
			        		obj.put("chapterName",chName);
			        		obj.put("inDTemplateStatus","true");
			        		//System.out.println(JSONValue.toJSONString(obj));
			        		//consoleLog.log(JSONValue.toJSONString(obj));
			        		
			        		consoleLog.log("URL : http://"+url_request.serverIp+"/maestro/updateChapterTemplateStatus" + ", type : \"PUT\" and content : \""+JSONValue.toJSONString(obj)+"\"");
			        		String urlResponse = url_request.urlRequestProcess("http://"+url_request.serverIp+"/maestro/updateChapterTemplateStatus","PUT",JSONValue.toJSONString(obj));
			        		
			        		System.out.println("Response:"+utilities.json_pretty_print(urlResponse));
			        		consoleLog.log("Response:"+utilities.json_pretty_print(urlResponse));
			        		//consoleLog.log("Response:"+url.putUrlRequest());
			        		
			        		JSONObject obj1=new JSONObject();
			        		obj1.put("jobId",jobId);
			        		obj1.put("clientId",clientId);
			        		//"chapterDate":"28-06-2018 20:01:03",
			        		//"styleSheetModifiedDate": "26-06-2018 23:01:00"
			        		
			        		System.out.println("Before Format : " + new File(pathString + "/" + chName + ".docx").lastModified());
			            	SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
			            	System.out.println("After Format : " + sdf.format(new File(pathString + "/" + chName + ".docx").lastModified()));
			        		
			        		obj1.put("chapterDate",sdf.format(new File(pathString + "/" + chName + ".docx").lastModified()));
			        		obj1.put("styleSheetModifiedDate",sdf.format(new File(pathString + "/" + chName + ".xlsx").lastModified()));
			        		
			        		consoleLog.log(JSONValue.toJSONString(obj1));
			        		System.out.println(JSONValue.toJSONString(obj1));
			        		
			        		consoleLog.log("URL : http://"+url_request.serverIp+"/maestro/updateStatus, type : \"PUT\" and content : "+JSONValue.toJSONString(obj1)+"\"");
			        		urlResponse = url_request.urlRequestProcess("http://"+url_request.serverIp+"/maestro/updateStatus","PUT",JSONValue.toJSONString(obj1));
			        		
			        		consoleLog.log("Response:"+utilities.json_pretty_print(urlResponse)+"\n");
			        		System.out.println("Response:"+utilities.json_pretty_print(urlResponse));
			        		
			        		//String chName;
			        		
			        		//sample : sendMail("CRC Team", "SUCCESS", "9781138556850_Ilyas_CH01", "", "");
			        		//sending mail to CRC Team
			        		mailObj = new mail(URLEncoder.encode("CRC Team", "UTF-8"),"SUCCESS",chName,"", "");
			    			//mailObj.mailProcess(URLEncoder.encode("CRC Team", "UTF-8"),"SUCCESS",chName.substring(0,chName.indexOf(".docx")),"", "");
			    			Thread mailThread7 = new Thread(mailObj, "Mail Thread for CRC Team");
			            	mailThread7.start();
			            	
			            	//sample : sendMail("Template", "SUCCESS", "9781138556850_Ilyas_CH01", "", "");
			            	//sending mail to Template Team
			            	mailObj = new mail("Template","SUCCESS",chName,"", "");
			    			//mailObj.mailProcess("Template","ERROR",chName.substring(0,chName.indexOf(".docx")),System.getProperty ("user.home")+"/Desktop/Maestro_QS/"+chName.substring(0,chName.indexOf(".docx"))+"_InDTReport.xls", "");
			    			Thread mailThread8 = new Thread(mailObj, "Mail Thread for Template Team");
			            	mailThread8.start();
			            	
			            	System.out.println("InDesign process finished Successfully.\n");
			            	consoleLog.log("InDesign process finished Successfully.\n");
			        	}
			        	else if(response.equals("404"))
			        	{
			        		String templateStatus = (String) jo.get("status");
			        		System.out.println("templateStatus:"+templateStatus);
			        		consoleLog.log("templateStatus:"+templateStatus);
			        		
			        		//sample : sendMail("CRC Team", "ERROR", "9781138556850_Ilyas_CH01", "", "");
			            	//sending mail to Template Team
//			        		mailObj = new mail(URLEncoder.encode("CRC Team", "UTF-8"),"ERROR",chName,System.getProperty ("user.home")+"/Desktop/Maestro_QS/"+chName+"_InDTReport.xls", "");
//			    			//mailObj.mailProcess(URLEncoder.encode("CRC Team", "UTF-8"),"SUCCESS",chName.substring(0,chName.indexOf(".docx")),"", "");
//			    			Thread mailThread9 = new Thread(mailObj, "Mail Thread for CRC Team");
//			            	mailThread9.start();
			        		String attachment = "";
			        		
			        		//check for attachment
			        		if(utilities.fileCheck(System.getProperty ("user.home")+"/Desktop/Maestro_QS/"+chName+"_InDTReport.xls"))
			        		{	
			        			attachment = System.getProperty ("user.home")+"/Desktop/Maestro_QS/"+chName+"_InDTReport.xls";
			        			mailObj = new mail("Template","ERROR",chName,attachment, "");
				    			//mailObj.mailProcess("Template","ERROR",chName.substring(0,chName.indexOf(".docx")),System.getProperty ("user.home")+"/Desktop/Maestro_QS/"+chName.substring(0,chName.indexOf(".docx"))+"_InDTReport.xls", "");
				    			Thread mailThread10 = new Thread(mailObj, "Mail Thread for Template Team");
				            	mailThread10.start();
			        		}
			        		else
			        		{
			        			mailObj = new mail("Pre-editing","ERROR",chName,attachment, "");
				    			//mailObj.mailProcess("Template","ERROR",chName.substring(0,chName.indexOf(".docx")),System.getProperty ("user.home")+"/Desktop/Maestro_QS/"+chName.substring(0,chName.indexOf(".docx"))+"_InDTReport.xls", "");
				    			Thread mailThread10 = new Thread(mailObj, "Mail Thread for Template Team");
				            	mailThread10.start();
			        		}
			        		
			    			//processError = true;
			            	
			            	System.out.println("InDesign process finished with Error.\n");
			            	consoleLog.log("InDesign process finished with Error.\n");
			        	}
			        }
				}
	        }
			else
			{
				System.out.println("InDesign could not access the \"Generic/standard style sheet\".");
        		consoleLog.log("InDesign could not access the \"Generic/standard style sheet\".");
				
				mailObj = new mail(URLEncoder.encode("CRC Team", "UTF-8"),"ERROR",chName,"", "");
    			//mailObj.mailProcess(URLEncoder.encode("CRC Team", "UTF-8"),"SUCCESS",chName.substring(0,chName.indexOf(".docx")),"", "");
    			Thread mailThread9 = new Thread(mailObj, "Mail Thread for CRC Team");
            	mailThread9.start();
			}
		 } 
		 catch (Exception e) 
		 {
			consoleLog.log(e.toString());
		 }
		 return appleScriptStatus;
	 }
	 
	/*private static String json_pretty_print(String json)
	{
		if(!json.isEmpty())
		{
			json = json.replace("{","{\n    ");
			json = json.replace(",",",\n    ");
			json = json.replace("}","\n}");
			//System.out.println("JSON:"+json);
			return json;
		}
		else
			return "";
	}*/
	 //templatePath, mapPath and used styles path details check
	 /*void jobParam()
	 {
		 try
		 {
			 String urlParams = "jobId="+URLEncoder.encode(jobId,"UTF-8")+"&clientId="+URLEncoder.encode(clientId,"UTF-8");//+"&chapter="+URLEncoder.encode(temp,"UTF-8")
	 		//local server 172.16.4.112, live - 172.16.1.25
	     	
	     	consoleLog.log("URL:\"http://"+url_request.serverIp+"/maestro/getTemplatePathDetails?"+urlParams + "\",type:\"GET\"\n");
	     	String templateResponse = url_request.urlRequestProcess("http://"+url_request.serverIp+"/maestro/getTemplatePathDetails?"+urlParams,"GET","");
	 		System.out.println("templateResponse:"+json_pretty_print(templateResponse));
	 		consoleLog.log("templateResponse:"+json_pretty_print(templateResponse)+"\n");
	 		
	 		if((!templateResponse.isEmpty()) && (!templateResponse.equals("")))
			{
	 			String templateNameLoc,templatePathLoc,importMapPathLoc,styleSheetPathLoc;
	 			
		 		JSONParser parser = new JSONParser();
				Object templateObj = parser.parse(templateResponse);
		        JSONObject jo = (JSONObject) templateObj;
			
		        templateNameLoc = (String) jo.get("templateName");
		        templatePathLoc = (String) jo.get("templatePath");
		        importMapPathLoc = (String) jo.get("maestroMappingPath");
		        styleSheetPathLoc = (String) jo.get("styleSheetPath");
		        
		        if(((templateNameLoc != null) && (!templateNameLoc.equals("")) && (!templateNameLoc.isEmpty())) 
		        	&& ((templatePathLoc != null) && (!templatePathLoc.equals("")) && (!templatePathLoc.isEmpty()))
		        	&& ((importMapPathLoc != null) && (!importMapPathLoc.equals("")) && (!importMapPathLoc.isEmpty()))
		        	&& ((styleSheetPathLoc != null) && (!styleSheetPathLoc.equals("")) && (!styleSheetPathLoc.isEmpty())))
		        {
		        	templatePathLoc = templatePathLoc.replace('\\','/');
		        	importMapPathLoc = importMapPathLoc.replace('\\','/');
		        	styleSheetPathLoc = styleSheetPathLoc.replace('\\','/');
		        	
		        	templatePathLoc = templatePathLoc.substring(templatePathLoc.indexOf("//")+2,templatePathLoc.length());
		        	templatePathLoc = templatePathLoc.substring(templatePathLoc.indexOf('/')+1,templatePathLoc.length());
		        	templatePathLoc = "/Volumes/"+templatePathLoc; //templatePath
		    		
		        	importMapPathLoc = importMapPathLoc.substring(importMapPathLoc.indexOf("//")+2,importMapPathLoc.length());
		        	importMapPathLoc = importMapPathLoc.substring(importMapPathLoc.indexOf('/')+1,importMapPathLoc.length());
		        	importMapPathLoc = "/Volumes/"+importMapPathLoc; //maestro mapping path
		    		
		        	styleSheetPathLoc = styleSheetPathLoc.substring(styleSheetPathLoc.indexOf("//")+2,styleSheetPathLoc.length());
		        	styleSheetPathLoc = styleSheetPathLoc.substring(styleSheetPathLoc.indexOf('/')+1,styleSheetPathLoc.length());
		        	styleSheetPathLoc = "/Volumes/"+styleSheetPathLoc; //styleSheetPath
		    		
//		        	System.out.println("templatePath : "+jobParams[1]);
//		    		System.out.println("maestroMappingPath : "+jobParams[2]);
//		    		System.out.println("styleSheetPath : "+jobParams[3]+"\n");
//		    		
//		    		consoleLog.log("templatePath : "+jobParams[1]);
//		    		consoleLog.log("maestroMappingPath : "+jobParams[2]);
//		    		consoleLog.log("styleSheetPath : "+jobParams[3]+"\n");
		        	
		    		boolean templatePathStatus = utilities.folderCheck(templatePathLoc);
		    		boolean maestroMappingPathStatus = utilities.folderCheck(importMapPathLoc);
		    		boolean styleSheetPathStatus = utilities.fileCheck(styleSheetPathLoc);
		    		
		    		System.out.println("fileCheckStatus of templatePath : "+templatePathStatus);
		    		System.out.println("fileCheckStatus of maestroMappingPath : "+maestroMappingPathStatus);
		    		System.out.println("fileCheckStatus of styleSheetPath : "+styleSheetPathStatus+"\n");
		    		
		    		consoleLog.log("fileCheckStatus of templatePath : "+templatePathStatus);
		    		consoleLog.log("fileCheckStatus of maestroMappingPath : "+maestroMappingPathStatus);
		    		consoleLog.log("fileCheckStatus of styleSheetPath : "+styleSheetPathStatus+"\n");
		    		
		    		if(!templatePathStatus && !maestroMappingPathStatus && !styleSheetPathStatus)
		    		{
		    			templateName = "";
		    			templatePath = "";
		    			importMapPath = "";
		    			styleSheetPath = "";
		    		}
		    		else
		    		{
		    			templateName = templateNameLoc;
		    			templatePath = templatePathLoc;
		    			importMapPath = importMapPathLoc;
		    			styleSheetPath = styleSheetPathLoc;
		    		}
		        }
		        else
		        {
		        	templateName = "";
	    			templatePath = "";
	    			importMapPath = "";
	    			styleSheetPath = "";
		        }
	 		}
		 }
		 catch(Exception e)
		 {
			 e.printStackTrace();
		 }
		 return;
	 }*/
	 
	private boolean isCompletelyWritten(File file) throws InterruptedException
    {
        Long fileSizeBefore = FileUtils.sizeOf(file);//size(file.toPath());
        Thread.sleep(3000);
        Long fileSizeAfter = FileUtils.sizeOf(file);//size(file.toPath());

        //System.out.println("comparing file size " + fileSizeBefore + " with " + fileSizeAfter);
        if (fileSizeBefore.equals(fileSizeAfter)) 
        {
            return true;
        }
        System.out.println("Copying.........");
        return false;
    }
	 
	void suspend() 
	{
		suspended = true;
	}
	   
	void resume() 
	{
		suspended = false;
		//notify();
	}
}