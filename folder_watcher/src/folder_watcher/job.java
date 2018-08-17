package folder_watcher;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URLEncoder;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import folder_watcher.Main;

public class job 
{
	private static String json_pretty_print(String json)
	{
		if(!json.isEmpty())
		{
			json = json.replace("{","{\n    ");
			json = json.replace(",",",\n    ");
			json = json.replace("}","\n}\n");
			//System.out.println("JSON:"+json);
			return json;
		}
		else
			return "";
	}
	
	public boolean job_insert(String jobId) throws IOException
	{
		Writer output2 = new BufferedWriter(new FileWriter("jobs.txt", true));  //clears file every time
		output2.append(jobId+ System.getProperty("line.separator"));
		output2.close();
		return true;
	}
	
	public boolean job_update(String jobId)
	{
		Writer output1;
		boolean successful = false;
		try 
		{		
			output1 = new BufferedWriter(new FileWriter("jobs_temp.txt", true));
			output1.close();
			
			File inputFile = new File("jobs.txt");
			File tempFile = new File("jobs_temp.txt");
		
			if(!inputFile.exists())
			{
				job_insert("");
			}
			
			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

			String lineToRemove = jobId;
			String currentLine;

			while((currentLine = reader.readLine()) != null) 
			{
			    // trim newline when comparing with lineToRemove
			    String trimmedLine = currentLine.trim();
			    if(trimmedLine.equals(lineToRemove)) 
			    {
			    	//System.out.println("Job found");
			    	continue;
			    }
			   writer.write(currentLine + System.getProperty("line.separator"));
			}
			writer.close(); 
			reader.close(); 
			successful = tempFile.renameTo(inputFile);
		}
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  //clears file every time
		//output1.append("hello"+ System.getProperty("line.separator"));
		return successful;
	}
	
	public boolean job_status(String jobId)
	{
		boolean successful = false;
		try
		{
			//output1 = new BufferedWriter(new FileWriter("jobs.txt", true));
			//output1.close();
			
			File inputFile = new File("jobs.txt");
			//File tempFile = new File("jobs_temp.txt");
			if(inputFile.exists()) 
    		{ 
    			//System.out.println("file exists");
				BufferedReader reader = new BufferedReader(new FileReader(inputFile));
				//BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
	
				String lineToRemove = jobId;
				String currentLine;
	
				while((currentLine = reader.readLine()) != null) 
				{
				    // trim newline when comparing with lineToRemove
				    String trimmedLine = currentLine.trim();
				    if(trimmedLine.equals(lineToRemove)) 
				    {
				    	//System.out.println("Job in progress");
				    	successful = true;
				    	break;
				    }
				   //writer.write(currentLine + System.getProperty("line.separator"));
				}
				//if(successful == false)
					//System.out.println("No jobs");
				//writer.close(); 
				reader.close();
    		}
			//else
				//System.out.println("file does not exists");
			//tempFile.renameTo(inputFile);
		}
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  //clears file every time
		//output1.append("hello"+ System.getProperty("line.separator"));
		return successful;
	}
	
	 static String[] getDocParam(String jobId_doc, String clientId_doc)
	 {
		 String jobParams[] = {"","","",""};
		 try
		 {
			 String urlParams = "jobId="+URLEncoder.encode(jobId_doc,"UTF-8")+"&clientId="+URLEncoder.encode(clientId_doc,"UTF-8");//+"&chapter="+URLEncoder.encode(temp,"UTF-8")
	 		//local server 172.16.4.112, live - 172.16.1.25
	     	
	     	consoleLog.log("URL:\"http://"+url_request.serverIp+"/maestro/getTemplatePathDetails?"+urlParams + "\",type:\"GET\"\n");
	     	String templateResponse = url_request.urlRequestProcess("http://"+url_request.serverIp+"/maestro/getTemplatePathDetails?"+urlParams,"GET","");
	 		System.out.println("templateResponse:"+json_pretty_print(templateResponse));
	 		consoleLog.log("templateResponse:"+json_pretty_print(templateResponse)+"\n");
	 		
	 		if((templateResponse != null) && (!templateResponse.isEmpty()) && (!templateResponse.equals("")))
			{
		 		JSONParser parser = new JSONParser();
				Object templateObj = parser.parse(templateResponse);
		        JSONObject jo = (JSONObject) templateObj;
			
		        jobParams[0] = (String) jo.get("templateName");
		        jobParams[1] = (String) jo.get("templatePath");
		        jobParams[2] = (String) jo.get("maestroMappingPath");
		        jobParams[3] = (String) jo.get("styleSheetPath");
		        
		        if((jobParams[0] != null) && (!jobParams[0].isEmpty()) && (jobParams[1] != null) && (!jobParams[1].isEmpty()) && (jobParams[2] != null) && (!jobParams[2].isEmpty()) && (jobParams[3] != null) && (!jobParams[3].isEmpty()))
		        {
		        	jobParams[1] = jobParams[1].replace('\\','/');
		        	jobParams[2] = jobParams[2].replace('\\','/');
		        	jobParams[3] = jobParams[3].replace('\\','/');
		        	
		        	jobParams[1] = jobParams[1].substring(jobParams[1].indexOf("//")+2,jobParams[1].length());
		        	jobParams[1] = jobParams[1].substring(jobParams[1].indexOf('/')+1,jobParams[1].length());
		        	jobParams[1] = "/Volumes/"+jobParams[1]; //templatePath
		    		
		        	jobParams[2] = jobParams[2].substring(jobParams[2].indexOf("//")+2,jobParams[2].length());
		        	jobParams[2] = jobParams[2].substring(jobParams[2].indexOf('/')+1,jobParams[2].length());
		        	jobParams[2] = "/Volumes/"+jobParams[2]; //maestro mapping path
		    		
		        	jobParams[3] = jobParams[3].substring(jobParams[3].indexOf("//")+2,jobParams[3].length());
		        	jobParams[3] = jobParams[3].substring(jobParams[3].indexOf('/')+1,jobParams[3].length());
		        	jobParams[3] = "/Volumes/"+jobParams[3]; //styleSheetPath
		    		
//		        	System.out.println("templatePath : "+jobParams[1]);
//		    		System.out.println("maestroMappingPath : "+jobParams[2]);
//		    		System.out.println("styleSheetPath : "+jobParams[3]+"\n");
//		    		
		    		consoleLog.log("templatePath : "+jobParams[1]);
		    		consoleLog.log("maestroMappingPath : "+jobParams[2]);
		    		consoleLog.log("styleSheetPath : "+jobParams[3]+"\n");
		        	
		    		boolean templatePath = utilities.folderCheck(jobParams[1]);
		    		boolean maestroMappingPath = utilities.folderCheck(jobParams[2]);
		    		boolean styleSheetPath = utilities.fileCheck(jobParams[3]);
		    		
		    		System.out.println("fileCheckStatus of templatePath : "+templatePath);
		    		System.out.println("fileCheckStatus of maestroMappingPath : "+maestroMappingPath);
		    		System.out.println("fileCheckStatus of styleSheetPath : "+styleSheetPath+"\n");
		    		
		    		consoleLog.log("fileCheckStatus of templatePath : "+templatePath);
		    		consoleLog.log("fileCheckStatus of maestroMappingPath : "+maestroMappingPath);
		    		consoleLog.log("fileCheckStatus of styleSheetPath : "+styleSheetPath+"\n");
		    		
		    		if(!templatePath && !maestroMappingPath && !styleSheetPath)
		    		{
		    			jobParams[1] = "";
		    			jobParams[2] = "";
		    			jobParams[3] = "";
		    		}
		        }
	 		}
		 }
		 catch(Exception e)
		 {
			 e.printStackTrace();
		 }
		 return jobParams;
	 }
	 
	 static boolean jobFailErrorFun(String jobId, String clientId) throws IOException
	 {
		boolean jobFailError = false; 
		try
		{
			String osResp = utilities.serverMount();
			System.out.println("osResp:" + osResp);
			consoleLog.log("Mount response:" + osResp);
			if (osResp.equals("Disk Found")) 
			{
				Main.mountError = false;
				String jobParams[] = getDocParam(jobId, clientId), errParam = "";
				if(jobParams[1].equals("") || !utilities.folderCheck(jobParams[1]))
				{
					errParam = "\n* Template Path is invalid";
				}
				if(jobParams[2].equals("") || !utilities.folderCheck(jobParams[2])) 
				{
					errParam = (errParam.isEmpty() ? "" : errParam+",\n")  + "* Maestro Map Path is invalid";
				}
				if(jobParams[3].equals("") || !utilities.fileCheck(jobParams[3]))
	    		{
					errParam = (errParam.isEmpty() ? "" : errParam+",\n")  + "* Standard StyleSheetPath is invalid";
	    		}
				
				if(!errParam.isEmpty())
				{
					//System.out.println("Job failed due to StyleSheetPath or Mapping Path or Template Path");
					//consoleLog.log("Job failed due to StyleSheetPath or Mapping Path or Template Path");
					
					jobFailError = true;
					errParam = errParam  + ".\n\n";
					
					//sample : sendMail("CRC Team", "JOB_FAIL", "9781138556850_Ilyas", "", errParam);
					//mail mailObj = new mail(URLEncoder.encode("CRC Team", "UTF-8"), "JOB_FAIL", jobId, "", errParam);
					//mailObj.mailProcess();
					//Thread mailThread4 = new Thread(mailObj, "Mail Thread for CRC Team");
					//mailThread4.start();
					
					//mailObj = new mail(URLEncoder.encode("Pre-editing", "UTF-8"), "JOB_FAIL", jobId, "", errParam);
					//Thread mailThread5 = new Thread(mailObj, "Mail Thread for CRC Team");
					//mailThread5.start();
				}
			}
			else
			{
				if(!Main.mountError)
				{
					// mail to netops
					consoleLog.log("MOUNT ERROR mail sent to group \"netops\"");
					// smaple : sendMail("Net-ops","rajarajan@codemantra.in", "", "MOUNT", "", "");
					//mail m = new mail("Net-ops", "ERROR", "MOUNT", "", "");
					// m.mailProcess("Net-ops", "ERROR", "MOUNT", "", "");
					//Thread mailThread = new Thread(m, "Mail Thread for Template path mount");
					//mailThread.start();
					Main.mountError = true;
				}
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
		
		return jobFailError;
	 }	
}

