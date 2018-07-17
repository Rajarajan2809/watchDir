package folder_watcher;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class fileWatch 
{
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
	
//	public static void main(String[] args) throws IOException 
//	{
//		// TODO Auto-generated method stub
//		
//		try
//		{
////			output2 = new BufferedWriter(new FileWriter("tasks1.txt", true));  //clears file every time
////			output2.append("hello1"+ System.getProperty("line.separator"));
////			output2.close();
//			job_insert("9781482298697_Yu");
//			job_update("9781482298697_Yu");
//			System.out.println(job_status("9781482298697_Yu"));
//		}
//		catch(Exception e)
//		{
//			e.printStackTrace();
//		}
//	}
	
}

