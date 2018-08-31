package folder_watcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
//import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.json.simple.parser.ParseException;

public class utilities 
{
	public static String osascript_call(String command)
	{
	    Runtime runtime = Runtime.getRuntime();
	    //String[] args = { "osascript", "-e", "tell application \"Finder\" to activate" };
	    //String[] args = { "osascript", "-e", "say \"Good morning Al.\" using \"Victoria\"" };
	    //tell application "Adobe InDesign CC 2014" to activate
	    /*
	     *  set aScriptPath to "Users:comp:Library:Preferences:Adobe InDesign:Version 10.0:en_US:Scripts:Scripts Panel:testapp.jsx"
			set myParameters to {"9780815364658_Steele", "CRC"}
			tell application "Adobe InDesign CC 2014"
			do script aScriptPath language javascript with arguments myParameters 
			end tell
	     */
	    /*
	     * 	try
				mount volume "smb://rajarajan:Amma2809appa_@172.16.1.2/OEO/"
			end try
			
			try				
				mount volume "smb://172.16.1.2/Copyediting/" as user name "maestroqs@cmpl.in" with password "M@est0123"
			end try
	     */
	    String[] appArgs = { "osascript", "-e", command};
	    String out="";
	    //System.out.println(appArgs[2]);
		try
		{
			Process process = runtime.exec(appArgs);
			//runtime.exec(args);
			BufferedReader stdInput = new BufferedReader(new 
			InputStreamReader(process.getInputStream()));
			
			//BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			
			// read the output from the command
			//System.out.println("Here is the standard output of the command:\n");
			String temp = null;
			while ((temp = stdInput.readLine()) != null) 
			{
			   //System.out.println(temp);
			   out = out + temp;
			}
			//System.out.println("out:"+out);
			// read any errors from the attempted command
			/*System.out.println("Here is the standard error of the command (if any):");
			while ((s = stdError.readLine()) != null) 
			{
			    System.out.println(s);
			}*/
		}
		catch (IOException e)
		{
		  e.printStackTrace();
		}
		return out;
	}
	
	public static String fileRead(String fileName) throws IOException
	{
		 // The name of the file to open.
        

        // This will reference one line at a time
        String line = null,content="";

        // FileReader reads text files in the default encoding.
        FileReader fileReader = new FileReader(fileName);

        // Always wrap FileReader in BufferedReader.
        BufferedReader bufferedReader = new BufferedReader(fileReader);

        while((line = bufferedReader.readLine()) != null) 
        {
            //System.out.println(line);
        	content = content + line;
        }   

        // Always close files.
        bufferedReader.close();
		return content;
	}
	
	public static File[] getExtension(String dirName,final String ext)
	{
	    File dir = new File(dirName);
	    return dir.listFiles(new FilenameFilter() 
	    {
	    	public boolean accept(File dir, String filename)
	    	{
	    		return filename.endsWith(ext);
	    	}
	    });
	}
	
	public static void move(String srcDir,String destDir) throws IOException 
	{
		try 
		{
			File theDir = new File(destDir);//dest//File theDir = new File("/Users/comp/Desktop/test2/");//dest

			// if the directory does not exist, create it
			if (!theDir.exists()) 
			{
				System.out.println("creating directory: " + theDir.getName());
				consoleLog.log("creating directory: " + theDir.getName());
				boolean result = false;
		        theDir.mkdir();
		        result = true; 
		         
			    if(result)
			    {
			        System.out.println("DIR created");
			        consoleLog.log("DIR created");
			    }
			}
		
			File folder = new File(srcDir);//File folder = new File("/Users/comp/Desktop/test1/");
			File[] listOfFiles = folder.listFiles();
			for (int i = 0; i < listOfFiles.length; i++) 
			{
				//move(new File("/Users/comp/Desktop/test2/"+listOfFiles[i].getName()),new File("/Users/comp/Desktop/test1/"+listOfFiles[i].getName()));
				if(!listOfFiles[i].getName().equals("ERROR"))
					recurMove(new File(listOfFiles[i].getPath()),new File(theDir.getPath()+"/"+listOfFiles[i].getName()));
//					if (listOfFiles[i].isFile()) 
//					{
//						//System.out.println("File " + listOfFiles[i].getName());
//						Files.move(Paths.get("/Volumes/Copyediting/02_Training/Team/Raja P/MaestroFolderStructure/STEM titles/9781482298697_Yu/MaestroReady/"+listOfFiles[i].getName()), Paths.get("/Volumes/Copyediting/02_Training/Team/Raja P/MaestroFolderStructure/STEM titles/9781482298697_Yu/MaestroReady/ERROR/"+listOfFiles[i].getName()), StandardCopyOption.REPLACE_EXISTING);
//					}
//					else if (listOfFiles[i].isDirectory())
//					{
//						System.out.println("Directory " + listOfFiles[i].getName());
//						Files.move(Paths.get("/Volumes/Copyediting/02_Training/Team/Raja P/MaestroFolderStructure/STEM titles/9781482298697_Yu/MaestroReady/"+listOfFiles[i].getName()), Paths.get("/Volumes/Copyediting/02_Training/Team/Raja P/MaestroFolderStructure/STEM titles/9781482298697_Yu/MaestroReady/ERROR/"+listOfFiles[i].getName()), StandardCopyOption.REPLACE_EXISTING);
//					}
			}
		}
		catch(SecurityException se)
		{
			 //handle it
			se.printStackTrace();
		}  
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
	
	//to move file from one path to other path
	public static void fileMove(String fromPath,String toPath)  throws IOException
	 {
		 try
		 {
			Path temp = null;
			//if(fileCheck(fromFolder) && fileCheck(toFolder))
			temp = Files.move(Paths.get(fromPath), Paths.get(toPath), StandardCopyOption.REPLACE_EXISTING);
		 
	        if(temp != null)
	        {
	            System.out.println("File moved to "+toPath+" successfully");
	            consoleLog.log("File moved from "+toPath+" successfully");
			}
			else
			{
			    System.out.println("Failed to move the file from "+fromPath+" to "+toPath);
			    consoleLog.log("Failed to move the file from "+fromPath+" to "+toPath);
			}
		 }
		 catch(Exception e)
		 {
			 consoleLog.log(e.toString().substring(0,e.toString().indexOf(":")) + " during file moving "+fromPath+" to "+toPath);
		 }
		 return;
	}
	
	//move folder from one Path to another path
	public static void folderMove(String frmPath,String toPath) throws IOException
	{
		try
		{
			if(folderCheck(frmPath) && folderCheck(toPath))
			{
				File folder = new File(frmPath);
				File[] listOfFiles = folder.listFiles();
				for (int j = 0; j < listOfFiles.length; j++)
				{
					if(!listOfFiles[j].isDirectory())
						Files.move(Paths.get(listOfFiles[j].getPath()), Paths.get(toPath+"/"+listOfFiles[j].getName()), StandardCopyOption.REPLACE_EXISTING);
					else
					{
						System.out.println("1:"+frmPath+"/"+listOfFiles[j].getName());
						System.out.println("1:"+frmPath+"/"+listOfFiles[j].getName());
						folderMove(frmPath+"/"+listOfFiles[j].getName(), toPath+"/"+listOfFiles[j].getName());
					}
				}
			}
		}
		catch(Exception e)
		{
			 consoleLog.log(e.toString().substring(0,e.toString().indexOf(":")) + "during file moving "+frmPath+" to "+toPath);
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
	
	//to check file exists in location
	public static boolean fileCheck(String filePathString) throws IOException,ParseException
	{
		boolean status = false;
		//System.out.println("filePathString:"+filePathString);
		File f = new File(filePathString);
		if(f.exists() && !f.isDirectory()) 
		{ 
			 // do something
			status = true;
			//System.out.println("filePathString:"+filePathString);
		}
//		else if(serverMount().equals("Disk Found"))
//		{
//			//Disk mounted but file not found
//			mail mailObj = new mail();
//			mailObj.mailProcess(URLEncoder.encode("CRC Team", "UTF-8"),"FILE_NOT_EXISTS","FILE","", "1. "+filePathString+"\n");
//		}
//		else
//		{
//			//Disk not mounted
//			try 
//			{
//				consoleLog.log("Volume not mounted and it is detected during fileCheck of "+filePathString+" MOUNT ERROR mail sent to group \"netops\"");
//				mail m = new mail();
//				m.mailProcess("Net-ops", "ERROR", "MOUNT", "", "");
//			} 
//			catch (Exception e) 
//			{
//				// TODO Auto-generated catch block
//				System.out.println("Exception during file check mail and logging");
//				e.printStackTrace();
//			}
//		}
		return status;
	}
	
	//to check folder exists in location
	public static boolean folderCheck(String folderPathString) throws IOException, ParseException
	{
		boolean status = false;
		//System.out.println("filePathString:"+filePathString);
		File f = new File(folderPathString);
		if(f.exists() && f.isDirectory()) 
		{ 
			 //file exists
			status = true;
			//System.out.println("filePathString:"+filePathString);
		}
//		else if(serverMount().equals("Disk Found"))
//		{
//			//Disk mounted but file not found
//			mail mailObj = new mail();
//			mailObj.mailProcess(URLEncoder.encode("CRC Team", "UTF-8"),"DIRECTORY_NOT_EXISTS","DIRECTORY","", "1. "+folderPathString+"\n");
//		}
//		else
//		{
//			//Disk not mounted
//			try 
//			{
//				consoleLog.log("Volume not mounted and it is detected during folderCheck of "+folderPathString+" MOUNT ERROR mail sent to group \"netops\"");
//				mail m = new mail();
//				m.mailProcess("Net-ops", "ERROR", "MOUNT", "", "");
//			}
//			catch (Exception e) 
//			{
//				// TODO Auto-generated catch block
//				System.out.println("Exception during folder check mail and logging");
//				e.printStackTrace();
//			}
//		}
		return status;
	}
	
	//SMB share mount
	public static String serverMount()
	{
		String command = "set mountedDiskName to \"Copyediting\"\n" +
				 "set mountedDiskName1 to \"comp_template\"\n" +
				 "tell application \"System Events\" to set diskNames to name of every disk\n" +
				 "if (mountedDiskName is in diskNames) and (mountedDiskName1 is in diskNames) then\n" +	
				 "\ttry\n" +
				 "\t\tlog \"Disk Found -->\" & mountedDiskName\n" +
				 "\t\treturn \"Disk Found\"\n" +
				 "\ton error\n" +
				 "\t\treturn \"Disk not Found, contact administrator\"\n" +
				 "\tend try\n" + 
				 "else if (mountedDiskName is in diskNames) and not (mountedDiskName1 is in diskNames) then\n" + 
				 "\ttry\n" + 
				 "\t\tmount volume \"smb://172.16.1.21/comp_template\" as user name \"maestroqs@cmpl.in\" with password \"M@est0123\"\n" +
				 "\t\treturn \"Disk Found\"\n" +
				 "\ton error\n" +
				 "\t\treturn \"Disk not Found, contact administrator\"\n" +
				 "\tend try\n" + 
				 "else if not (mountedDiskName is in diskNames) and (mountedDiskName1 is in diskNames) then\n" +
				 "\ttry\n" +
				 "\t\tmount volume \"smb://172.16.1.2/Copyediting\" as user name \"maestroqs@cmpl.in\" with password \"M@est0123\"\n" +
				 "\t\treturn \"Disk Found\"\n" +
				 "\ton error\n" +
				 "return \"Disk not Found, contact administrator\"\n" +
				 "\tend try\n" +
				 "else\n" +
				 "\ttry\n" +
				 "\t\tmount volume \"smb://172.16.1.2/Copyediting\" as user name \"maestroqs@cmpl.in\" with password \"M@est0123\"\n" +
				 "\t\tmount volume \"smb://172.16.1.21/comp_template\" as user name \"maestroqs@cmpl.in\" with password \"M@est0123\"\n" +
				 "\t\tlog \"Disk Found -->\" & mountedDiskName & mountedDiskName1\n" +
				 "\t\treturn \"Disk Found\"\n" +
				 "\ton error\n" +	
				"\t\treturn \"Disk not Found, contact administrator\"\n" +
				"\tend try\n" +
				"end if";
		//System.out.println(command);
		//test = new test4();
		return (osascript_call(command));
	}
	
	public static String mountDisk(String ip, String shareName, String user, String password)
	{
		String command = "set mountedDiskName to \""+shareName+"\"\n"
					+	 "set sharedIp to \""+ip+"\"\n"
					+	 "set userName to \""+user+"\"\n"
					+	 "set passwd to \""+password+"\"\n"
					+	 "tell application \"System Events\" to set diskNames to name of every disk\n"
					+	 "try\n"
					+	 "\tdo shell script \"umount /Volumes/\" & mountedDiskName & \"-*\"\n" 
					+	 "on error\n"
					+	 "\tif (mountedDiskName is in diskNames) then\n"
					+	 "\t\ttry\n"
					+	 "\t\t\tif (do shell script \"/bin/bash -s <<'EOF'\n"
					+	 "ping -c5 -t5 \\\"\" & sharedIp & \"\\\" > /dev/null \n" 
					+	 "[[ $? == 0 ]] && echo \\\"online\\\" || echo \\\"offline\\\" \n" 
					+	 "EOF\") contains \"online\" then\n"
					+	 "\t\t\t\treturn \"Disk Found\"\n"
					+	 "\t\t\tend if\n"
					+	 "\t\ton error\n"
					+	 "\t\t\t\ttell application \"Finder\"\n"
					+	 "\t\t\t\t\teject alias mountedDiskName\n"
					+	 "\t\t\t\tend tell\n"
					+	 "\t\t\t\treturn \"Disk not Found2 -->\" & mountedDiskName\n"
					+	 "\t\tend try\n"
					+	 "\telse\n"
					+	 "\t\ttry\n"
					+	 "\t\t\t\tif (do shell script \"/bin/bash -s <<'EOF'\n"
					+	 "ping -c5 -t5 \\\"\" & sharedIp & \"\\\" > /dev/null \n"
					+	 "[[ $? == 0 ]] && echo \\\"online\\\" || echo \\\"offline\\\" \n" 
					+	 "EOF\") contains \"online\" then\n"
					+	 "\t\t\t\tmount volume \"smb://\" & sharedIp & \"/\" & mountedDiskName as user name userName with password passwd\n"
					+	 "\t\t\t\ttell application \"System Events\" to set diskNames to name of every disk\n"
					+	 "\t\t\t\tif (mountedDiskName is in diskNames) then\n"
					+	 "\t\t\t\t\treturn \"Disk Found\"\n"
					+	 "\t\t\t\tend if\n"
					+	 "\t\t\tend if\n"
					+	 "\t\ton error\n"
					+	 "\t\t\treturn \"Disk not Found -->\" & mountedDiskName\n"
					+	 "\t\tend try\n"
					+	 "\tend if\n"
					+	 "end try";
		
		//System.out.println(command);
		//test = new test4();
		return (osascript_call(command));
	}
	
	static String getFileNameWithoutExtension(File file) 
	{
	    String fileName = "";
        try 
        {
            if (file != null && file.exists()) 
            {
                String name = file.getName();
                fileName = name.replaceFirst("[.][^.]+$", "");
            }
	    }
	    catch (Exception e) 
	    {
	        e.printStackTrace();
	        fileName = "";
	    }
	    return fileName;
	}
	 
	static String getFileExtension(File file) 
	{
	    String fileName = file.getName();
	    //System.out.println("fileName:"+fileName);
	    
	    if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
	    {
	    	//System.out.println("Extn:"+fileName.substring(fileName.lastIndexOf(".")+1));
	    	return fileName.substring(fileName.lastIndexOf(".")+1);
	    }
	    else 
	    	return "";
	}
	
	static String json_pretty_print(String json)
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
