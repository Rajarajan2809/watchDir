package folder_watcher;

//import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import folder_watcher.consoleLog;

public class webServer implements Runnable, HttpHandler 
{
	@Override
	public void run()
	{
		try 
		{
			server();
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("Error:"+e.toString().substring(0, e.toString().indexOf(":")));
			switch (e.toString().substring(0, e.toString().indexOf(":"))) 
			{
				case "java.net.BindException":
				{
					System.out.println("Another instance of \"MaestroQS\" is already running, Please close these instances, before starting this service.");
					try 
					{
						consoleLog.log("Another instance of \"MaestroQS\" is already running, Please close these instances, before starting this service.");
					} 
					catch (IOException e1) 
					{
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					System.exit(0);
				}
				break;
			}
		} 
		catch (InterruptedException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			//System.exit(0);
		}
	}
	
	public static void server() throws IOException, InterruptedException
	{
		 HttpServer server = HttpServer.create(new InetSocketAddress(9000), 0);
		    server.createContext("/maestro", new MyHandler());
		    server.setExecutor(null); // creates a default executor
		    server.start();
	}

	@Override
	public void handle(HttpExchange arg0) throws IOException {
		// TODO Auto-generated method stub
		
	}
}

class MyHandler implements HttpHandler 
{
	@SuppressWarnings("unchecked")
    public void handle(HttpExchange t) throws IOException 
    {
    	JSONObject obj = new JSONObject();
		JSONArray jobs = new JSONArray();
		obj.toJSONString();
		List<String> lines = Files.readAllLines(Paths.get("jobs.txt"));
		
		Iterator<String> iterator = lines.iterator();
		while (iterator.hasNext()) 
		{
			//System.out.println(iterator.next());
			jobs.add(iterator.next());
		}
		obj.put("status", "Server is online");
		obj.put("jobs", jobs);
		
		//System.out.print(obj.toJSONString());
		byte [] response = obj.toJSONString().getBytes();
		t.sendResponseHeaders(200, response.length);
		OutputStream os = t.getResponseBody();
		os.write(response);
		os.close();
    }
  }