package watchDirRec;

import java.nio.file.*;
import static java.nio.file.StandardWatchEventKinds.*;
import static java.nio.file.LinkOption.*;
import java.nio.file.attribute.*;
import java.io.*;
import java.util.*;

import org.apache.commons.io.FilenameUtils;

import watchDirRec.consoleLog;


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
            registerAll(dir);
            System.out.println("Done.");
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
	                String ext1 = FilenameUtils.getExtension(child.toString());
	                //System.out.println("extension:"+ext1);
	                
	                // print out event
	                if(kind.equals(ENTRY_CREATE) && ext1.equals("rtf") && createdFile.isFile() && (createdFile.getParentFile().getName().equals("Process")))
	                {
	                	//System.out.format("%s: %s\n", event.kind().name(), child);
	                	//File createdFile = child.toFile();
	                	System.out.println("extension:"+ext1);
	                	vbScriptCall(child.toString());
	                }
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
    
    private boolean vbScriptCall(String path) throws IOException, InterruptedException
    {
    	 
    	 try 
    	 {
    		Path outputPathRtf = Paths.get(path.replace("Process", "Output"));
    		
    		String inputPathXml = path;
    		//inputPathXml = path.replace("Process", "Output");
    		inputPathXml = path.replace("rtf", "xml");
    		//pathXml = pathXml.replace("rtf", "xml");
    		
    		Path outputPathXml = Paths.get(inputPathXml.replace("Process", "Output"));
    		Path errPath = Paths.get(path.replace("Process", "Error"));
			
    		System.out.println("input rtf Path:"+path);
    		System.out.println("output rtf Path:"+outputPathRtf+"\n");
    		
    		System.out.println("Error Path:"+errPath+"\n");
    		
    		System.out.println("input xml path:"+inputPathXml);
    		System.out.println("output xml path:"+outputPathXml);
    		
    		File file = new File("macro_call.vbs"); 
    		//file = File.createNewFile();
			file.deleteOnExit();
			FileWriter fw = new java.io.FileWriter(file);
			
			String vbs = "Dim Word \n"
			+ "Dim WordDoc \n"
			+ "Set Word = CreateObject(\"Word.Application\") \n"
			+ "' Make Word visible \n"
			+ "'Open the Document \n"
			+ "Set WordDoc = Word.Documents.open(\"" + path + "\") \n"
			+ "'Run the macro called foo \n"
			+ "Word.Run \"preprocess_cleanup\" \n"
			+ "' Close Word \n"
			+ "Word.Quit \n"
			+ "'Release the object variables \n"
			+ "Set WordDoc = Nothing \n"
			+ "Set Word = Nothing \n";
			
			fw.write(vbs);
			fw.close();
			
			//System.out.println("Macro path:"+file);
			
			Process p = Runtime.getRuntime().exec("wscript " + file.getPath());
			p.waitFor();
			//System.out.println("p.exitValue():"+p.exitValue());
			if(p.exitValue() == 0)
			{
				System.out.println("Macro Success\n\n");
				Files.move(Paths.get(path),outputPathRtf,StandardCopyOption.REPLACE_EXISTING);
				Files.move(Paths.get(inputPathXml),outputPathXml,StandardCopyOption.REPLACE_EXISTING);
				return (true);
			}
			else //if(p.exitValue() == -1)
			{
				System.out.println("Macro Failure");
				Files.move(Paths.get(path),errPath,StandardCopyOption.REPLACE_EXISTING);
				return (false);
			}
    	 }
    	 catch (Exception e) 
    	 {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
    	 }
    	 return false;
        
    }

    private void initProcessing(String pathString) throws FileNotFoundException, IOException, InterruptedException
    {
    	try
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
					System.out.println("File:"+listOfFiles[i].getParent().indexOf("Process"));
					if (listOfFiles[i].isFile() && (listOfFiles[i].getParent().indexOf("Process") != -1)) 
					{
						System.out.println("Extension:"+FilenameUtils.getExtension(listOfFiles[i].toString()));
						System.out.println("File/Folder Status:"+listOfFiles[i].isFile());
						
						if(FilenameUtils.isExtension(listOfFiles[i].getName(),"rtf") || FilenameUtils.isExtension(listOfFiles[i].getName(),"doc") || FilenameUtils.isExtension(listOfFiles[i].getName(),"docx"))
						{
							vbScriptCall(listOfFiles[i].toString());
						}
					}
					else if (listOfFiles[i].isDirectory()) 
					{
						//Move manuscripts to error folder
						System.out.println("Found:"+listOfFiles[i].getName());
						initProcessing(listOfFiles[i].toString());
//						if(listOfFiles[i].getName().equals("Process"))
//			        	{
//							System.out.println("Found:"+listOfFiles[i].getName());
//							initProcessing(listOfFiles[i].toString());
//			        	}
						//xlsx and docx files does not match regex
					}
				}
			}
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    }
    
	public static void main(String[] args) throws IOException 
	{
        // parse arguments
        // register directory and process its events
		try
		{
			//System.out.println(System.getProperty ("user.home")+"/Desktop/PDF2XML/");
			
			if(new File(System.getProperty ("user.home")+"/Desktop/PDF2XML/").exists())
			{
				Path dir = Paths.get(System.getProperty ("user.home")+"/Desktop/PDF2XML/");
				new watchDirRec(dir).processEvents();
			}
			else
				System.out.println("Folder does not exists");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
    }
}
