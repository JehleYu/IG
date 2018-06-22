package EZshare;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import org.apache.commons.cli.ParseException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Client{
	private String ip = "sunrise.cis.unimelb.edu.au";   //"localhost"; //127.0.0.1
	private int port =  3780;
	
	public static void main(String[] args) throws ParseException, URISyntaxException{
		Client client = new Client();
//		String ip = "sunrise.cis.unimelb.edu.au";   //"localhost"; //127.0.0.1
//		int port =  3780;
		JSONParser parser = new JSONParser();
		JSONObject command = new JSONObject();
		ArgumentParser argParser = new ArgumentParser();
		command = argParser.clientArgsParse(args);		
		if(command.containsKey("host")){
			client.ip = (String)command.get("host");
			command.remove("host");
		}
		if(command.containsKey("port")){
			client.port = (int)command.get("port");
			command.remove("port");
		}
		
		try(Socket socket = new Socket(client.ip,client.port)){	
			DataInputStream input = new DataInputStream(socket.getInputStream());
			DataOutputStream output = new DataOutputStream(socket.getOutputStream());
					
			Boolean debug = (Boolean)command.get("debug");
			command.remove("debug");
			if(debug){
				System.out.println("SEND: "+command.toJSONString());
			}
			output.writeUTF(command.toJSONString());
			output.flush();
			while(true){
				if(input.available() > 0){
					
					JSONObject response = (JSONObject) parser.parse(input.readUTF());
					if(debug){
						System.out.println("RECEIVE:"+response.toJSONString());
					}
					client.handleResponse((String)command.get("command"),response);			
				}
			}		
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void handleResponse(String commandType, JSONObject response){
		switch(commandType){
			case "PUBLISH":
				if(response.get("response").equals("success")){
					System.out.println("publish succeeded");
				}else{
					System.out.println(response.get("errorMessage"));
				}break;
			case "REMOVE":
				if(response.get("response").equals("success")){
					System.out.println("remove succeeded");
				}else{
					System.out.println(response.get("errorMessage"));
				}break;
			case "SHARE":
				if(response.get("response").equals("success")){
					System.out.println("share succeeded");
				}else{
					System.out.println(response.get("errorMessage"));
				}break;
			case "EXCHANGE":
				//TODO  file download
				if(response.get("response").equals("success")){
					System.out.println("exchange succeeded");
				}else{
					System.out.println(response.get("errorMessage"));
				}break;
			case "QUERY":
				if(response.containsKey("response")){
					if(response.get("response").equals("success")){
						System.out.println("query succeeded");
						
					}else{
						System.out.println(response.get("errorMessage"));
					}
				}else if(response.containsKey("resultSize")){
					System.out.println("hit "+response.get("resultSize")+" resource(s)");
				}else{
					System.out.println("| "+response.get("name")+" "+response.get("tags"));
					System.out.println("| "+response.get("uri"));
					System.out.println("| =="+response.get("channel")+" ==");
					System.out.println("| ezserver: " + response.get("ezserver"));
				}break;
			case "FETCH":
				if(response.containsKey("response")){
					if(response.get("response").equals("success")){
						System.out.println("fetch succeeded");
						
					}else{
						System.out.println(response.get("errorMessage"));
					}
				}else if(response.containsKey("resultSize")){
					System.out.println("hit "+response.get("resultSize")+" resource(s)");
				}else{
					System.out.println("| "+response.get("name")+" "+response.get("tags"));
					System.out.println("| "+response.get("uri"));
					System.out.println("| =="+response.get("channel")+" ==");
					System.out.println("| ezserver: " + response.get("ezserver"));
					System.out.println("| " + response.get("resourceSize") + " bytes");
				}break;
		}
	}
 
}