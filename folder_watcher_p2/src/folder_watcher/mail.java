package folder_watcher;

import java.io.File;
//import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
//import java.net.ConnectException;
import java.net.URLDecoder;
import java.net.UnknownHostException;
//import java.net.URLEncoder;
//import java.net.URLEncoder;
//import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

//import fileWatch.utilities;

public class mail implements Runnable
{
	private String group, status, subject, attachment, errParam;
	
	public mail(String group,String status, String subject, String attachment, String errParam)
	{
		this.group = group;
		this.status = status;
		this.subject = subject;
		this.attachment = attachment;
		this.errParam = errParam;
	}
	
	public static String sendPingRequest(String ipAddress) throws UnknownHostException, IOException
	{
		InetAddress geek = InetAddress.getByName(ipAddress);
		//System.out.println("Sending Ping Request to " + ipAddress);
		if (geek.isReachable(5000))
			return "online";
		else
			return "offline";
	}
	
	public void run()
	{
		try
		{
			sendMail(group,status,subject,attachment,errParam);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private String json_pretty_print(String json)
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
	
	public void sendMail(String group,String status, String subject, String attachment, String errParam) throws IOException, ParseException
	{
		try
		{
			System.out.println("(Thread Process) group:"+group);
			//System.out.println("(Thread Process) mail_id:"+mail_id);
			System.out.println("(Thread Process) chName:"+subject);
			System.out.println("(Thread Process) status:"+status);
			System.out.println("(Thread Process) attachment:"+attachment+"\n");
			System.out.println("(Thread Process) errParam:"+errParam+"\n");
			
			consoleLog.log("(Thread Process) group:"+group);
			//System.out.println("(Thread Process) mail_id:"+mail_id);
			consoleLog.log("(Thread Process) chName:"+subject);
			consoleLog.log("(Thread Process) status:"+status);
			consoleLog.log("(Thread Process) attachment:"+attachment+"\n");
			consoleLog.log("(Thread Process) errParam:"+errParam+"\n");
			
			final String username = "maestroqs@codemantra.in";
			final String password = "Mast$123";
	
			Properties props = new Properties();
			props.put("mail.smtp.auth", "true");
			//props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.host", "172.16.1.5");
			//props.put("mail.smtp.port", 587);
	
			Session session = Session.getInstance(props,
			new javax.mail.Authenticator() 
			{
				protected PasswordAuthentication getPasswordAuthentication() 
				{
					return new PasswordAuthentication(username, password);
				}
			});
	
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress("maestroqs@codemantra.in"));
			//message.setRecipients(Message.RecipientType.TO,InternetAddress.parse(mail_id));
			
			String mailGrpResponse="";
			if(!subject.equals("DB"))
			{
				consoleLog.log("(Thread Process) URL:\"http://"+url_request.serverIp+"/maestro/getMailConfig?groupName="+group+"\",type:\"GET\"\n");
				System.out.println("(Thread Process) URL:\"http://"+url_request.serverIp+"/maestro/getMailConfig?groupName="+group+"\",type:\"GET\"\n");
				
				mailGrpResponse = url_request.urlRequestProcess("http://"+url_request.serverIp+"/maestro/getMailConfig?groupName="+group,"GET","");
				
				consoleLog.log("(Thread Process) Response:"+json_pretty_print(mailGrpResponse)+"\n");
				System.out.println("(Thread Process) Response:"+json_pretty_print(mailGrpResponse)+"\n");
				if((mailGrpResponse != null) && (!mailGrpResponse.isEmpty()) && (!mailGrpResponse.equals("")))	
				{
					Object obj = new JSONParser().parse(mailGrpResponse);
					JSONObject jo = (JSONObject) obj;
					
					String mailIds = (String) jo.get("userEmailIds");
		     		
					consoleLog.log("(Thread Process) mail_ids:"+mailIds+"\n");
					System.out.println("(Thread Process) mail_ids:"+mailIds+"\n");
					
					//String to = "rajarajan@codemantra.in";
					InternetAddress[] parse = InternetAddress.parse(mailIds , true);
					message.setRecipients(javax.mail.Message.RecipientType.TO,  parse);
				}	
			}
			
			if(subject.equals("DB") || ((mailGrpResponse != null) && (mailGrpResponse.isEmpty()) && (mailGrpResponse.equals(""))))
			{
				//API server error
				//sendMail("Net-ops","ERROR","DB","","");
				if(new File("maestroqs_support.json").exists())
				{
					String mailIdJson = utilities.fileRead("maestroqs_support.json");
					if ((mailIdJson != null) && (!mailIdJson.isEmpty()) && (!mailIdJson.equals(""))) 
					{
						JSONParser parser = new JSONParser();
						Object preEditObj = parser.parse(mailIdJson);
						JSONObject jo = (JSONObject) preEditObj;
						String mailIds = (String) jo.get("mail_id");
	
						consoleLog.log("(Thread Process offline) mail_ids:"+mailIds+"\n");
						System.out.println("(Thread Process offline) mail_ids:"+mailIds+"\n");
						
						//String to = "rajarajan@codemantra.in";
						InternetAddress[] parse = InternetAddress.parse(mailIds , true);
						message.setRecipients(javax.mail.Message.RecipientType.TO,  parse);
					}
				}
				else 
				{
					consoleLog.log("JSON Parse failed for JSON on maestroqs_support.json\n\n");
					System.out.println("JSON Parse failed for JSON on maestroqs_support.json");
					
					String mailIds = "rajarajan@codemantra.in";
					InternetAddress[] parse = InternetAddress.parse(mailIds , true);
					message.setRecipients(javax.mail.Message.RecipientType.TO,  parse);
					
					//return;
				}
			}
			
			message.addRecipient(Message.RecipientType.BCC, new InternetAddress("rajkannan@codemantra.com"));
//			message.addRecipient(Message.RecipientType.BCC, new InternetAddress("thiyagarajan@codemantra.com"));
			message.addRecipient(Message.RecipientType.BCC, new InternetAddress("rajarajan@codemantra.in"));
			if((subject.equals("DB")&& status.equals("ERROR")) || (subject.equals("MOUNT") && status.equals("ERROR")))
				message.setSubject("NETWORK - ERROR");
			else
			{
				if(status.equals("JOB_SUCCESS"))
					message.setSubject(subject+" - SUCCESS");
				else if(status.equals("JOB_FAIL") || status.equals("API") || status.equals("STYLESHEET") || status.equals("DIRECTORY"))
					message.setSubject(subject+" - ERROR");
				else
					message.setSubject(subject+" - "+status);
			}
			//message.setText(mailMessage);
			
			// Create the message part
	        BodyPart messageBodyPart = new MimeBodyPart();

	        // Now set the actual message
	        String mailMessage = "";
	        switch(URLDecoder.decode(group, "UTF-8"))
	        {
		        case "Template":
	        		if(status.equals("ERROR"))
	        		{
	        			if(!attachment.isEmpty())
	        			{
			        		mailMessage = "Dear All,\n\n"+
										subject+"\n" +
									"--------------------------------------------\n\n" +
					 				"Please find the attached error log and clear the listed errors within the InDD Template and Import Map respectively.\n\n" +
					 				"After clearing the error[s], the above error log need to be uploaded into Maestro Queuing System through \"cM_Tools->Comp->T&F->MaestroQS->InDT/IIMap Certification\".\n\n" +
					 				"\nThanks & Regards,\n" +
					 				"Maestro Queuing System";
	        			}
	        			else
	        			{
	        				mailMessage = "Dear All,\n\n"+
									subject+"\n" +
								"--------------------------------------------\n\n" +
				 				"The Used stylesheet file for this manuscript is Invalid.\n\n" +
				 				"\nThanks & Regards,\n" +
				 				"Maestro Queuing System";
	        			}
	        		}
	        		else if(status.equals("SUCCESS"))
	        		{
	        			mailMessage = "Dear All,\n\n"+
								subject+"\n" +
							"--------------------------------------------\n\n" +
			 				"The file is ready for Maestro IDML process.\n\n" +
			 				"\nThanks & Regards,\n" +
			 				"Maestro Queuing System";
	        		}
	        		else if(status.equals("JOB_SUCCESS"))
	        		{
	        			mailMessage = "Dear All,\n\n"+
								subject+"\n" +
							"--------------------------------------------\n\n" +
							"This job is completed successfully.\n\n" +
							errParam + "/"+ errParam +" manuscript[s] has been posted for Maestro IDML process.\n\n" +
			 				"\nThanks & Regards,\n" +
			 				"Maestro Queuing System";
	        		}
	        		else if(status.equals("JOB_FAIL"))
	        		{
	        			mailMessage = "Dear All,\n\n"+
								subject+"\n" +
	        				"--------------------------------------------\n\n" +
			 				"This Job feed has failed and it has the following error[s].\n" +
			 				errParam +
			 				"\nPlease review and re-feed the Job into Maestro Queuing System.\n\n" +
			 				"\nThanks & Regards,\n" +
			 				"Maestro Queuing System";
	        		}
	        		break;
	        		
	        	case "Net-ops":
	        		if(subject.equals("MOUNT"))
	        		{
	        			if(status.equals("ERROR"))
	        			{
		        			mailMessage = "Dear Team,\n\n" +
		        						//"--------------------------------------------\n\n" +
		        						"MaestroQS has failed to mount the following server share[s] "+errParam+" within the system.\n\n" +
		        					   	"Please check and do the needful promptly.\n\n" +
		        					   	"\nThanks & Regards,\n" +
		        					   	"Maestro Queuing System";
	        			}
	        			else if(status.equals("SUCCESS"))
	        			{
	        				mailMessage = "Dear Team,\n\n" +
	        						//"--------------------------------------------\n\n" +
	        						"MaestroQS successfully mount the server share[s] within the system.\n\n" +
	        					   	"\nThanks & Regards,\n" +
	        					   	"Maestro Queuing System";
	        			}
	        		}
	        		else if(subject.equals("DB"))
	        		{
	        			if(status.equals("ERROR"))
	        			{
		        			mailMessage = "Dear Team,\n\n" +
		        						//"--------------------------------------------\n\n" +
		        						"MaestroQS is failed to connect with (IP: 172.16.1.25) server, it seems the server is \"Offline\".\n\n" +
		        						"Please check and do the needful ASAP.\n\n" +
		        						"\nThanks & Regards,\n" +
		        					   	"Maestro Queuing System";
	        			}
	        			else if(status.equals("SUCCESS"))
	        			{
	        				mailMessage = "Dear Team,\n\n" +
	        						//"--------------------------------------------\n\n" +
	        						"MaestroQS can able to connect with (IP: 172.16.1.25) Server, it seems the Server is back \"Online\".\n\n" +
	        						"\nThanks & Regards,\n" +
	        					   	"Maestro Queuing System";
	        			}
	        		}
	        		break;
	        		
	        	case "Pre-editing":
	        		if(status.equals("API"))
	        		{
		        		mailMessage = "Dear All,\n\n"+
									subject+"\n" +
									"--------------------------------------------\n\n" +
									"The file has not completed/passed all the Content-Modelling stages.\n\n" +
									"Please check and repost the file through Content-Modelling System.\n\n" +
									"\nThanks & Regards,\n" +
									"Maestro Queuing System";
		        	}
	        		else if(status.equals("STYLESHEET"))
	        		{
		        		mailMessage = "Dear All,\n\n"+
									subject+"\n" +
									"--------------------------------------------\n\n" +
									"The stylesheet file for \""+subject+"\" is missing.\n\n" +
									"Please check and repost the file through Content-Modelling System.\n\n" +
									"\nThanks & Regards,\n" +
									"Maestro Queuing System";
		        	}
	        		else if(status.equals("INVALID"))
	        		{
		        		mailMessage = "Dear All,\n\n"+
									subject+"\n" +
									"--------------------------------------------\n\n" +
									"The invalid file / folder ("+errParam+") posted by the user for this Job.\n\n" + 
									"Please follow the MaestroQS guidelines.\n\n" +
									//"This file("+errParam+") is invalid for the Job :\""+subject+"\".\n\n" +
									//"Please check and repost the file through Content-Modelling module.\n\n" +
									"\nThanks & Regards,\n" +
									"Maestro Queuing System";
		        	}
	        		else if(status.equals("JOB_FAIL"))
	        		{
	        			mailMessage = "Dear All,\n\n"+
							subject+"\n" +
	        				"--------------------------------------------\n\n" +
			 				"This Job feed has failed and it has the following error[s].\n" +
			 				errParam +
			 				"\nPlease review and re-feed the Job into Maestro Queuing System.\n\n" +
			 				"\nThanks & Regards,\n" +
			 				"Maestro Queuing System";
	        		}
	        		else if(status.equals("DIRECTORY"))
	        		{
	        			mailMessage = "Dear All,\n\n"+
								subject+"\n" +
							"--------------------------------------------\n\n" +
			 				"MaestroQS failed to locate the \"Equations\\"+subject+"\" folder for this chapter in the \"MaestroReady\" folder.\n\n" +
			 				"\nThanks & Regards,\n" +
			 				"Maestro Queuing System";
	        		}
	        		else if(status.equals("ERROR"))
	        		{
	        			if(!attachment.isEmpty())
	        			{
			        		mailMessage = "Dear All,\n\n"+
										subject+"\n" +
									"--------------------------------------------\n\n" +
					 				"Please find the attached error log and clear the listed errors within the InDD Template and Import Map respectively.\n\n" +
					 				"After clearing the error[s], the above error log need to be uploaded into Maestro Queuing System through \"cM_Tools->Comp->T&F->MaestroQS->InDT/IIMap Certification\".\n\n" +
					 				"\nThanks & Regards,\n" +
					 				"Maestro Queuing System";
	        			}
	        			else
	        			{
	        				mailMessage = "Dear All,\n\n"+
									subject+"\n" +
								"--------------------------------------------\n\n" +
				 				"The Used stylesheet file for this manuscript is Invalid.\n\n" +
				 				"\nThanks & Regards,\n" +
				 				"Maestro Queuing System";
	        			}
	        		}
	        		break;
	        		
	        	case "CRC Team":
	        		if(status.equals("JOB_FAIL"))
	        		{
	        			mailMessage = "Dear All,\n\n"+
							subject+"\n" +
	        				"--------------------------------------------\n\n" +
			 				"This Job feed has failed and it has the following error[s].\n" +
			 				errParam +
			 				"\nPlease review and re-feed the Job into Maestro Queuing System.\n\n" +
			 				"\nThanks & Regards,\n" +
			 				"Maestro Queuing System";
	        		}
	        		else if(status.equals("ERROR"))
	        		{
	        			if(!attachment.isEmpty())
	        			{
			        		mailMessage = "Dear All,\n\n"+
										subject+"\n" +
									"--------------------------------------------\n\n" +
					 				"Please find the attached error log and clear the listed errors within the InDD Template and Import Map respectively.\n\n" +
					 				"After clearing the error[s], the above error log need to be uploaded into Maestro Queuing System through \"cM_Tools->Comp->T&F->MaestroQS->InDT/IIMap Certification\".\n\n" +
					 				"\nThanks & Regards,\n" +
					 				"Maestro Queuing System";
	        			}
	        			else
	        			{
	        				mailMessage = "Dear All,\n\n"+
									subject+"\n" +
								"--------------------------------------------\n\n" +
				 				"The Used stylesheet file for this manuscript is Invalid.\n\n" +
				 				"\nThanks & Regards,\n" +
				 				"Maestro Queuing System";
	        			}
//	        			mailMessage = "Dear All,\n\n"+
//								subject+"\n" +
//							"--------------------------------------------\n\n" +
//			 				"Please find the attached error log and clear the listed errors within the InDD Template and Import Map respectively.\n\n" +
//			 				"After clearing the erro[s], the above error log need to be uploaded into Maestro Queuing System through \"cM_Tools->Comp->T&F->MaestroQS->InDT/IIMap Certification\".\n\n" +
//			 				"\nThanks & Regards,\n" +
//			 				"Maestro Queuing System";
	        		}
	        		else if(status.equals("SUCCESS"))
	        		{
	        			mailMessage = "Dear All,\n\n"+
								subject+"\n" +
							"--------------------------------------------\n\n" +
			 				"The file is ready for Maestro IDML process.\n\n" +
			 				"\nThanks & Regards,\n" +
			 				"Maestro Queuing System";
	        		}
	        		else if(status.equals("DIRECTORY"))
	        		{
	        			mailMessage = "Dear All,\n\n"+
								subject+"\n" +
							"--------------------------------------------\n\n" +
			 				"MaestroQS failed to locate the Job folder ("+errParam+") in the following server share[s] within the system (IP:172.16.4.184).\n\n" +
			 				"\nThanks & Regards,\n" +
			 				"Maestro Queuing System";
	        		}
	        		break;
	        		
	        	case "Graphics":
	        		if(status.equals("JOB_FAIL"))
	        		{
	        			mailMessage = "Dear All,\n\n"+
							subject+"\n" +
	        				"--------------------------------------------\n\n" +
			 				"This Job feed has failed and it has the following error[s].\n" +
			 				errParam +
			 				"\nPlease review and re-feed the Job into Maestro Queuing System.\n\n" +
			 				"\nThanks & Regards,\n" +
			 				"Maestro Queuing System";
	        		}
	        		break;
	        }
	        
			if(!attachment.isEmpty())
			{
				File file = new File(attachment);
				if (file.exists() && file.isFile())
			    {
					System.out.println("(Thread Process) attachment exists.");
					consoleLog.log("(Thread Process) attachment exists.");
					
					messageBodyPart.setText(mailMessage);
					
					// Create a multipar message
			        Multipart multipart = new MimeMultipart();
	
			        // Set text message part
			        multipart.addBodyPart(messageBodyPart);
			        
					// Part two is attachment
					messageBodyPart = new MimeBodyPart();
					String filename = attachment;//"/Users/comp/Desktop/9781138556850_Ilyas_CH01_InddReport.xls";
					DataSource source = new FileDataSource(filename);
					messageBodyPart.setDataHandler(new DataHandler(source));
					messageBodyPart.setFileName(attachment.substring(attachment.lastIndexOf("/")+1));
					multipart.addBodyPart(messageBodyPart);
					// Send the complete message parts
					message.setContent(multipart);
			    }
				else
				{
					System.out.println("(Thread Process) attachment not found, mail not sent.\n");
					consoleLog.log("(Thread Process) attachment not found, mail not sent.\n");
					return;
				}
			}
			else
				 message.setText(mailMessage);  
			
			//consoleLog.log("Mail content:"+mailMessage);
			while(sendPingRequest("172.16.1.5").equals("offline"))
			{
				TimeUnit.SECONDS.sleep(1);
				//mail ip is offline
			}
			Transport.send(message);
			System.out.println("(Thread Process) mail sent to group="+group+" with subject="+subject+" and status="+status+".\n\n");
			consoleLog.log("(Thread Process) mail sent to group="+group+" with subject="+subject+" and status="+status+".\n\n");
		}
		catch (MessagingException e)
		{
			//throw new RuntimeException(e);
			consoleLog.log("(Thread Process) mail failed to send to group="+group+" with subject="+subject+" and status="+status+".\n\n");
			System.out.println("(Thread Process) mail failed to send to group="+group+" with subject="+subject+" and status="+status+".\n\n");
			
			System.out.println("(Thread Process) Error in mailing parameters for javax api"+"\n"+e.toString());
			consoleLog.log("(Thread Process) Error in mailing parameters for javax api"+"\n"+e.toString());
		}
		catch (Exception e) 
		{
			//e.printStackTrace();
			switch(e.toString().substring(0,e.toString().indexOf(":")))
			{
				case "java.nio.file.NoSuchFileException":
				{
					System.out.println("(Thread Process) Directory does not exists");
					consoleLog.log("(Thread Process) Exception:Directory to scan does not exists in SMB Server.");
				}
				break;
				
				case "java.net.SocketException":
				{
					System.out.println("(Thread Process) connection refuse error");
					consoleLog.log("(Thread Process) Exception:Could not able to connect to API Server.");
				}
				break;
				
				case "java.net.ConnectException":
				{
					consoleLog.log("(Thread Process) connection refuse error");
					System.out.println("(Thread Process) Exception:Could not able to connect to API Server.");
					
					//if(mailTriggNet)
					{
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
					}
					//mailObj.mailProcess("Net-ops","ERROR","DB","");
				}
				break;
				
				case "java.lang.IndexOutOfBoundsException":
				{
					System.out.println("(Thread Process) Error in sending mail.");
					consoleLog.log("(Thread Process) Error in sending mail.");
				}
				break;
				
				case "java.lang.NullException":
				{
					System.out.println("(Thread Process) Exception: sending mail.");
					consoleLog.log("(Thread Process) Exception: sending mail.");
				}
				break;
				
				case "org.json.simple.parser.ParseException":
				{
					System.out.println("(Thread Process) Exception: Invaid JSON in mail configuration.");
					consoleLog.log("(Thread Process) Exception: Invaid JSON in mail configuration.");
				}
			}
		}
		return;
	}
	
//	public static ArrayList<String> mailIdParse(String mail_ids)
//	{
//		//String mail_ids = "dhivakart@codemantra.co.in,thiyagarajan@codemantra.com,rajkannan@codemantra.com";
//        ArrayList<String> mail_id = new ArrayList<String>();
//        
//        System.out.println("mail_ids.length():"+mail_ids.length());
//        
//        for(; mail_ids.length() > 0;)
//        {
//            if(mail_ids.indexOf(",") > 0)
//            {
//                mail_id.add(mail_ids.substring(0,mail_ids.indexOf(",")));
//                mail_ids = mail_ids.substring(mail_ids.indexOf(",")+1,mail_ids.length());
//                //System.out.println("id:"+mail_id.get(i));
//                //System.out.println("mail_ids:"+mail_ids);
//            }
//            else if(!mail_ids.isEmpty())
//            {
//               mail_id.add(mail_ids);
//               mail_ids = "";
//               //System.out.println("id:"+mail_id.get(1));
//               // System.out.println("mail_ids:"+mail_ids);
//            }
//        }
//        return mail_id;
//	}
	
//	public void mailProcess(String group,String status,String subject,String attachment, String errParam) throws ConnectException, IOException, ParseException
//	{
//		try 
//		{
//			System.out.println("(Thread Process) group:"+group);
//			consoleLog.log("(Thread Process) group:"+group);
////			System.out.println("status:"+status);
////			System.out.println("subject:"+subject);
////			System.out.println("attachment:"+attachment);
//			
//			//api call server is down or url doesnot exists
//			consoleLog.log("(Thread Process) URL:\"http://"+url_request.serverIp+"/maestro/getMailConfig?groupName="+group+"\",type:\"GET\"\n");
//			System.out.println("(Thread Process) URL:\"http://"+url_request.serverIp+"/maestro/getMailConfig?groupName="+group+"\",type:\"GET\"\n");
//			
//			String response = url_request.urlRequestProcess("http://"+url_request.serverIp+"/maestro/getMailConfig?groupName="+group,"GET","");
//			
//			consoleLog.log("(Thread Process) Response:"+response+"\n");
//			System.out.println("(Thread Process) Response:"+response+"\n");
//			if((response != null) && (!response.isEmpty()) && (!response.equals("")))
//			{
//				// typecasting obj to JSONObject
//				Object obj = new JSONParser().parse(response);
//				JSONObject jo = (JSONObject) obj;
//				
//				//String group = (String) jo.get("groupName");
//				String mail_ids = (String) jo.get("userEmailIds");
//	      
//				//String message = "Dear Mail Crawler,"
//				//    		   	+"No spam to my email, please!";
//	      
//				//mail trigger server is down or url doesnot exists
//				
//				consoleLog.log("(Thread Process) mail_ids:"+mail_ids+"\n");
//				System.out.println("(Thread Process) mail_ids:"+mail_ids+"\n");
//				
//				if(!mail_ids.isEmpty() && !status.isEmpty() && !subject.isEmpty() )
//				{
//					//mail failedMail = new mail();
//					ArrayList<String> mail_id = mailIdParse(mail_ids);
//					for(int i=0; i < mail_id.size();i++)
//					{
//						consoleLog.log("(Thread Process) mail_id:"+mail_id+"\n");
//						System.out.println("(Thread Process) mail_id:"+mail_id+"\n");
//						sendMail(group,mail_id.get(i),status,subject,attachment,errParam);
//					}
//				}
//			}
//			else
//			{
//				String mailIdJson = utilities.fileRead("maestroqs_support.json");
//				if((mailIdJson != null) && (!mailIdJson.isEmpty()) && (!mailIdJson.equals("")))
//    			{
//					JSONParser parser = new JSONParser();
//					Object preEditObj = parser.parse(mailIdJson);
//			        JSONObject jo = (JSONObject) preEditObj;
//	    		    String mailIds = (String) jo.get("mail_id");
//	    		    if((response != null) && (!response.isEmpty()) && (!response.equals("")))
//	    			{
//		    		    ArrayList<String> mail_id = mailIdParse(mailIds);
//						for(int i=0; i < mail_id.size();i++)
//						{
//							sendMail("Net-ops",mail_id.get(i),"ERROR","DB","","");
//						}
//	    			}
//    			}
//			}
//		}
//		catch (Exception e) 
//		{
//			e.printStackTrace();
//			switch(e.toString().substring(0,e.toString().indexOf(":")))
//			{
//				case "java.nio.file.NoSuchFileException":
//				{
//					System.out.println("(Thread Process) Directory does not exists");
//					consoleLog.log("(Thread Process) Exception:Directory to scan does not exists in SMB Server.");
//					
//					//for comp
////					mail mailObj = new mail();
////					mailObj.mailProcess("Template","DIRECTORY_NOT_EXISTS","DIRECTORY","");
////	    			mailObj.mailProcess(URLEncoder.encode("CRC Team", "UTF-8"),"DIRECTORY_NOT_EXISTS","DIRECTORY","");
//				}
//				break;
//				
//				case "java.net.SocketException":
//				{
//					System.out.println("(Thread Process) connection refuse error");
//					consoleLog.log("(Thread Process) Exception:Could not able to connect to API Server.");
////					mail mailObj = new mail();
////					mailObj.mailProcess("Net-ops","ERROR","DB","");
//				}
//				break;
//				
//				case "java.net.ConnectException":
//				{
//					consoleLog.log("(Thread Process) connection refuse error");
//					System.out.println("(Thread Process) Exception:Could not able to connect to API Server.");
//					
//					//if(mailTriggNet)
//					{
//						//utilities U = new utilities();
////						String mailIdJson = utilities.fileRead("maestroqs_support.json");
////						JSONParser parser = new JSONParser();
////						Object preEditObj = parser.parse(mailIdJson);
////				        JSONObject jo = (JSONObject) preEditObj;
////		    		    String mailIds = (String) jo.get("mail_id");
////		    		    
////		    		    mail mailObj = new mail();
////		    		    ArrayList<String> mail_id = mail.mailIdParse(mailIds);
////						for(int i=0; i < mail_id.size();i++)
////						{
////							mailObj.sendMail("Net-ops",mail_id.get(i),"ERROR","DB","","");
////						}
//						//mailTriggNet = false;
//					}
//					//mailObj.mailProcess("Net-ops","ERROR","DB","");
//				}
//				break;
//				
//				case "java.lang.IndexOutOfBoundsException":
//				{
//					System.out.println("(Thread Process) Error in sending mail.");
//					consoleLog.log("(Thread Process) Error in sending mail.");
//				}
//				break;
//				
//				case "java.lang.NullException":
//				{
//					System.out.println("(Thread Process) Exception: sending mail.");
//					consoleLog.log("(Thread Process) Exception: sending mail.");
//				}
//				break;
//				
//			}
//		}
//	}
}
