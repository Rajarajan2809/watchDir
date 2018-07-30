package watchDirRec;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
//import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.io.Writer;
import java.util.Date;

public class consoleLog 
{
	public static boolean log(final String object) throws IOException
	{
		try
		{
			DateFormat dateFormat2 = new SimpleDateFormat("dd-MMM-yy HH:mm:ss");
			String dateString2 = dateFormat2.format(new Date()).toString();
			//System.out.println("Job started at : "+dateString2);
			
			File theDir = new File("Log");//Users/comp/Library/Logs
	    	if (!theDir.exists()) 
			{
	    		theDir.mkdir();
			}
			Calendar cal = Calendar.getInstance();
	    	String fileName = theDir.getAbsolutePath()+"/FW_ABBY_"+new SimpleDateFormat("MMM").format(cal.getTime()) + new SimpleDateFormat("YY").format(cal.getTime())+".log";
			//System.out.println("fileName:"+fileName);
	    	Writer output2 = new BufferedWriter(new FileWriter(fileName, true));  //clears file every time
			output2.append(dateString2 + " " + object + System.getProperty("line.separator"));
			output2.close();
			return true;
		}
		catch(Exception e)
		{
			System.out.println("Exception occurrd during logging");
			e.printStackTrace();
		}
		return false;
	}
	
//	public static void main(String[] args) throws IOException, InterruptedException 
//	{
//		//method 1
//		//DateFormat dateFormat2 = new SimpleDateFormat("dd-MMM-yy hh:mm:ss aa");
//    	//String dateString2 = dateFormat2.format(new Date()).toString();
//    	//System.out.println("Job started at : "+dateString2);
//    	//line_insert("Job started at : "+dateString2);
//		
//		try 
//	    {
//	    	// This block configure the logger with handler and formatter
//	    	
//	        //fh = new FileHandler(System.getProperty ("user.home")+"/Library/Logs/Maestro_QS/"+ fileName +".log");
//	    	line_insert("hello1");
//	    	line_insert("hello2");
//	    	line_insert("hello3");
//	    	line_insert("hello4");
//	    	line_insert("hello5");
//	    	
//	    }
//		catch (IOException e) 
//    	{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
}
