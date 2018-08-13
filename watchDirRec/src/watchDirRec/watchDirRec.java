package watchDirRec;

import java.nio.file.*;
import static java.nio.file.StandardWatchEventKinds.*;
import static java.nio.file.LinkOption.*;
import java.nio.file.attribute.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import static java.nio.file.StandardCopyOption.*;
//import java.time.Duration;
//import java.time.Instant;

import org.apache.commons.io.FileUtils;
//apache include
import org.apache.commons.io.FilenameUtils;

//https://docs.oracle.com/javase/tutorial/essential/io/examples/WatchDir.java
//https://docs.oracle.com/javase/tutorial/essential/io/notification.html
//import watchDirRec.consoleLog;


public class watchDirRec
{
	//class variables
    private final WatchService watcher;
    private final Map<WatchKey,Path> keys;
    private final boolean recursive;
    private boolean trace = false;
    
    private Path dir;
    
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
                consoleLog.log("register: "+dir+"\n");
            } 
            else 
            {
                if (!dir.equals(prev)) 
                {
                    System.out.format("update: %s -> %s\n", prev, dir);
                    consoleLog.log("update: "+prev+" -> "+dir+"\n");
                }
            }
        }
        keys.put(key, dir);
    }

    /**
     * Register the given directory, and all its sub-directories, with the
     * WatchService.
     */
    private void registerAll(final Path start) throws IOException 
    {
        // register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() 
        {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                throws IOException
            {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Creates a WatchService and registers the given directory
     * @throws InterruptedException 
     */
    watchDirRec(Path dir1) throws IOException, InterruptedException 
    {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<WatchKey,Path>();
        //default recursive
        this.recursive = true;
        
        this.dir = dir1;
        
        initProcessing(this.dir.toString());
        
        if (recursive) 
        {
            System.out.format("Scanning %s ...\n", dir);
            consoleLog.log("Scanning "+dir+" ...\n");
            registerAll(dir);
            System.out.println("Done.\n\n");
            consoleLog.log("Done.\n\n");
        } 
        else 
        {
            register(dir);
        }

        // enable trace after initial registration
        this.trace = true;
    }

    /**
     * Process all events for keys queued to the watcher
     */
    @SuppressWarnings("rawtypes")
	void processEvents() 
    {
	     try
	     {
	    	for (;;) 
	        {
    		    // wait for key to be signalled
	            WatchKey key;
	            try 
	            {
	                key = watcher.take();
	            } 
	            catch (InterruptedException x) 
	            {
	                return;
	            }
	
	            Path dir = keys.get(key);
	            if (dir == null) 
	            {
	                System.err.println("WatchKey not recognized!!");
	                consoleLog.log("WatchKey not recognized!!");
	                continue;
	            }
	
	            for (WatchEvent<?> event: key.pollEvents()) 
	            {
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
	
	                File createdFile = child.toFile();
	                //System.out.println("extension:"+ext1);
	                
	                Collection<String> extn = new ArrayList<String>();
    				extn.add("rtf");
    				extn.add("doc");
    				extn.add("docx");
	                
	                // print out event
    				if((createdFile.isFile()) && (createdFile.getName().indexOf("~") == -1))
    				{
		                if(kind.equals(ENTRY_CREATE) && (createdFile.getParentFile().getName().equals("_process")))
		                {
		                	if(FilenameUtils.isExtension(createdFile.getName(),extn))
							{
		                		while(!isCompletelyWritten(child.toFile()))
	    						{}
								DateFormat dateFormat2 = new SimpleDateFormat("dd-MMM-yy HH:mm:ss");
								String dateString2 = dateFormat2.format(new Date()).toString();
								System.out.println("File("+createdFile.getName()+") created at : "+dateString2);
								docProcessing(child.toString());
							}
							else
							{
								while(!isCompletelyWritten(child.toFile()))
	    						{}
								System.out.println("File("+createdFile.getName()+") is Invalid and moved to \"_log\" folder.");
								String errPath = createdFile.toString().replace("_process","_log"); 
								Files.move(createdFile.toPath(),Paths.get(errPath),REPLACE_EXISTING);
							}
			            }
    				}
    				else if((createdFile.isDirectory()) && (createdFile.getName().indexOf("~") == -1))
    				{
    					if(kind.equals(ENTRY_CREATE) && (createdFile.getParentFile().getName().equals("_process")))
		                {
    						while(!isCompletelyWritten(createdFile))
    						{}
    						Files.delete(createdFile.toPath());
		                }
    				}
//    				else
//    				{
//    					Files.delete(createdFile);
//    				}
    				// if directory is created, and watching recursively, then
	                // register it and its sub-directories
	                if (recursive && (kind == ENTRY_CREATE))
	                {
	                    try 
	                    {
	                        if (Files.isDirectory(child, NOFOLLOW_LINKS)) 
	                        {
	                            registerAll(child);
	                        }
	                    }
	                    catch (IOException x) 
	                    {
	                        // ignore to keep sample readbale
	                    }
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
    	}
        catch (IOException | InterruptedException e) 
        {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    private void docProcessing(String path) throws IOException, InterruptedException
    {
    	 
    	 try 
    	 {
    		if((path.indexOf("~") == -1) && (new File(path).exists()))
    		{
    			String outputPath,inputPathXml,outputPathXml,errPath,extn;
    			outputPath = path.replace("PDF2XML_Process", "PDF2XML");
    			outputPath = outputPath.replace("_process", "OUT");
				
    			extn = FilenameUtils.getExtension(path);
    			
				//inputPathXml = path;
				//inputPathXml = path.replace("Process", "Output");
				inputPathXml = path.replace(extn, "xml");
				//pathXml = pathXml.replace("rtf", "xml");
				
				outputPathXml = outputPath.replace(extn, "xml");
				
				//Path outputPathXml = Paths.get();
				errPath = path.replace("_process", "_log");
				
				System.out.format("Processing file: "+path+"\n");
	        	//consoleLog.log("Processing file: "+path+"\n");
				//System.out.println("input rtf Path:"+path);
				//System.out.println("output rtf Path:"+outputPathRtf);
				//System.out.println("Error Path:"+errPath);
				//System.out.println("input xml path:"+inputPathXml);
				//System.out.println("output xml path:"+outputPathXml);
				
				/*File theDir = new File(outputPathRtf).getParentFile();
				System.out.println("outputPathRtf:"+theDir.exists());
		    	while (!theDir.exists()) 
				{
		    		theDir.mkdir();
		    		TimeUnit.SECONDS.sleep(1);
				}
				
		    	theDir = new File(errPath).getParentFile();
				System.out.println("errPath:"+theDir.exists());
		    	while (!theDir.exists()) 
				{
		    		theDir.mkdir();
		    		TimeUnit.SECONDS.sleep(1);
				}
		    	
		    	theDir = new File(inputPathXml).getParentFile();
				System.out.println("inputPathXml:"+theDir.exists());
		    	while (!theDir.exists()) 
				{
		    		theDir.mkdir();
		    		TimeUnit.SECONDS.sleep(1);
				}
		    	
		    	theDir = new File(outputPathXml).getParentFile();
				System.out.println("outputPathXml:"+theDir.exists());
		    	while (!theDir.exists()) 
				{
		    		theDir.mkdir();
		    		TimeUnit.SECONDS.sleep(1);
				}*/
		    	
				consoleLog.log("input rtf Path:"+path);
				consoleLog.log("output rtf Path:"+outputPath);
				consoleLog.log("Error Path:"+errPath);
				consoleLog.log("input xml path:"+inputPathXml);
				consoleLog.log("output xml path:"+outputPathXml);
	    		
				if(new File(path).isFile())
	    		{
	    			String ext1 = FilenameUtils.getExtension(path);
	    			if(ext1.equals("rtf") || ext1.equals("doc") || ext1.equals("docx"))
	                {
						Runtime.getRuntime().exec("taskkill /F /IM WINWORD.EXE");
						Runtime.getRuntime().exec("taskkill /F /IM wscript.exe");
						
						File file = new File(System.getProperty ("user.home")+"/macro_call.vbs"); 
						//file = File.createNewFile();
						//file.deleteOnExit();
						FileWriter fw = new java.io.FileWriter(file);
						
						String vbs = "Dim Word "
						+ System.getProperty("line.separator") + "Dim WordDoc "
						+ System.getProperty("line.separator") + "Set Word = CreateObject(\"Word.Application\") "
						+ System.getProperty("line.separator") + "' Make Word visible "
						+ System.getProperty("line.separator") + "Word.Visible = True "
						+ System.getProperty("line.separator") + "'Open the Document "
						+ System.getProperty("line.separator") + "Set WordDoc = Word.Documents.open(\"" + path + "\") "
						+ System.getProperty("line.separator") + "'Run the macro called foo "
						+ System.getProperty("line.separator") + "Word.Run \"preprocess_cleanup\" "
						+ System.getProperty("line.separator") + "' Close Word "
						+ System.getProperty("line.separator") + "Word.Quit "
						+ System.getProperty("line.separator") + "'Release the object variables "
						+ System.getProperty("line.separator") + "Set WordDoc = Nothing "
						+ System.getProperty("line.separator") + "Set Word = Nothing "+ System.getProperty("line.separator") ;
						
						fw.write(vbs);
						fw.close();
						
						TimeUnit.SECONDS.sleep(2);
						Runtime.getRuntime().exec("attrib +H macro_call.vbs");
						
						//System.out.println("Macro path:"+file);
						//System.out.println("vbs content:"+vbs);
						//TimeUnit.SECONDS.sleep(1);
						
						Process p = Runtime.getRuntime().exec("wscript " + file.getPath());
						p.waitFor();
						
						DateFormat dateFormat2 = new SimpleDateFormat("dd-MMM-yy HH:mm:ss");
    	    			String dateString2 = dateFormat2.format(new Date()).toString();
						
    	    			//String othFolder1 = System.getProperty ("user.home")+"/Desktop/PDF2XML/01_test/out/"+ new File(path).getName(); 
    	    			//String othFolder2 = System.getProperty ("user.home")+"/Desktop/PDF2XML/01_test/out/"+ new File(outputPathXml).getName();
    	    			
    	    			TimeUnit.SECONDS.sleep(3);
    	    			
						System.out.println("p.exitValue():"+p.exitValue());
						System.out.println("XML status:"+new File(inputPathXml).exists());
						if((p.exitValue() == -1) || !(new File(inputPathXml).exists()))
						{
							if(new File(path).exists())
							{
								System.out.println("File : "+FilenameUtils.getName(path)+",\nMacro Status : FAIL,\n"+"Finished time : "+dateString2+"\n\n");
								consoleLog.log("File : "+FilenameUtils.getName(path)+",\nMacro Status : FAIL,\n"+"Finished time : "+dateString2+"\n\n");
								Files.move(Paths.get(path),Paths.get(errPath),StandardCopyOption.REPLACE_EXISTING);
							}
							//file.delete();
							//return (false);
						}
						else
						{
							if(new File(inputPathXml).exists() && new File(path).exists())
							{
								System.out.println("File : "+FilenameUtils.getName(path)+",\nMacro Status : SUCCESS,\n"+"Finished time : "+dateString2+"\n\n");
								consoleLog.log("File : "+FilenameUtils.getName(path)+",\nMacro Status : SUCCESS,\n"+"Finished time : "+dateString2+"\n\n");
								Files.move(Paths.get(path),Paths.get(outputPath),StandardCopyOption.REPLACE_EXISTING);
								Files.move(Paths.get(inputPathXml),Paths.get(outputPathXml),StandardCopyOption.REPLACE_EXISTING);
							}
							//Files.copy(Paths.get(path),Paths.get(outputPathRtf),StandardCopyOption.REPLACE_EXISTING);
							//Files.copy(Paths.get(inputPathXml),Paths.get(outputPathXml),StandardCopyOption.REPLACE_EXISTING);
							//Files.delete(Paths.get(path));
							//Files.delete(Paths.get(inputPathXml));
							//file.delete();
							//return (true);
						}
						file.delete();
						Runtime.getRuntime().exec("taskkill /F /IM WINWORD.EXE");
						Runtime.getRuntime().exec("taskkill /F /IM wscript.exe");
						//Runtime.getRuntime().exec("cacls "+othFolder1+" -e -p  ");
						//Runtime.getRuntime().exec("attrib -a -s -h -r "+othFolder2);
	                }
	    			else
	    			{
	    				System.out.println("File is invalid, moved to \""+errPath+"\n\n");
						consoleLog.log("Macro processing finished with Error\n\n");
	    				Files.move(Paths.get(path),Paths.get(errPath),StandardCopyOption.REPLACE_EXISTING);
	    			}
	    		}
	    		else
	    		{
	    			System.out.println("Folder is invalid, moved to \""+errPath+"\n\n");
	    			consoleLog.log("Invalid folder \""+path+"\" moved to \""+errPath+"\n\n");
	    			recurMove(new File(path),new File(errPath));
	    		}
    		}
    		//return;
		 }
    	 catch (Exception e) 
    	 {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
    	 }
    	 return;
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
    
    public void setPermission(File file) throws IOException
    {
        Set<PosixFilePermission> perms = new HashSet<>();
        perms.add(PosixFilePermission.OWNER_READ);
        perms.add(PosixFilePermission.OWNER_WRITE);
        perms.add(PosixFilePermission.OWNER_EXECUTE);

        perms.add(PosixFilePermission.OTHERS_READ);
        perms.add(PosixFilePermission.OTHERS_WRITE);
        perms.add(PosixFilePermission.OTHERS_EXECUTE);

        perms.add(PosixFilePermission.GROUP_READ);
        perms.add(PosixFilePermission.GROUP_WRITE);
        perms.add(PosixFilePermission.GROUP_EXECUTE);

        Files.setPosixFilePermissions(file.toPath(), perms);
    }
    
    private void initProcessing(String pathString) throws FileNotFoundException, IOException, InterruptedException
    {
    	try
    	{
	    	File folder = new File(pathString);
	        File[] listOfFiles = folder.listFiles();
			if(folder.exists())
			{
				for (int i = 0; i < listOfFiles.length; i++) 
				{
					Collection<String> extn = new ArrayList<String>();
    				extn.add("rtf");
    				extn.add("doc");
    				extn.add("docx");
					if (listOfFiles[i].isFile() && (listOfFiles[i].getParent().indexOf("_process") != -1)) 
					{
						if(FilenameUtils.isExtension(listOfFiles[i].getName(),extn))
						{
							while(!isCompletelyWritten(listOfFiles[i]))
    						{}
							docProcessing(listOfFiles[i].toString());
						}
						else
						{
							while(!isCompletelyWritten(listOfFiles[i]))
    						{}
							//String errPath = listOfFiles[i].toString().replace("_process","_log");
							//testPath = errPath;
//							while(!new File(testPath).getParentFile().exists())
//							{
//								File theDir = new File(testPath).getParentFile();
//								System.out.println("errPath:"+theDir.exists());
//								System.out.println("nextPath:"+testPath);
//						    	while (!theDir.exists()) 
//								{
//						    		theDir.mkdir();
//						    		TimeUnit.SECONDS.sleep(1);
//								}
//						    	testPath = testPath.substring(0,testPath.lastIndexOf("\\"));
//							}
							//Files.move(listOfFiles[i].toPath(),Paths.get(errPath),REPLACE_EXISTING);
							Files.delete(listOfFiles[i].toPath());
						}
					}
					else if (listOfFiles[i].isDirectory()) 
					{
						initProcessing(listOfFiles[i].toString());
					}
				}
			}
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    }
    
    public static boolean recurMove(File sourceFile, File destFile)
	{
	    if (sourceFile.isDirectory())
	    {
	    	//File theDir = new File(pathString+"/INVALID_FILES/");
        	
        	if (!destFile.exists()) 
			{
        		destFile.mkdir();
			}
	        for (File file : sourceFile.listFiles())
	        {
	        	//System.out.println("dest1:"+destFile.getPath() + "/" + file.getName()+"\n");
	        	//System.out.println("dest2:"+file.getPath() );
	        	//file.
	        	recurMove(file, new File(destFile.getPath() + "/" + file.getName()));
	        }
	        sourceFile.delete();
	    }
	    else
	    {
	        try 
	        {
	        	//SourceFile.getPath0)
	        	//System.out.println("sourceFile.toString():"+sourceFile.toString()+"\n");
	        	//System.out.println("destFile.toString():"+destFile.toString()+"\n");
	            Path temp = Files.move(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
	            
	            if(temp != null)
	            {
	                System.out.println("File renamed and moved successfully");
	            }
	            else
	            {
	                System.out.println("Failed to move the file");
	            }
	            return true;
	        } 
	        catch (IOException e) 
	        {
	            return false;
	        }
	    }
	    return false;
	}
    
    public static void delete(File file)throws IOException
	{ 
	    if(file.isDirectory())
	    {
	    	//directory is empty, then delete it
	    	if(file.list().length==0)
	    	{
	    		file.delete();
	    		//System.out.println("Directory is deleted : "+ file.getAbsolutePath());
	    	}
	    	else
	    	{
	    		//list all the directory contents
        	   String files[] = file.list();
        	   for (String temp : files) 
        	   {
        	      //construct the file structure
        	      File fileDelete = new File(file, temp);
        		  //recursive delete
        	      delete(fileDelete);
        	   }
        	   //check the directory again, if empty then delete it
        	   if(file.list().length==0)
        	   {
        		   file.delete();
        		   //System.out.println("Directory is deleted : "+ file.getAbsolutePath());
        	   }
	    	}
	    }
	    else
	    {
    		//if file, then delete it
    		file.delete();
    		//System.out.println("File is deleted : " + file.getAbsolutePath());
    	}
	}
    
	public static void main(String[] args) throws IOException 
	{
        // parse arguments
        // register directory and process its events
		try
		{
			//System.out.println(System.getProperty ("user.home")+"/Desktop/PDF2XML/");
			DateFormat dateFormat2 = new SimpleDateFormat("dd-MMM-yy hh:mm:ss aa");
			String dateString2 = dateFormat2.format(new Date()).toString();
			consoleLog.log("--------------------------------------------------------");
			System.out.println("Watch Service started at " + dateString2 + "...\n");
			consoleLog.log("Watch Service started at " + dateString2 + "...\n");
			
			if(new File("D:/PDF2XML_Process/").exists())//System.getProperty ("user.home")+"/Desktop/PDF2XML_Process/").exists())
			{
				Path dir = Paths.get("D:/PDF2XML_Process/");
				new watchDirRec(dir).processEvents();
			}
			else
			{
				System.out.println("D:/PDF2XML_Process/ does not exists");
				consoleLog.log("D:/PDF2XML_Process/ does not exists");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
    }
}
