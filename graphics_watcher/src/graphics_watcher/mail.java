package graphics_watcher;

//import java.io.FileNotFoundException;
import java.io.IOException;
//import java.net.ConnectException;
//import java.net.URLEncoder;
//import java.net.URLEncoder;
//import java.util.ArrayList;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

//import fileWatch.utilities;

public class mail implements Runnable
{
	private String mailId, status, subject;
	
	public mail(String mailId,String status, String subject, String attachment, String errParam)
	{
		this.mailId = mailId;
		this.status = status;
		this.subject = subject;
	}
	
	public void run()
	{
		try
		{
			sendMail(mailId,status,subject);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void sendMail(String mailId,String status, String subject) throws IOException
	{
		try
		{
			System.out.println("(Thread Process) mailId:"+mailId);
			//System.out.println("(Thread Process) mail_id:"+mail_id);
			System.out.println("(Thread Process) chName:"+subject);
			System.out.println("(Thread Process) status:"+status);
			
			final String username = "graphicsqs@codemantra.co.in";
			final String password = "5UNnCEE9";
	
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
			
			consoleLog.log("JSON Parse failed for JSON on maestroqs_support.json\n\n");
			System.out.println("JSON Parse failed for JSON on maestroqs_support.json");
			
			String mailIds = "rajarajan@codemantra.in";
			InternetAddress[] parse = InternetAddress.parse(mailIds , true);
			message.setRecipients(javax.mail.Message.RecipientType.TO,  parse);
					
					//return;
			
//			message.addRecipient(Message.RecipientType.BCC, new InternetAddress("rajkannan@codemantra.com"));
//			message.addRecipient(Message.RecipientType.BCC, new InternetAddress("thiyagarajan@codemantra.com"));
			message.addRecipient(Message.RecipientType.BCC, new InternetAddress("rajarajan@codemantra.in"));
			if( subject.equals("MOUNT"))
			{
				message.setSubject("NETWORK - ERROR");
			}
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
			
			// Now set the actual message
	        String mailMessage = "";
	        switch(status)
	        {
		        case "404":
	        		{
		        		mailMessage = "Dear All,\n\n"+
									subject+"\n" +
								"--------------------------------------------\n\n" +
				 				"Please find the attached error log and clear the listed errors within the InDD Template and Import Map respectively.\n\n" +
				 				"After clearing the error[s], the above error log need to be uploaded into Maestro Queuing System through \"cM_Tools->Comp->T&F->MaestroQS->InDT/IIMap Certification\".\n\n" +
				 				"\nThanks & Regards,\n" +
				 				"Maestro Queuing System";
	        		}
	        		break;
	        		
		        case "200":	
	        		{
	        			mailMessage = "Dear All,\n\n"+
								subject+"\n" +
							"--------------------------------------------\n\n" +
			 				"The file is ready for Maestro IDML process.\n\n" +
			 				"\nThanks & Regards,\n" +
			 				"Maestro Queuing System";
	        		}
	        		break;
	        		
	        }		
	        
	        message.setText(mailMessage);  
			
			//consoleLog.log("Mail content:"+mailMessage);
			Transport.send(message);
			System.out.println("(Thread Process) mail sent.\n\n");
			consoleLog.log("(Thread Process) mail sent.\n\n");
		}
		catch (MessagingException e)
		{
			//throw new RuntimeException(e);
			System.out.println("(Thread Process) Error in mailing parameters for javax api"+"\n");
			consoleLog.log("(Thread Process) Error in mailing parameters for javax api"+"\n");
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
}
