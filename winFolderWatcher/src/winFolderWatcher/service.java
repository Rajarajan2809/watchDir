package winFolderWatcher;

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
//import java.text.DateFormat;
//import java.text.SimpleDateFormat;
import java.io.*;
//import java.net.URLEncoder;
import java.util.*;
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;

//import javax.script.ScriptEngine;
//import javax.script.ScriptEngineManager;

//import org.json.simple.JSONArray;

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
   // private final AtomicInteger counter;
    private String pathString, jobId, jsxFile, localFolder;
    mail mailObj;
    //job j1;
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
    service(String pathString) throws IOException 
    {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<WatchKey,Path>();
        this.pathString = pathString;
        
        //this.jobFailError = false;
        Path listPath;
        
        this.localFolder = System.getProperty ("user.home")+"/Desktop/Graphics_QS/IN";
        
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

    private void initProcessing() throws FileNotFoundException, IOException
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
					
					//String REGEX = jobId+"_CH\\d\\d|"+jobId+"_FM\\d\\d|"+jobId+"_BM\\d\\d|"+jobId+"_RM\\d\\d|"+jobId+"_PT\\d\\d";
					
					//regex matching
					//Pattern p = Pattern.compile(REGEX);
					//Matcher m = p.matcher(utilities.getFileNameWithoutExtension(listOfFiles[i]));   // get a matcher object
					if(!listOfFiles[i].getName().equals(".DS_Store"))
					{
						functionalityCheck(listOfFiles[i].getName());
					}
					else
					{
						File theDir = new File(pathString+"/ERROR/");
			        	if (!theDir.exists()) 
						{
			        		theDir.mkdir();
						}
			        	utilities.fileMove(pathString+"/"+listOfFiles[i].getName(),pathString+"/ERROR/"+listOfFiles[i].getName());
					}
//					if(m.matches() && (listOfFiles[i].getName().lastIndexOf(".docx") > 0))
//					{
//						//System.out.println("File " + listOfFiles[i].getName());
//						
//						//System.out.println("jobFailError : "+jobFailError);
//						functionalityCheck(listOfFiles[i].getName());
////	    				if(job.jobFailErrorFun(jobId, jsxFile))
////	    				{
////	    					return;
////	    				}
//						//processedFiles++;
//					}
//					else if(m.matches() && (listOfFiles[i].getName().lastIndexOf(".xlsx") > 0))
//					{
//						System.out.println("xlsx file created at "+listOfFiles[i].getName());
//	    				consoleLog.log("xlsx file created at "+listOfFiles[i].getName()+"\n");
//	    				
//	    				if(!utilities.fileCheck(pathString +"/"+listOfFiles[i].getName()))
//	    				{
//	    					System.out.println("Manuscript not found, hence moved to \"ERROR\" folder");
//	        				consoleLog.log("Manuscript not found, hence moved to \"ERROR\" folder");
//	        				
//	        				utilities.fileMove(pathString+"/"+listOfFiles[i].getName(),pathString+"/ERROR/"+listOfFiles[i].getName());
//	    				}
//					}
//					else if(!listOfFiles[i].getName().equals(".DS_Store"))
//					{
//						//invalid other format files
//			        	File theDir = new File(pathString+"/ERROR/");
//			        	if (!theDir.exists()) 
//						{
//			        		theDir.mkdir();
//						}
//			        	utilities.fileMove(pathString+"/"+listOfFiles[i].getName(),pathString+"/INVALID_FILES/"+listOfFiles[i].getName());
//						
////			        	mailObj = new mail("Pre-editing", "INVALID", jobId, "", listOfFiles[i].getName());
////						Thread mailThread6 = new Thread(mailObj, "Mail Thread for Pre editing Team");
////			        	mailThread6.start();
//			        	
//						//xlsx and docx files does not match regex
//						System.out.println("Waiting for more files1...");
//						consoleLog.log("Waiting for more files1...");
//					}
				}
				else if (listOfFiles[i].isDirectory()) 
				{
					//Move manuscripts to error folder
					if(!listOfFiles[i].getName().equals("ERROR"))
		        	{
						utilities.recurMove(new File(pathString+"/"+listOfFiles[i].getName()), new File (pathString+"/ERROR/"+listOfFiles[i].getName()));
						
//			        	mailObj = new mail("Pre-editing", "INVALID", jobId, "", listOfFiles[i].getName());
//						Thread mailThread6 = new Thread(mailObj, "Mail Thread for Pre editing Team");
//			        	mailThread6.start();
			        	
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
    int processEvents() throws IOException 
    {
    	boolean initFlag = false;
    	
		for (;;) 
        {
        	if(initFlag == false)
        	{
        		initProcessing();
        		initFlag = true;
        		
//        		postValidation postVal = new postValidation(pathString+"/ERROR/", jobId, jsxFile);
//				Thread postValThread = new Thread(postVal, "Watch Thread for \"ERROR\" folder.");
//				postValThread.start();
        		
        		//template path or stylesheet path or map path is missing
//        		System.out.println("jobId:"+jobId);
//        		System.out.println("jsxFile:"+jsxFile);
//        		if(job.jobFailErrorFun(jobId, jsxFile))
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
            				//String REGEX = jobId+"_CH\\d\\d|"+jobId+"_FM\\d\\d|"+jobId+"_BM\\d\\d|"+jobId+"_RM\\d\\d|"+jobId+"_PT\\d\\d";
            				
            				//regex matching
            				//Pattern p = Pattern.compile(REGEX);
            				//Matcher m = p.matcher(fileNameWoExtn);   // get a matcher object
            				
            				System.out.println("chap_name:"+fileNameWoExtn);
            				//System.out.println("Manuscript name match:"+m.matches());
            				
            				consoleLog.log("chap_name:"+fileNameWoExtn);
            				//consoleLog.log("Manuscript name match:"+m.matches()+"\n");
            				
	            			/*if(m.matches() && (utilities.getFileExtension(createdFile).equals("docx") || utilities.getFileExtension(createdFile).equals("xlsx")))
	                    	{
	            				if(m.matches() && utilities.getFileExtension(createdFile).equals("docx"))
	            				{
		            				//System.out.print("Docx file created at ");
		            				//System.out.format("%s\n", child);
		            				//consoleLog.log("Docx file created at "+child+"\n");
	            					functionalityCheck(child.getFileName().toString());
		            				//process the manuscript
//		            				System.out.println("jobFailError : "+job.jobFailErrorFun(jobId, jsxFile));
//		            				
//		            				//template path or stylesheet path or map path is missing
//		            				if(job.jobFailErrorFun(jobId, jsxFile))
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
	            			else*/ if(!createdFile.getName().equals(".DS_Store"))
            				{
	            				System.out.println(child.getFileName().toString()+" is a valid file.");
	            				functionalityCheck(child.getFileName().toString());
            					//Move manuscripts to error folder
//            		        	File theDir = new File(pathString+"/INVALID_FILES/");
//            		        	if (!theDir.exists()) 
//            					{
//            		        		theDir.mkdir();
//            					}
//            		        	utilities.fileMove(pathString+"/"+child.getFileName().toString(),pathString+"/INVALID_FILES/"+child.getFileName().toString());
//            		        	
//            		        	mailObj = new mail("Pre-editing", "INVALID", jobId, "", child.getFileName().toString());
//            					Thread mailThread6 = new Thread(mailObj, "Mail Thread for Pre editing Team");
//            		        	mailThread6.start();
            		        	
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
//	        		        	File theDir = new File(pathString+"/INVALID_FILES/");
//	        		        	if (!theDir.exists()) 
//	        					{
//	        		        		theDir.mkdir();
//	        					}
//	        		        	
//	        		        	utilities.recurMove(new File(pathString+"/"+child.getFileName().toString()), new File(pathString+"/INVALID_FILES/"+child.getFileName().toString()));
//	        		        	
//	        		        	mailObj = new mail("Pre-editing", "INVALID", jobId, "", child.getFileName().toString());
//	        					Thread mailThread6 = new Thread(mailObj, "Mail Thread for Pre editing Team");
//	        		        	mailThread6.start();
//	        		        	
//	        		        	//xlsx and docx files does not match regex
//	        					System.out.println("Waiting for more files4...");
//	        					consoleLog.log("Waiting for more files4...");
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

	@Override
	public void run()
	{
		//boolean procStatus = false;
    	try 
    	{
    		//local job folder
    		if(!new File(localFolder).exists())
			{
				File theDir = new File(localFolder);
	        	if (!theDir.exists()) 
				{
	        		theDir.mkdir();
				}
			}
    		
    		//local JOB IN Folder
    		if(!new File(localFolder+"/IN").exists())
			{
				File theDir = new File(localFolder+"/IN");
	        	if (!theDir.exists()) 
				{
	        		theDir.mkdir();
				}
			}
    		
    		//local JOB OUT Folder
    		if(!new File(localFolder+"/OUT").exists())
			{
				File theDir = new File(localFolder+"/OUT");
	        	if (!theDir.exists()) 
				{
	        		theDir.mkdir();
				}
			}

    		//local JOB OUT Folder
    		if(!new File(pathString+"/OUT").exists())
			{
				File theDir = new File(pathString+"/OUT");
	        	if (!theDir.exists()) 
				{
	        		theDir.mkdir();
				}
			}

    		
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
			    	String osResp = utilities.mountDisk("172.16.1.2", "OEO", "rajarajan", "test@123");
					System.out.println("Mount response:" + osResp+"\n");
					consoleLog.log("Mount response:" + osResp+"\n");
					
					if(osResp.equals("Disk Found"))
						continue job_continue;
					else
					{
						// mail to netops
						consoleLog.log("MOUNT ERROR mail sent to group \"netops\"");
						// smaple : sendMail("Net-ops","rajarajan@codemantra.in", "", "MOUNT", "", "");
						mail m = new mail("rajarajan@codemantra.in", "ERROR", "MOUNT", "", "");
						// m.mailProcess("Net-ops", "ERROR", "MOUNT", "", "");
						Thread mailThread = new Thread(m, "Mail Thread for Template path mount");
						mailThread.start();
					}
				}
				//successful job finish
				else
					break;
			}
    		
//			JSONObject obj=new JSONObject();
//    		obj.put("jobId",jobId);
//    		obj.put("jsxFile",jsxFile);
    		
    		//System.out.println("processError for "+jobId+" :"+processError);
         	
         	//DateFormat dateFormat2 = new SimpleDateFormat("dd-MMM-yy hh:mm:ss aa");
        	//String dateString2 = dateFormat2.format(new Date()).toString();
         	
         	//String jsonText = JSONValue.toJSONString(obj);  
    		//System.out.println(jsonText);
         	//jobStatus update
         	
    		
//         	job j1 = new job();
//         	j1.job_update(jobId);
			
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
//    	catch (InterruptedException e) 
//    	{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
    	return;
	}
	
	private boolean functionalityCheck(String file) throws IOException, FileNotFoundException
	{
		boolean jsxProcessStatus = false;// 0 > failed,0 = success
		String chName = file;
		chName = chName.substring(0,chName.indexOf(".docx"));
		
		utilities.fileMove("pathString"+file, localFolder+file);
		
		//stylesheet for manuscript checking
		
		//stylesheet file found and contentModelling stage passed
		consoleLog.log("Stylesheet file present and PreEditStatus stage passed.\n");
		System.out.println("Stylesheet file present and PreEditStatus stage passed.\n");
		
    	if(vbScriptTest(chName))
    	{
    		//jsx process passed
    		jsxProcessStatus = true;
    	}
    	else
    	{
    		consoleLog.log("Failed in InDesign style mapping stage.\n");
    		System.out.println("Failed in InDesign style mapping stage.\n");
    	}
    		
		//Move manuscripts to error folder
    	File theDir = new File(pathString+"/ERROR/");
    	if (!theDir.exists()) 
		{
    		theDir.mkdir();
		}
    	utilities.fileMove(pathString+"/"+file,pathString+"/ERROR/"+file);
		return (jsxProcessStatus);
	}
	 
	public boolean vbScriptTest(String filePath) throws IOException
	 {
		boolean appleScriptStatus = false;
		//String result = "";
		
		try
		{
			Process p = Runtime.getRuntime().exec("wscript " + new File(filePath).getPath());
			p.waitFor();
	        p.exitValue();
	        //return (p.exitValue() == 1);
		} 
		catch (Exception e) 
		{
		    //e.printStackTrace();
			consoleLog.log(e.toString());	
		}
		 return appleScriptStatus;
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