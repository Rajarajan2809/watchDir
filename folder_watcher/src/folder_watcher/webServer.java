package folder_watcher;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

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
			e.printStackTrace();
		} 
		catch (InterruptedException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
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
    public void handle(HttpExchange t) throws IOException 
    {
      byte [] response = "{\"status\" : \"Server is online.\" }".getBytes();
      t.sendResponseHeaders(200, response.length);
      OutputStream os = t.getResponseBody();
      os.write(response);
      os.close();
    }
  }