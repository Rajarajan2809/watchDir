package folder_watcher;

import java.net.*;

//import org.json.simple.JSONObject;
//import org.json.simple.JSONValue;

import java.io.*;


public class url_request
{
	//private String urlQuery,type,content;

	static String serverIp = "172.16.4.112:8088";//testing server
	//static String serverIp = "172.16.1.25:8080";//production server
	//static String serverIp = "172.16.1.55";//testing server2
	
	url_request()
    {
//		this.urlQuery = urlQuery;
//		this.type = type;
//		this.content = content;
		//System.out.println("URL:"+urlQuery);
		//System.out.println("JSON:"+content);
    }
	
//	public String getUrlRequest() throws IOException, ConnectException, FileNotFoundException
//	{
//		try
//		{
//			URL urlReq = new URL(urlQuery);
//			System.out.println("URL:"+urlQuery);
//	        URLConnection connParam = null;
//	        connParam = urlReq.openConnection();
//	        //connParam.setRequestMethod(t);
//	        connParam.setDoOutput(true);
//	        
//	        BufferedReader in = new BufferedReader(new InputStreamReader(connParam.getInputStream()));
//	        
//	        String temp,webCurResp= "";
//	        
//	        while ((temp = in.readLine()) != null)
//	        {
//	        	webCurResp = webCurResp +"\n"+ temp;
//	        }
//	        
//	        //System.out.println(webCurResp);
//	    	in.close();
//	    	return webCurResp;
//			
//			//String url = "http://www.google.com/search?q=mkyong";
//		
//		URL obj = new URL(urlQuery);
//		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
//
//		// optional default is GET
//		con.setRequestMethod("GET");
//
//		//add request header
//		con.setRequestProperty("User-Agent", "Mozilla/5.0");
//
//		int responseCode = con.getResponseCode();
//		System.out.println("\nSending 'GET' request to URL : " + urlQuery);
//		System.out.println("Response Code : " + responseCode);
//
//		BufferedReader in = new BufferedReader(
//		        new InputStreamReader(con.getInputStream()));
//		String inputLine;
//		StringBuffer response = new StringBuffer();
//
//		while ((inputLine = in.readLine()) != null) 
//		{
//			response.append(inputLine);
//		}
//		in.close();
//		return response.toString();
//		}
//		catch(Exception e)
//		{
//			e.printStackTrace();
//		}
//		//print result
//		//System.out.println(response.toString());
//		return "";
//	}
	
	public static String urlRequestProcess(String urlQuery,String type, String content) throws IOException, ConnectException, FileNotFoundException
	{
		//http://172.16.4.112:8080/maestro/updateStatus?jobId=9781482298697_Yu&clientId=TF_CRC&chapterName=9781482298697_Yu_CH01
		//Random random = new Random();
		try
		{
			URL url = new URL(urlQuery);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			
			connection.setConnectTimeout(5000);
			
			if(!content.isEmpty() && type.equals("GET"))
			{
				// optional default is GET
				connection.setRequestMethod("GET");
			}
			else if(type.equals("PUT"))
			{
				connection.setRequestMethod(type);
				connection.setDoOutput(true);
				connection.setRequestProperty("Content-Type", "application/json");
				connection.setRequestProperty("Accept", "application/json");
				OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream());
				osw.write(content);
				osw.flush();
				osw.close();
			}
			//System.out.println(content);
			//System.out.println("URL response code:"+connection.getResponseCode());
			
			 String temp,webCurResp= "";
			if(connection.getResponseCode() == HttpURLConnection.HTTP_OK)
			{
			    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			    while ((temp = in.readLine()) != null)
			    {
			    	webCurResp = webCurResp +"\n"+ temp;
			    }
				in.close();
				//System.out.println(webCurResp);
			}
			//consoleLog.log("response:"+webCurResp);
			return webCurResp;
		}
		catch (Exception e) 
		{
			//e.printStackTrace();
			switch(e.toString().substring(0,e.toString().indexOf(":")))
			{
				case "java.nio.file.NoSuchFileException":
				{
					//System.out.println("(Polling Process) Directory does not exists");
					//consoleLog.log("(Polling Process) Exception:Directory to scan does not exists in SMB Server.");
				}
				break;
				
				case "java.net.SocketException":
				{
					//System.out.println("(Polling Process) connection refuse error");
					//consoleLog.log("(Polling Process) Exception:Could not able to connect to API Server.");
				}
				break;
				
				case "java.net.ConnectException":
				{
					//consoleLog.log("(Polling Process url) connection refuse error");
					//System.out.println("(Polling Process url) Exception:Could not able to connect to API Server.");
					
					//if(mailTriggNet)
					//{
						//utilities U = new utilities();
//						String mailIdJson = utilities.fileRead("maestroqs_support.json");
//						JSONParser parser = new JSONParser();
//						Object preEditObj = parser.parse(mailIdJson);
//				        JSONObject jo = (JSONObject) preEditObj;
//		    		    String mailIds = (String) jo.get("mail_id");
//		    		    
//		    		    mail mailObj = new mail();
//		    		    ArrayList<String> mail_id = mail.mailIdParse(mailIds);
//						for(int i=0; i < mail_id.size();i++)
//						{
//							mailObj.sendMail("Net-ops",mail_id.get(i),"ERROR","DB","","");
//						}
						//mailTriggNet = false;
					//}
					//mailObj.mailProcess("Net-ops","ERROR","DB","");
				}
				break;
				
				case "java.lang.IndexOutOfBoundsException":
				{
					//System.out.println("(Polling Process) Error in sending mail.");
					//consoleLog.log("(Polling Process) Error in sending mail.");
				}
				break;
				
				case "java.lang.NullException":
				{
					//System.out.println("(Polling Process) Exception: sending mail.");
					//consoleLog.log("(Polling Process) Exception: sending mail.");
				}
				break;
				
				case "org.json.simple.parser.ParseException":
				{
					//System.out.println("(Polling Process) Exception: Invaid JSON in mail configuration.");
					//consoleLog.log("(Polling Process) Exception: Invaid JSON in mail configuration.");
				}
			}
		}
		return "";
	}
	
	/*@Override
	public void run()
	{
		try 
		{
			urlRequest();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/

	
}
