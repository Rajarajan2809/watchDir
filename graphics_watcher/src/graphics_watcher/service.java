package graphics_watcher;

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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import javax.script.ScriptEngine;
//import javax.script.ScriptEngineManager;

//import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.*;

import graphics_watcher.mail;

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
public class service implements Runnable
{
    private final WatchService watcher;
    private final Map<WatchKey,Path> keys;
    private boolean trace = false;
    private final AtomicInteger counter;
    private int processedFiles = 0;
    private String pathString, jobId, clientId;
    mail mailObj;
    job j1;
    //private boolean processError = false;
    //private utilities U = new utilities();
    //private final boolean recursive = false;
	//String templateName;
	//String templatePath;
	//String styleSheetPath;
	//String maestroMappingPath;
    //list of dirs as static
   // private ArrayList<String> docxTitles = new ArrayList<String>();
	//private ArrayList<String> xlsStyles = new ArrayList<String>();
    
    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) 
    {
        return (WatchEvent<T>)event;
    }

    /**
     * Register the given directory with the WatchService
     */
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

    /**
     * Register the given directory, and all its sub-directories, with the
     * WatchService.
     */
//    private void registerAll(final Path start) throws IOException 
//    {
//        // register directory and sub-directories
//        Files.walkFileTree(start, new SimpleFileVisitor<Path>() 
//        {
//            @Override
//            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
//                throws IOException
//            {
//                register(dir);
//                return FileVisitResult.CONTINUE;
//            }
//        });
   // }

    /**
     * Creates a WatchService and registers the given directory
     * @param noOfManuScripts 
     * @throws ParseException 
     */
    service(String pathString, String jobId, String clientId,AtomicInteger counter) throws IOException, ParseException 
    {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<WatchKey,Path>();
        this.pathString = pathString;
        this.clientId = clientId;
        this.jobId = jobId;
        this.counter = counter;
        //this.jobFailError = false;
        Path listPath;
        
        //this.recursive = false;//to avoid checking sub directories ERROR folder
        //ArrayList<Path> listPath = new ArrayList<Path>();
        //if (recursive) 
        //{
            //for(int i=0; i < listA.size() ;i++)
            //{
            	
            	//listPath.add(Paths.get(pathString));
            	listPath = Paths.get(pathString);
            	register(listPath);
            	//System.out.format("Scanning \"%s\" ...\n", pathString);
            	//System.out.println("Done.");
            	
            	//consoleLog.log("Scanning \""+pathString+"\" ...\n");
            	//consoleLog.log("Done.");
            //}
        //} 
        /*else 
        {
        	//for(int i=0; i < listA.size();i++)
        	//{
        		listPath = Paths.get(pathString);
            	registerAll(listPath);
        	//}
        }*/

        // enable trace after initial registration
        this.trace = true;
    }

    private void initProcessing() throws FileNotFoundException, IOException, ParseException
    {
    	File folder = new File(pathString);
        File[] listOfFiles = folder.listFiles();
        //String[] strList = folder.list();
        //listOfFiles.length
        
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
						
						//System.out.println("jobFailError : "+jobFailError);
	    				
//	    				if(job.jobFailErrorFun(jobId, clientId))
//	    				{
//	    					return;
//	    				}
						//processedFiles++;
					}
					else if(m.matches() && (listOfFiles[i].getName().lastIndexOf(".xlsx") > 0))
					{
						System.out.println("xlsx file created at "+listOfFiles[i].getName());
	    				consoleLog.log("xlsx file created at "+listOfFiles[i].getName()+"\n");
	    				
	    				if(!utilities.fileCheck(pathString +"/"+listOfFiles[i].getName()))
	    				{
	    					System.out.println("Manuscript not found, hence moved to \"ERROR\" folder");
	        				consoleLog.log("Manuscript not found, hence moved to \"ERROR\" folder");
	        				
	        				utilities.fileMove(pathString+"/"+listOfFiles[i].getName(),pathString+"/ERROR/"+listOfFiles[i].getName());
	    				}
					}
					else if(!listOfFiles[i].getName().equals(".DS_Store"))
					{
						//invalid other format files
			        	File theDir = new File(pathString+"/INVALID_FILES/");
			        	if (!theDir.exists()) 
						{
			        		theDir.mkdir();
						}
			        	utilities.fileMove(pathString+"/"+listOfFiles[i].getName(),pathString+"/INVALID_FILES/"+listOfFiles[i].getName());
						
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
					if(!listOfFiles[i].getName().equals("ERROR") && !listOfFiles[i].getName().equals("INVALID_FILES") && !listOfFiles[i].getName().equals("Equations"))
		        	{
						File theDir = new File(pathString+"/INVALID_FILES/");
		        	
			        	if (!theDir.exists()) 
						{
			        		theDir.mkdir();
						}
			        	utilities.recurMove(new File(pathString+"/"+listOfFiles[i].getName()), new File (pathString+"/INVALID_FILES/"+listOfFiles[i].getName()));
						
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
    int processEvents() throws IOException, ParseException 
    {
    	boolean initFlag = false;
    	
		for (;;) 
        {
        	if(initFlag == false)
        	{
        		initProcessing();
        		initFlag = true;
        		
//        		postValidation postVal = new postValidation(pathString+"/ERROR/", jobId, clientId);
//				Thread postValThread = new Thread(postVal, "Watch Thread for \"ERROR\" folder.");
//				postValThread.start();
        		
        		//template path or stylesheet path or map path is missing
        		System.out.println("jobId:"+jobId);
        		System.out.println("clientId:"+clientId);
//        		if(job.jobFailErrorFun(jobId, clientId))
//				{
//					return 1;
//				}
        		
        		//this prints when job started watching folder
        		System.out.println("Watch Folder initiated......\n");
				consoleLog.log("Watch Folder initiated......\n");
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
            				
            				System.out.println("chap_name:"+fileNameWoExtn);
            				System.out.println("Manuscript name match:"+m.matches());
            				
            				consoleLog.log("chap_name:"+fileNameWoExtn);
            				consoleLog.log("Manuscript name match:"+m.matches()+"\n");
            				
	            			if(m.matches() && (utilities.getFileExtension(createdFile).equals("docx") || utilities.getFileExtension(createdFile).equals("xlsx")))
	                    	{
	            				if(m.matches() && utilities.getFileExtension(createdFile).equals("docx"))
	            				{
		            				//System.out.print("Docx file created at ");
		            				//System.out.format("%s\n", child);
		            				//consoleLog.log("Docx file created at "+child+"\n");
		            				
		            				//process the manuscript
//		            				System.out.println("jobFailError : "+job.jobFailErrorFun(jobId, clientId));
//		            				
//		            				//template path or stylesheet path or map path is missing
//		            				if(job.jobFailErrorFun(jobId, clientId))
//		            				{
//		            					System.out.println("Job Fail Error");
//		            					return 5;
//		            				}
		            			}
	            				else if(m.matches() && utilities.getFileExtension(createdFile).equals("xlsx"))
	            				{
	            					System.out.print("xlsx file created at ");
		            				System.out.format("%s\n", child);
		            				consoleLog.log("xlsx file created at "+child+"\n");
		            				
		            				if(!utilities.fileCheck(pathString +"/"+child.getFileName().toString()))
		            				{
		            					System.out.println("Manuscript not found, hence moved to \"ERROR\" folder");
		                				consoleLog.log("Manuscript not found, hence moved to \"ERROR\" folder");
		                				
		                				utilities.fileMove(pathString+"/"+child.getFileName().toString(),pathString+"/ERROR/"+child.getFileName().toString());
		            				}
	            				}
	                    	}
	            			else if(!createdFile.getName().equals(".DS_Store"))
            				{
	            				System.out.println(child.getFileName().toString()+" is a Invalid file.");
            					//Move manuscripts to error folder
            		        	File theDir = new File(pathString+"/INVALID_FILES/");
            		        	if (!theDir.exists()) 
            					{
            		        		theDir.mkdir();
            					}
            		        	utilities.fileMove(pathString+"/"+child.getFileName().toString(),pathString+"/INVALID_FILES/"+child.getFileName().toString());
            		        	
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
            				if(!child.getFileName().toString().equals("ERROR") && !child.getFileName().toString().equals("INVALID_FILES") && !child.getFileName().toString().equals("Equations"))
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
    		if(pathString.indexOf("ERROR") != -1)
    		{
    			//TimeUnit.SECONDS.sleep(2);
    			System.out.println("pathString:"+pathString);
    			//postValidation(pathString);
    		}
    		else
    		{
    			int processStatus;
    			//watch thread job
	    	    //this.jobId = jobId;
	            //this.noOfManuScripts = noOfManuScripts;
	            //this.counter = counter;
	           // Path listPath;
    			//int processStatus;
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
		    			System.out.println("Process status:"+processStatus);
		    			consoleLog.log("Process status:"+processStatus);
				    	String osResp = utilities.serverMount();
						System.out.println("Mount response:" + osResp+"\n");
						consoleLog.log("Mount response:" + osResp+"\n");
						
						if(osResp.equals("Disk Found"))
							continue job_continue;
						else
						{
							// mail to netops
							consoleLog.log("MOUNT ERROR mail sent to group \"netops\"");
							// smaple : sendMail("Net-ops","rajarajan@codemantra.in", "", "MOUNT", "", "");
							mail m = new mail("Net-ops", "ERROR", "MOUNT", "", "");
							// m.mailProcess("Net-ops", "ERROR", "MOUNT", "", "");
							Thread mailThread = new Thread(m, "Mail Thread for Template path mount");
							mailThread.start();
						}
    				}
    				//successful job finish
    				else
    					break;
    			}
        		
    			JSONObject obj=new JSONObject();
	    		obj.put("jobId",jobId);
	    		obj.put("clientId",clientId);
	    		
	    		//System.out.println("processError for "+jobId+" :"+processError);
	         	
	         	//DateFormat dateFormat2 = new SimpleDateFormat("dd-MMM-yy hh:mm:ss aa");
	        	//String dateString2 = dateFormat2.format(new Date()).toString();
	         	
	         	//String jsonText = JSONValue.toJSONString(obj);  
	    		//System.out.println(jsonText);
	         	//jobStatus update
	         	
	    		
	         	job j1 = new job();
	         	j1.job_update(jobId);
			}
    	}
    	catch (IOException e) 
    	{
			e.printStackTrace();
			try 
			{
				consoleLog.log(e.toString());
			} 
			catch (IOException e1) 
			{
				e1.printStackTrace();
			}
		}
    	catch (ParseException e) 
    	{
			e.printStackTrace();
			try 
			{
				consoleLog.log(e.toString());
			} 
			catch (IOException e1) 
			{
				e1.printStackTrace();
			}
		} 
//    	catch (InterruptedException e) 
//    	{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
    	return;
	}
	
	private boolean functionalityCheck(String file) throws IOException,FileNotFoundException, ParseException
	{
		boolean styleSheetfileStatus = false, contentModellingStatus = false, jsxProcessStatus = false, eqnFolderStatus = false;// 0 > failed,0 = success
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
		
        if(styleSheetfileStatus && contentModellingStatus )
        {
    		//172.16.4.112:8088/maestro/getChapterEquation?jobId=9781138556850_Ilyas&clientId=TF_STEM&chapter=9781138556850_Ilyas_CH11
        }
        
        //condition for inDesign to initiate or not
        if(styleSheetfileStatus && contentModellingStatus && eqnFolderStatus)
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
				
				
				
	        	if(JavaApplescriptTest(chName,pathString+"/"+chName+".xlsx","","","",""))
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
			processedFiles++;
		}
		else
		{
			//Move manuscripts to error folder
        	File theDir = new File(pathString+"/ERROR/");
        	if (!theDir.exists()) 
			{
        		theDir.mkdir();
			}
        	utilities.fileMove(pathString+"/"+chName+".docx",pathString+"/ERROR/"+chName+".docx");
			if(utilities.fileCheck(pathString +"/"+chName+".xlsx"))
				utilities.fileMove(pathString+"/"+chName+".xlsx",pathString+"/ERROR/"+chName+".xlsx");
			//processedFiles--;
		}
		
		return (styleSheetfileStatus && contentModellingStatus && jsxProcessStatus);
	}
	 
	@SuppressWarnings("unchecked")
	public boolean JavaApplescriptTest(String chName,String stylePath,String templateName,String templatePath,String mapPath,String styleSheetPath) throws IOException
	 {
		boolean appleScriptStatus = false;
		String result = "";
		
		//System.out.println("jobId:"+jobId);
		//System.out.println("clientId:"+clientId);
		//System.out.println("chName:"+chName);
		System.out.println("templateName:"+templateName);
		System.out.println("templatePath:"+templatePath);
		System.out.println("styleSheetPath:"+styleSheetPath);
		System.out.println("mapPath:"+mapPath+"\n");
		
		consoleLog.log("templateName : "+templateName);
		consoleLog.log("templatePath : "+templatePath);
		consoleLog.log("styleSheetPath : "+styleSheetPath);
		consoleLog.log("mapPath : "+mapPath+"\n");
		//ScriptEngineManager manager = new ScriptEngineManager();
		//ScriptEngine engine = manager.getEngineByName("AppleScript");
		//String pathFolder = path.get(i).substring(path.get(i).indexOf("//")+2,path.get(i).length());
		
		try 
		{
			String command = "set aScriptPath to \"Users:"+System.getProperty ("user.name")+":Library:Preferences:Adobe InDesign:Version 10.0:en_US:Scripts:Scripts Panel:Maestro_Styles_Validations_v1.0.jsx\"\n" +
			"set myParameters to {\""+ clientId +"\",\"" + jobId +"\", \"" + chName +".docx\", \"" + stylePath +"\",\"" + templateName +"\",\"" + templatePath +"\", \"" + mapPath +"\",\"" + styleSheetPath +"\"}\n" +
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
			System.out.println("Applescript response:"+json_pretty_print(result));
			consoleLog.log("Applescript response:"+json_pretty_print(result));
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
			        		
			            	mailObj = new mail("Template","ERROR",chName,System.getProperty ("user.home")+"/Desktop/Maestro_QS/"+chName+"_InDTReport.xls", "");
			    			//mailObj.mailProcess("Template","ERROR",chName.substring(0,chName.indexOf(".docx")),System.getProperty ("user.home")+"/Desktop/Maestro_QS/"+chName.substring(0,chName.indexOf(".docx"))+"_InDTReport.xls", "");
			    			Thread mailThread10 = new Thread(mailObj, "Mail Thread for Template Team");
			            	mailThread10.start();
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
			//reader.close();
		 } 
		 catch (Exception e) 
		 {
		     //e.printStackTrace();
			consoleLog.log(e.toString());
//			switch(e.toString().substring(0,e.toString().indexOf(":")))
//			{
//				case "java.nio.file.NoSuchFileException":
//				{
//					System.out.println("Javascript error");
//				}
//				break;
//			}	
		 }
		 return appleScriptStatus;
	 }
	 
	private String json_pretty_print(String json)
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
	}
	 //templatePath, mapPath and used styles path details check
	
}
	
//	 private boolean fileMove(String fromFolder,String toFolder)
//	 {
//		 boolean status = false;
//		 try 
//		{
//			File folder = new File("/Users/comp/Desktop/test1/");
//			File[] listOfFiles = folder.listFiles();
//			for (int i = 0; i < listOfFiles.length; i++) 
//			{
//				if (listOfFiles[i].isFile()) 
//				{
//					//System.out.println("File " + listOfFiles[i].getName());
//					Files.move(Paths.get("/Users/comp/Desktop/test1/"+listOfFiles[i].getName()), Paths.get("/Users/comp/Desktop/test files/"+listOfFiles[i].getName()), StandardCopyOption.REPLACE_EXISTING);
//			    }
//				else if (listOfFiles[i].isDirectory()) 
//				{
//					System.out.println("Directory " + listOfFiles[i].getName());
//			    }
//			}
//		}
//		catch (IOException e) 
//		{
//			e.printStackTrace();
//		}
//		return status;
//	 }
//}