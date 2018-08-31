package folder_watcher;

import java.util.concurrent.TimeUnit;

import folder_watcher.utilities;

public class mount_thread implements Runnable
{
	public void run()
	{
		try 
		{
			TimeUnit.SECONDS.sleep(50);
			String command = "try\n" + 
				"	do shell script \"killall NetAuthAgent\"\n" + 
				"on error\n" + 
				"	return\n" + 
				"end try";
			utilities.osascript_call(command);
		}
		catch (InterruptedException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
