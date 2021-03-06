package graphics_watcher;

import java.nio.file.*;
import java.time.Duration;
import java.time.Instant;

import static java.nio.file.StandardWatchEventKinds.*;
import java.io.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;

import graphics_watcher.mail;

/*
 * try
	mount volume "smb://rajarajan:Amma2809appa_@172.16.1.2/OEO/"
end try

	try
	
	mount volume "smb://172.16.1.2/Copyediting/" as user name "maestroqs@cmpl.in" with password "M@est0123"
	
end try
 */
public class service implements Runnable
{
    private final WatchService watcher;
    private final Map<WatchKey,Path> keys;
    private boolean trace = false;
    private final AtomicInteger counter;
    private String jobFolder, jobId, jsxFile, localFolder, localInFolder, localOutFolder, jobInFolder, jobOutFolder;
    mail mailObj;
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
     * Creates a WatchService and registers the given directory
     * @param noOfManuScripts 
     * @throws ParseException 
     * @throws InterruptedException 
     */
    service(String jobId,String jobFolder, String type, String jsxFile,AtomicInteger counter) throws IOException, ParseException, InterruptedException 
    {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<WatchKey,Path>();
        this.jobFolder = jobFolder;
        this.jsxFile = jsxFile;
        this.jobId = jobId;
        this.counter = counter;
        //this.type = type;
        
        Path listPath;
        this.localFolder = System.getProperty ("user.home")+"/Graphics_QS/"+jobId;
        
        //user exists or not
        File theDir = new File(System.getProperty ("user.home"));
		System.out.println("theDir1:"+theDir.exists());
    	while (!theDir.exists()) 
		{
    		theDir.mkdir();
    		TimeUnit.SECONDS.sleep(1);
		}
    	
    	//Desktop exists or not
    	theDir = new File(System.getProperty ("user.home")+"/");
		System.out.println("theDir2:"+theDir.exists());
    	while (!theDir.exists()) 
		{
    		theDir.mkdir();
    		TimeUnit.SECONDS.sleep(1);
		}
    	
    	//Graphics_QS exists or not
    	theDir = new File(System.getProperty ("user.home")+"/Graphics_QS/");
		System.out.println("theDir3:"+theDir.exists());
    	while (!theDir.exists()) 
		{
    		theDir.mkdir();
    		TimeUnit.SECONDS.sleep(1);
		}

    	//
    	theDir = new File(this.localFolder);
		System.out.println("theDir4:"+theDir.exists());
    	while (!theDir.exists()) 
		{
    		theDir.mkdir();
    		TimeUnit.SECONDS.sleep(1);
		}
        //System.out.println("localFolder:"+localFolder);
        //local job folder
        //System.out.println("localFolder:"+new File(localFolder).exists());
		
		
		//local JOB IN Folder
		this.localInFolder = localFolder+"/IN";
		theDir = new File(localFolder+"/IN");
		System.out.println("theDir5:"+theDir.exists());
    	while (!theDir.exists()) 
		{
    		theDir.mkdir();
    		TimeUnit.SECONDS.sleep(1);
		}
    	
		//local local OUT Folder
		this.localOutFolder = localFolder+"/OUT";
		//System.out.println("localOutFolder:"+this.localOutFolder);
		File theDir1 = new File(localFolder+"/OUT");
		System.out.println("theDirOut:"+theDir1.exists());
    	while (!theDir1.exists()) 
		{
    		theDir1.mkdir();
    		TimeUnit.SECONDS.sleep(1);
    		//System.out.println("status1:"+theDir1.mkdir());
		}
		
		//local JOB OUT Folder
		this.jobOutFolder = jobFolder+"/OUT";
		theDir = new File(jobFolder+"/OUT");
		System.out.println("theDir:"+theDir.exists());
    	while (!theDir.exists())
		{
    		theDir.mkdir();
    		TimeUnit.SECONDS.sleep(1);
		}
        
		this.jobInFolder = jobFolder+"/IN";
		
		//File f1 = new File(jobInFolder);
		
		//this.parentFolderName = new File(f1.getParent()).getName();
		
		//System.out.println("parent folder name:"+this.parentFolderName);
		
		//job folder processing
		initProcessing();
		
        listPath = Paths.get(this.jobInFolder);
        register(listPath);
        this.trace = false;
    }

    private void initProcessing() throws FileNotFoundException, IOException, ParseException, InterruptedException
    {
    	File folder = new File(jobFolder+"/IN");
        File[] listOfFiles = folder.listFiles();
        System.out.println("Processing already present files in Job folder...\n");
		consoleLog.log("Processing already present files in Job folder...\n");
		if(folder.exists())
		{
			for (int i = 0; i < listOfFiles.length; i++) 
			{			
				if (listOfFiles[i].isFile()) 
				{
					Collection<String> extn = new ArrayList<String>();
    				extn.add("indd");
    				extn.add("idml");
					//System.out.println("File/Folder Status:"+listOfFiles[i].getName()+"("+listOfFiles[i].isFile()+")");
					if(!listOfFiles[i].getName().equals(".DS_Store") && FilenameUtils.isExtension(listOfFiles[i].getName(),extn))
					{
						while(!isCompletelyWritten(listOfFiles[i]))
						{}
						functionalityCheck(listOfFiles[i].getName());
					}
					else
						utilities.delete(listOfFiles[i]);
				}
				else if (listOfFiles[i].isDirectory()) 
				{
					while(!isCompletelyWritten(listOfFiles[i]))
					{}
					functionalityCheck(listOfFiles[i].getName());
				}
			}
		}
    }
    
    /**
     * Process all events for keys queued to the watcher
     * @throws IOException 
     * @throws ParseException 
     * @throws InterruptedException 
     */
    int processEvents() throws IOException, ParseException, InterruptedException 
    {
    	boolean initFlag = false;
    	//String fileName = null;
		for (;;) 
        {
        	if(initFlag == false)
        	{
        		//initProcessing();
        		initFlag = true;
        		
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
                } 
                catch (IOException | DirectoryIteratorException x) 
                {
                    // IOException can never be thrown by the iteration.
                    // In this snippet, it can only be thrown by newDirectoryStream.
                    System.err.println(x);
                }
                
                // print out event
                if((kind == ENTRY_CREATE) || (kind == ENTRY_DELETE) || (kind == ENTRY_MODIFY))
                {
                	if(kind == ENTRY_CREATE)
            		{
            			File createdFile = child.toFile();
            			//System.out.println(jobFolder + "/" + child.getFileName().toString());
            			
            			if(createdFile.isFile())
            			{
            				
            				Collection<String> extn = new ArrayList<String>();
            				extn.add("indd");
            				extn.add("idml");
            				
            				//System.out.println("file extn status:"+FilenameUtils.isExtension(createdFile.getName(),extn));
            				
            				if(!createdFile.getName().equals(".DS_Store") && FilenameUtils.isExtension(createdFile.getName(),extn))
            				{ 		
            					Instant start = Instant.now();
            					while(!isCompletelyWritten(child.toFile()))
        						{}
            					Instant end = Instant.now();
            					Duration timeElapsed = Duration.between(start, end);
            					System.out.println("Time taken to copy: "+ timeElapsed.getSeconds() +" seconds");
	            				functionalityCheck(child.getFileName().toString());
            				}
            				else
            					utilities.delete(createdFile);
            			}
            			else if(createdFile.isDirectory())
            			{
            				Instant start = Instant.now();
        					while(!isCompletelyWritten(child.toFile()))
    						{}
        					Instant end = Instant.now();
        					Duration timeElapsed = Duration.between(start, end);
        					System.out.println("Time taken to copy: "+ timeElapsed.getSeconds() +" seconds");
            				functionalityCheck(child.getFileName().toString());
            			}
            		}
            		/*else if(kind == ENTRY_DELETE)
            		{
            			String fName = ev.context().getFileName().toString();
                        if (fileName != null && !fName.equals(fileName)) {
                            System.out.println("file copying completed !!" + fileName);
                        }
            		}
            		else if(kind == ENTRY_MODIFY)
            		{}*/
            		
                }
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
        }
        return 7;
    }

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
    
	@SuppressWarnings("unchecked")
	@Override
	public void run()
	{
		try 
    	{
    		    		
    		int processStatus;
			job_continue:
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
			    	String osResp = utilities.mountDisk("172.16.1.21", "COMP", "graphicsqs", "@Op=0eja");
					System.out.println("Mount response:" + osResp+"\n");
					consoleLog.log("Mount response:" + osResp+"\n");
					
					if(osResp.equals("Disk Found"))
					{
//						this.jobFolder = jobFolder;
//				        this.jsxFile = jsxFile;
//				        this.jobId = jobId;
//				        this.counter = counter;
						//new service(jobFolder,jsxFile,"Folder",jobId,counter);
						//;
					    register(Paths.get(this.jobInFolder));
						continue job_continue;
					}
					else
					{
						// mail to netops
						consoleLog.log("MOUNT ERROR mail sent to group \"netops\"");
						// smaple : sendMail("Net-ops","rajarajan@codemantra.in", "", "MOUNT", "", "");
						//mail m = new mail("rajarajan@codemantra.in", "ERROR", "MOUNT", "", "");
						// m.mailProcess("Net-ops", "ERROR", "MOUNT", "", "");
						//Thread mailThread = new Thread(m, "Mail Thread for Template path mount");
						//mailThread.start();
					}
				}
				//successful job finish
				else
					break;
			}
    		
			JSONObject obj=new JSONObject();
    		obj.put("jobId",jobId);
    		obj.put("jsxFile",jsxFile);
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
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    	return;
	}
	
	private boolean functionalityCheck(String file) throws IOException, FileNotFoundException, ParseException, InterruptedException
	{
		boolean jsxProcessStatus = false;// 0 > failed,0 = success
		
		//System.out.println("File:"+file+"\n");
		
		Instant start = Instant.now();
		
		if(new File(jobInFolder+"/"+file).isFile())
			System.out.println(Files.move(Paths.get(jobInFolder+"/"+file), Paths.get(localInFolder+"/"+file), StandardCopyOption.REPLACE_EXISTING));
		else if(new File(jobInFolder+"/"+file).isDirectory())
			utilities.recurMove(new File(jobInFolder+"/"+file), new File(localInFolder+"/"+file));
		
		Instant end = Instant.now();
		Duration timeElapsed = Duration.between(start, end);
		System.out.println("Time taken to copy: "+ timeElapsed.getSeconds() +" seconds");
		
		System.out.println("dir status:"+new File(jobInFolder+"/"+file).isDirectory());
		
		//if(new File(jobInFolder+"/"+file).isDirectory())
		//{
		
			TimeUnit.SECONDS.sleep(1);
			
			System.out.println("out folder:"+localOutFolder+"/"+file);
			File theDir1 = new File(localOutFolder+"/"+file);
			while (!theDir1.exists()) 
			{
	    		//theDir1.mkdir();
	    		System.out.println("status1:"+theDir1.mkdir());
			}
			
	    	if(JavaApplescriptTest(localInFolder+"/"+file, localOutFolder+"/"+file))
	    	{
	    		//jsx process passed
	    		jsxProcessStatus = true;
	    		
	    		
	    	}
	    	else
	    	{
	    		consoleLog.log("Failed in InDesign style mapping stage.\n");
	    		System.out.println("Failed in InDesign style mapping stage.\n");
	    	}
	    	//testing
	    	//utilities.recurMove(new File(localInFolder+"/"+file),new File(localOutFolder+"/"+file));
	    	
	    	counter.decrementAndGet();
			System.out.println("localOutFolderFile:"+localOutFolder+"/"+file);
	    	System.out.println("jobOutFolder:"+jobOutFolder+"/"+file);
	    	utilities.delete(new File(localInFolder+"/"+file));
	    	//utilities.recurMove(new File(localOutFolder+"/"+file),new File(jobOutFolder+"/"+file));
		//}
		return (jsxProcessStatus);
	}
	 
	//@SuppressWarnings("unchecked")
	 public boolean JavaApplescriptTest(String inFolder,String outFolder) throws IOException
	 {
		boolean appleScriptStatus = false;
		
		System.out.println("inFolder:"+inFolder);
    	System.out.println("outFolder:"+outFolder);
		
		String result = "";
		try 
		{
			String command = "set aScriptPath to \""+jsxFile+"\"\n" +
			"set myParameters to {\""+ inFolder +"\",\"" + outFolder +"\"}\n" +
			"tell application \"Adobe InDesign CC 2018\"\n" +
			"with timeout of 600 seconds\n" +
			"Activate\n" +
			"do script aScriptPath language javascript with arguments myParameters\n"+ 
			"tell script args \n" +
			"set myScriptArgumentA to get value name \"ScriptArgumentB\"\n" +
			"end tell\n" +
			"end timeout\n" +
			"return myScriptArgumentA \n" +
			"end tell\n";
			
			//System.out.println("command:"+command+"\n\n");
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
			        		utilities.recurMove(new File(localOutFolder+"/"+outFolder.substring(outFolder.lastIndexOf("/")+1)),new File(jobOutFolder+"/"+outFolder.substring(outFolder.lastIndexOf("/")+1)));
			        		
			        		appleScriptStatus = true;
			        		mailObj = new mail("artwork@codemantra.co.in","SUCCESS",outFolder.substring(outFolder.lastIndexOf("/")+1),"", "");
			    			Thread mailThread7 = new Thread(mailObj, "Mail Thread for SUCCESS");
			            	mailThread7.start();
			            	
			            	System.out.println("InDesign process finished Successfully.\n");
			            	consoleLog.log("InDesign process finished Successfully.\n");
			        	}
			        	else if(response.equals("404"))
			        	{
			        		mailObj = new mail("artwork@codemantra.co.in","ERROR",outFolder.substring(outFolder.lastIndexOf("/")+1),"", "");
			    			Thread mailThread10 = new Thread(mailObj, "Mail Thread for FAILURE");
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
				System.out.println("No response from InDesign.");
				consoleLog.log("No response from InDesign.");
        		
        		mailObj = new mail("artwork@codemantra.co.in","ERROR",outFolder.substring(outFolder.lastIndexOf("/")+1),"", "");
    			Thread mailThread10 = new Thread(mailObj, "Mail Thread for FAILURE");
            	mailThread10.start();
			}
			//reader.close();
		 } 
		 catch (Exception e) 
		 {
		     //e.printStackTrace();
			consoleLog.log(e.toString());
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
}