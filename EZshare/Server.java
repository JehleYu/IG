package EZshare;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ServerSocketFactory;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;



public class Server {
	private JSONArray resourceList = new JSONArray();
	private JSONArray serverList = new JSONArray();
	
	
	private String advertisedhostname = "";
	private int connectionintervallimit = 10;
	private int exchangeinterval = 600;
	private int port = 1024;  //port class change to Long after transfer, don't unser
	private String ip = "";
	private String secret = "2os41f58vkd9e1q4ua6ov5emlv";
	
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws org.apache.commons.cli.ParseException, UnknownHostException {
		Server server = new Server();
		ArgumentParser argsParser = new ArgumentParser();
		JSONObject argsJSON = argsParser.serverArgsParse(args);
		server.advertisedhostname = argsJSON.containsKey("advertisedhostname")?(String) argsJSON.get("advertisedhostname"):server.advertisedhostname;	
		server.connectionintervallimit = argsJSON.containsKey("connectionintervallimit")?(int) argsJSON.get("connectionintervallimit"):server.connectionintervallimit;
		server.exchangeinterval = argsJSON.containsKey("exchangeinterval")?(int) argsJSON.get("exchangeinterval"):server.exchangeinterval;
		server.port = argsJSON.containsKey("port")?(int)argsJSON.get("port"):server.port;
		server.secret = argsJSON.containsKey("secret")?(String) argsJSON.get("secret"):server.secret;
		server.ip = InetAddress.getLocalHost().getHostAddress();
		
		System.out.println("Starting the EZshare Server");
		System.out.println("using secret: "+server.secret);
		System.out.println("using advertised host name: "+server.advertisedhostname);
		System.out.println("bond to port "+server.port);			
		System.out.println("started");
		
		server.runInteraction(server.exchangeinterval);
		
		JSONObject serverAddr = new JSONObject();
		serverAddr.put("hostname",server.ip);
		serverAddr.put("port",server.port);
		server.serverList.add(serverAddr);
		
		ServerSocketFactory fact = ServerSocketFactory.getDefault();
		try(ServerSocket serverSocket = fact.createServerSocket(server.port)){
			boolean isActivate = true;
			while(isActivate){
				Socket client = serverSocket.accept();
				System.out.println("IP:PORT : "+client.getInetAddress()+":"+client.getPort()+" connected");
				Thread t =new Thread(() -> server.serverClient(client));
				t.start();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private void serverClient(Socket client){
		
		try(Socket clientSocket = client){
			
			JSONParser parser = new JSONParser();
			DataInputStream input = new DataInputStream(clientSocket.getInputStream());
			DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
		
			while(true){
				if(input.available() > 0){

					
					JSONObject command = (JSONObject) parser.parse(input.readUTF());
					//the invalid command will not be transmitted and command will be null
					JSONArray responseArray = new JSONArray();
					if(command == null){
						responseArray.add(nullcommand(command));
					}
					else if (command.get("command").equals("PUBLISH")) {
						responseArray.add(this.dealingPublish(command));
					}
					else if (command.get("command").equals("REMOVE")) {
						responseArray.add(this.dealingRemove(command));
					}
					else if (command.get("command").equals("SHARE")) {
						responseArray.add(this.dealingShare(command));
					}
					else if (command.get("command").equals("QUERY")) {
						responseArray = (JSONArray) this.dealingQuery(command).clone();
					}
					else if (command.get("command").equals("FETCH")) {
						responseArray = (JSONArray) this.dealingFetch(command).clone();
					}
					else if (command.get("command").equals("EXCHANGE")) {
						responseArray.add(this.dealingExchange(command));
					}
					else {
						responseArray.add(invalidcommand(command));
					}
					
					//TODO  debug
					for(int i = 0; i< responseArray.size(); i++){
						output.writeUTF(((JSONObject)responseArray.get(i)).toJSONString());
						output.flush();
					}

				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	//server to server communication (ad hoc)
	private void serverServer(Socket remoServer){
		try(Socket remoServerSocket = remoServer){
			
			JSONParser parser = new JSONParser();
			DataInputStream input = new DataInputStream(remoServerSocket.getInputStream());
			DataOutputStream output = new DataOutputStream(remoServerSocket.getOutputStream());
		
			while(true){
				if(input.available() > 0){

					JSONObject command = (JSONObject) parser.parse(input.readUTF());
					//the invalid command will not be transmitted and command will be null
					JSONObject response = new JSONObject();
					JSONArray responseArray = new JSONArray();
					if(command == null){
						response = nullcommand(command);
					}
					else if (command.get("command").equals("PUBLISH")) {
						response = this.dealingPublish(command);
					}
					else if (command.get("command").equals("REMOVE")) {
						response = this.dealingRemove(command);
					}
					else if (command.get("command").equals("SHARE")) {
						response = this.dealingShare(command);
					}
					else if (command.get("command").equals("QUERY")) {
						responseArray = this.dealingQuery(command);
					}
					else if (command.get("command").equals("FETCH")) {
						responseArray = this.dealingFetch(command);
					}
					else if (command.get("command").equals("EXCHANGE")) {
						response = this.dealingExchange(command);
					}
					else {
						response = invalidcommand(command);
					}
							
					//TODO  debug		
					if(command.get("command").equals("QUERY")||command.get("command").equals("FETCH")){
						for(int i = 0; i< responseArray.size(); i++){
							output.writeUTF(((JSONObject)responseArray.get(i)).toJSONString());
							output.flush();
						}
					}else{
					//System.out.print("form client:"+command.toJSONString());
						output.writeUTF(response.toJSONString());
						output.flush();
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	//This is a method dealing with null command
	@SuppressWarnings({ "unchecked" })
	private synchronized static JSONObject nullcommand(JSONObject command){
		JSONObject response = new JSONObject();
		response.put("response", "error");
		response.put("errorMessage","missing or incorrect type for command");
		return response;
	}
		
		
	//This is a method dealing with publish, throwing different kinds of exceptions
	@SuppressWarnings({ "unchecked", "unused" })
	private synchronized JSONObject dealingPublish(JSONObject command) {
		JSONObject response = new JSONObject();
		//get the resource
		JSONObject resource = (JSONObject) command.get("resource");
		URI uri = URI.create((String)resource.get("uri"));
		String name = (String) resource.get("name");
		String description = (String) resource.get("description");
		String tags = resource.get("tags").toString();
//			JSONArray tagsJA = (JSONArray) resource.get("tags");
//			String[] tagsList = new String[tagsJA.size()];
//			for(int i = 0; i< tagsJA.size(); i++){
//				tagsList[i] = (String) tagsJA.get(i);
//			}
//			String[] tagsList = tags.split(",");
		String channel = (String) resource.get("channel");
		String owner = (String) resource.get("owner");
		String uriString = (String) resource.get("uri");
		int duplicationNo = -1;
//			boolean tagsarevalid = true;
		boolean duplication = false;
		for(int i = 0; i< this.resourceList.size();i++){
			JSONObject storeddata = (JSONObject) this.resourceList.get(i);
			URI storeduri = URI.create(storeddata.get("uri").toString());
			if(storeduri == uri){
				duplication = true;
				duplicationNo = i;
			}
		}
		//TODO  tags.contains("\0")
//			for(int i = 0; i<tagsList.length; i++){
//				if(tagsList[i].contains("\0")){
//					tagsarevalid = false;
//				}
//			}
		
		name = name.trim();
		description = description.trim();
		channel = channel.trim();
		owner = owner.trim();
		
		if(resource == null){
			response.put("response", "error");
			response.put("errorMessage", "missing resource");
			return response;
		}else if(owner.equals("*") || !uri.isAbsolute() ||
				channel.contains("\0") || name.contains("\0") ||
				description.contains("\0") || tags.contains("\0") ||
				channel.contains("\0") || owner.contains("\0") ||
				uriString.contains("\0")
				){
			response.put("response", "error");
			response.put("errorMessage", "invalid resource");
			return response;
		}else if(uri==null) {
			response.put("response", "error");
			response.put("errorMessage", "cannot publish resource");
			return response;
		}else if(duplication){
			JSONObject duplicatedData = (JSONObject) this.resourceList.get(duplicationNo);
			if(owner.equals((String)duplicatedData.get("owner"))
				&& channel.equals((String)duplicatedData.get("channel"))){
				//replace
				this.resourceList.set(duplicationNo, resource);	
				response.put("response", "success");
				return response;
			}else{
				response.put("response", "error");
				response.put("errorMessage", "cannot publish resource");
				return response;
			}
			
		}else{
			int index = this.resourceList.size();
			this.resourceList.add(index,resource);
			response.put("response", "success");
			return response;
		}	
	}
		
		
	@SuppressWarnings({ "unchecked" })
	private synchronized JSONObject dealingRemove(JSONObject command){
		JSONObject response = new JSONObject();
		//get the resource
		JSONObject resource = (JSONObject) command.get("resource");
		URI uri = URI.create((String)resource.get("uri"));
		String name = (String) resource.get("name");
		String description = (String) resource.get("description");
		String tags = resource.get("tags").toString();
//			String[] tagsList = tags.split(",");
		String channel = (String) resource.get("channel");
		String owner = (String) resource.get("owner");
		String uriString = (String) resource.get("uri");
		
		name = name.trim();
		description = description.trim();
		channel = channel.trim();
		owner = owner.trim();
		
		if (uri == null) {
			response.put("response", "error");
			response.put("errorMessage", "cannot remove resource");
			return response;
		}else if (owner.equals("*") || !uri.isAbsolute() ||
				channel.contains("\0") || name.contains("\0") ||
				description.contains("\0") ||  
				channel.contains("\0") || owner.contains("\0") ||
				uriString.contains("\0") || tags.contains("\0")) {
			response.put("response", "error");
			response.put("errorMessage", "invalid resource");
			return response;
		}else{
			int index;
			for(index= 0; index<this.resourceList.size(); index++){
				JSONObject tempresource = (JSONObject) this.resourceList.get(index);
				if (owner.equals((String)tempresource.get("owner"))
					&& channel.equals((String)tempresource.get("channel"))
					&& uri.equals(URI.create((String)tempresource.get("uri")))){
					this.resourceList.remove(index);
					response.put("response", "success");
					return response;
				}
			}
			response.put("response", "error");
			response.put("errorMessage", "cannot remove resource");
			return response;	
		}
	}
		
		
	@SuppressWarnings("unchecked")
	private synchronized JSONObject dealingShare(JSONObject command){
		JSONObject response = new JSONObject();
		//get the resource
		String secret = (String) command.get("secret");
		JSONObject resource = (JSONObject) command.get("resource");
		URI uri = URI.create((String)resource.get("uri"));
		String name = (String) resource.get("name");
		String description = (String) resource.get("description");
		String tags = resource.get("tags").toString();
		String[] tagsList = tags.split(",");
		String channel = (String) resource.get("channel");
		String owner = (String) resource.get("owner");
		String uriString = (String) resource.get("uri");
		boolean correctSecret = false;
		boolean duplication = false;
		int duplicationNo = -1;
	//			for(int i = 0; i<resourceList.size(); i++){
	//				JSONObject temptresource = (JSONObject) resourceList.get(i);
	//				if (secret.equals(temptresource.get("secret"))){
	//					correctSecret = true;
	//					index = 
	//				}
	//			}
		
		name = name.trim();
		description = description.trim();
		channel = channel.trim();
		owner = owner.trim();
		secret = secret.trim();
		
		for(int i = 0; i<this.resourceList.size();i++){
			JSONObject storeddata = (JSONObject) this.resourceList.get(i);
			URI storeduri = URI.create(storeddata.get("uri").toString());
			if(storeduri.equals(uri)){
				duplication = true;
				duplicationNo = i;
			}
		}
		
		if (secret.equals("")){
			response.put("response", "error");
			response.put("errorMessage", "missing resource and/or secret");
			return response;
			
			//the followings deals with secret, having problems???
		}else if(secret.contains("\0") || !secret.equals(this.secret)){
			response.put("response", "error");
			response.put("errorMessage", "incorrect secret");
			return response;
			//the followings are fine
		}else if(owner.equals("*") ||
				channel.contains("\0") || name.contains("\0") ||
				description.contains("\0") || secret.contains("\0") ||
				channel.contains("\0") || owner.contains("\0") ||
				uriString.contains("\0")
				){
			response.put("response", "error");
			response.put("errorMessage", "invalid resource");
			return response;
		}else if(!uri.isAbsolute()){
			response.put("response", "error");
			response.put("errorMessage", "cannot share resource");
			return response;
		}else if(duplication){
			JSONObject duplicatedData = (JSONObject) this.resourceList.get(duplicationNo);
			if(owner.equals((String)duplicatedData.get("owner"))
				&& channel.equals((String)duplicatedData.get("channel"))){
				//replace
				this.resourceList.set(duplicationNo, resource);
				response.put("response", "success");
				return response;
			}else{
				response.put("response", "error");
				response.put("errorMessage", "cannot share resource");
				return response;
			}
		}else{
			this.resourceList.add(resourceList.size(),resource);
			response.put("response", "success");
			return response;
		}
	}
		
		
	@SuppressWarnings("unchecked")
	private synchronized JSONArray dealingQuery(JSONObject command) {
		JSONArray responseArray = new JSONArray();
		JSONObject response = new JSONObject();
		JSONObject resourceTemplate = (JSONObject) command.get("resourceTemplate");
		if(resourceTemplate == null){
			response.put("response", "error");
			response.put("errorMessage", "missing resourceTemplate");
			responseArray.add(response);
			return responseArray;
		}
		URI uri = URI.create((String)resourceTemplate.get("uri"));
		String name = (String) resourceTemplate.get("name");
		String description = (String) resourceTemplate.get("description");
		JSONArray tagsArray = (JSONArray)resourceTemplate.get("tags");
		boolean tagIsValid = true;
		ArrayList<String> tagsList = new ArrayList<String>();
		if(!tagsArray.isEmpty()){
			for(int i = 0; i<tagsArray.size();i++){
				tagsList.add(i,tagsArray.get(i).toString());
				if(( tagsList.get(i)).contains("\0")){
					tagIsValid = false;
				}
			}
		}
		
		String channel = (String) resourceTemplate.get("channel");
		String owner = (String) resourceTemplate.get("owner");
		String uriString = (String) resourceTemplate.get("uri");
		boolean match = false;

		name = name.trim();
		description = description.trim();
		channel = channel.trim();
		owner = owner.trim();
		
		if(owner.equals("*") ||
				channel.contains("\0") || name.contains("\0") ||
				description.contains("\0") || !tagIsValid ||
				channel.contains("\0") || owner.contains("\0") ||
				uriString.contains("\0")
				){
			response.put("response", "error");
			response.put("errorMessage", "invalid resourceTemplate");
			responseArray.add(response);
			return responseArray;
			
			//The following dealing with invalid resourceTemplate, mainly breaking rules
			
		}else{
			response.put("response", "success");
			responseArray.add(response);
			int resultSize = 0;
			for (int i = 0; i < this.resourceList.size(); i++) {
				JSONObject realResource = (JSONObject) this.resourceList.get(i);
				JSONObject temptResource = (JSONObject) realResource.clone();
				String temptName = (String) temptResource.get("name");
				String temptDescription = (String) temptResource.get("description");
				JSONArray temptTagsArray = (JSONArray)temptResource.get("tags");
				ArrayList<String> temptTagsList = new ArrayList<String>();
				if(!temptTagsArray.isEmpty()){
					for(int j = 0; j<temptTagsArray.size();j++){
						temptTagsList.add(j,temptTagsArray.get(j).toString());
					}
				}
				
//					String[] temptTagsList = new String[temptTagsArray.size()];
//					for(int j = 0; j < temptTagsArray.size(); j++){
//						temptTagsList[j] = temptTagsArray.get(j).toString();
//					}
				String temptChannel = (String) temptResource.get("channel");
				String temptOwner = (String) temptResource.get("owner");
				String temptUriString = (String) temptResource.get("uri");
				int temptTagsNo = 0;
				
				for(int j = 0; j<tagsList.size(); j++){
					for(int k = 0; k<temptTagsList.size(); k++){
						if(temptTagsList.get(k).equals(tagsList.get(j))){
							temptTagsNo = temptTagsNo + 1;
						}
					}
				}

				if (channel.equals(temptChannel)) {
					if(((owner.equals("")) || owner.equals(temptOwner))){
						if ( tagsList.isEmpty() || temptTagsNo == tagsList.size()){
							if((uriString.equals("")) || uriString.equals(temptUriString)){
								if ( 
										( (!name.equals("")) && temptName.contains(name) )
										|| ( (!description.equals(""))  && temptDescription.contains(description) )
										|| ( name.equals("") && description.equals("") )
									){						

									//put the whole JSON object to the outputResource
									temptResource.replace("owner", temptOwner.equals("")?"":"*");
									if(this.advertisedhostname != ""){
										temptResource.replace("ezserver", this.advertisedhostname+":"+this.port);
									}else{
										temptResource.replace("ezserver", this.ip+":"+this.port);
									}
									responseArray.add(temptResource);
									resultSize = resultSize + 1;
								}
							}
						}
					}
					
				}
			}
			
			boolean relay = (boolean) command.get("relay");
			if(relay){
				JSONArray remoResourceList = new JSONArray();
				command.replace("relay", false);
				for(int i = 0;i<this.serverList.size(); i++){
					JSONArray remoResponse = new JSONArray();
					JSONObject targetAddr = (JSONObject) this.serverList.get(i);
					String targetIP = (String) targetAddr.get("hostname");
					int targetPort = (int) targetAddr.get("port");
					if(!(targetIP.equals(this.ip) && targetPort == this.port)){
						remoResponse = this.transferQueryCommand(targetIP, targetPort, command);
						if(remoResponse.size() > 2){
							JSONObject resultSizeJSON = (JSONObject) remoResponse.get(remoResponse.size()-1);
							resultSize += (int)resultSizeJSON.get("resultSize");
							remoResponse.remove(remoResponse.size()-1);
							remoResponse.remove(0);
							remoResourceList.addAll(remoResponse);
						}
					}
				}
				responseArray.addAll(remoResourceList);
			}
			
			JSONObject resourceSize = new JSONObject();
			resourceSize.put("resultSize", resultSize);
			responseArray.add(resourceSize);
			return responseArray; //The ezserver should be set in the server cache
		}	
	}
		
	//TODO  file download
	@SuppressWarnings("unchecked")
	private synchronized JSONArray dealingFetch(JSONObject command) {
		JSONObject response = new JSONObject();
		JSONArray responseArray = new JSONArray();
		JSONObject resourceTemplate = (JSONObject) command.get("resourceTemplate");
		if(resourceTemplate == null){
			response.put("response", "error");
			response.put("errorMessage", "missing resourceTemplate");
			responseArray.add(response);
			return responseArray;
		}
		URI uri = URI.create((String)resourceTemplate.get("uri"));
		String name = (String) resourceTemplate.get("name");
		String description = (String) resourceTemplate.get("description");
		
		String channel = (String) resourceTemplate.get("channel");
		String owner = (String) resourceTemplate.get("owner");
		String uriString = (String) resourceTemplate.get("uri");
		boolean match = false;
		int matchNo = -1;
		JSONArray temptResourceList = (JSONArray) this.resourceList.clone();
		for (int i = 0; i<temptResourceList.size(); i++){
			JSONObject temptresource = (JSONObject)temptResourceList.get(i);
			URI temptUri = URI.create((String)temptresource.get("uri"));
			String temptChannel = (String) temptresource.get("channel");
			if(temptUri.equals(uri) && channel.equals(temptChannel)){
				match= true;
				matchNo = i;
			}
		}
		
		if(match){
			JSONObject matchresource = (JSONObject)temptResourceList.get(matchNo);
			response.put("response", "success");
			
			//The resourceSize were not added into the response
			matchresource.replace("owner", !owner.equals("")?"*":"");
			if(this.advertisedhostname != ""){
				matchresource.replace("ezserver", this.advertisedhostname+":"+this.port);
			}else{
				matchresource.replace("ezserver", this.ip+":"+this.port);
			}
			int size = 0;
			matchresource.put("resourceSize",size);
			responseArray.add(matchresource);
			return responseArray;
			
		}else{
			response.put("response", "error");
			response.put("errorMessage", "invalid resourceTemplate");
			responseArray.add(response);
			return responseArray;
		}
	}
	
	
	
	
	private synchronized void serverInteraction(){
//		System.out.println("only for test successful exchange");
		JSONObject interactionCommand = new JSONObject();
		interactionCommand.put("command", "EXCHANGE");
		interactionCommand.put("serverList", this.serverList);
		//the automatic interaction
		if(this.serverList.size() !=0 ){
					
			java.util.Random random=new java.util.Random();
			int choosenNo =random.nextInt(this.serverList.size());				
			JSONObject selectedServer = (JSONObject) this.serverList.get(choosenNo);
			String temptip = (String) selectedServer.get("hostname");
			int temptport =  Integer.parseInt(selectedServer.get("port").toString());
			if(!(temptip.equals(this.ip)&&temptport==this.port)){
				try(Socket socket = new Socket(temptip,temptport)){
					DataInputStream input = new DataInputStream(socket.getInputStream());
					DataOutputStream output = new DataOutputStream(socket.getOutputStream());
					output.writeUTF(interactionCommand.toJSONString());
					output.flush();
					socket.close();
				}catch (UnknownHostException e) {
					// TODO Auto-generated catch block
							
					//if the host is not connected, then remove the server from the list
					e.printStackTrace();
					this.serverList.remove(choosenNo);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}		
		}
	}
	private synchronized void runInteraction(int exchangeinterval){
		Timer   timer   =   new   Timer();
		timer.schedule(new  TimerTask(){

			@Override
			public void run() {
				serverInteraction();
				
			}
			
		},   0,   1000*exchangeinterval);
		
	}
	
	@SuppressWarnings("unchecked")
	private synchronized JSONObject dealingExchange(JSONObject command) {
		JSONObject response = new JSONObject();
		JSONArray tempServerList = (JSONArray) command.get("serverList");
	//	System.out.println("only for test successful exchange");
		
		
		if (tempServerList == null) {
			response.put("response", "error");
			response.put("errorMessage", "missing or invalid server list");
			return response;
		}
		if (tempServerList.size() == 0){
			response.put("response", "error");
			response.put("errorMessage", "missing resourceTemplate");
			return response;
		}
		//TODO
		//try connect, if exception, return error response
		
/*
		//inform other servers
//		if(!command.containsKey("fromServer")){
//			
//			for(int i = 0; i < this.serverList.size(); i++){
//				JSONObject temptServer = (JSONObject) this.serverList.get(i);
//				String temptip = (String) temptServer.get("hostname");
//				int temptport =  Integer.parseInt(temptServer.get("port").toString());
//				if(!(temptip.equals(this.ip)&&temptport==this.port)){
//					try(Socket socket = new Socket(temptip,temptport)){
//						DataInputStream input = new DataInputStream(socket.getInputStream());
//						DataOutputStream output = new DataOutputStream(socket.getOutputStream());
//						command.put("fromServer", true);
//						output.writeUTF(command.toJSONString());
//						output.flush();
//						socket.close();
//					}catch (UnknownHostException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (Exception e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
//			}
//		}
 * 
 */
		//add to serverlist
		for(int i = 0; i < tempServerList.size(); i++){
			boolean duplicationServer = false;
			JSONObject tempServer = (JSONObject) tempServerList.get(i);
			String hostName = (String) tempServer.get("hostname");
			Long port = (Long) tempServer.get("port");
			
			for(int j = 0; j < this.serverList.size(); j++){
				JSONObject serverInList = (JSONObject) this.serverList.get(j);
				String serverInListhostName = (String) serverInList.get("hostname");
				int serverInListport = Integer.parseInt(serverInList.get("port").toString());
				if(serverInListhostName.equals(hostName) && port.intValue() == serverInListport){
					duplicationServer = true;
				}
			}
			if(!duplicationServer){
				this.serverList.add(tempServer);
			}
		}
		//TODO
		//transfer serverlist to the new servers, using exchange command.
		
		response.put("response", "success");
		return response;
	}
	//This is a method dealing with invalid command
	@SuppressWarnings({ "unchecked" })
	private synchronized static JSONObject invalidcommand(JSONObject command) {
		JSONObject response = new JSONObject();
		response.put("response", "error");
		response.put("errorMessage", "invalid command");
		return response;
		
		
	}
	
	
	@SuppressWarnings("unchecked")
	private JSONArray mergeResponse(JSONArray remoResponseList, JSONArray localResponseArray){
		//filt error response form other server
		JSONArray successResponse = new JSONArray();
		for(int i =0; i< remoResponseList.size();i++){
			JSONObject tempResponse = (JSONObject) remoResponseList.get(i);
			if(tempResponse.containsKey("response")){
				if(tempResponse.get("response").toString().equals("success")){
					successResponse.add(tempResponse);
				}
			}
			else{
				successResponse.add(tempResponse);
			}
			
		}
		// merge local response and remote success response
		JSONArray finalResponseArray = new JSONArray();
		if(successResponse.isEmpty()){
			finalResponseArray = localResponseArray;
		}else{
			JSONObject finalResponse = new JSONObject();
			finalResponse.put("response", "success");
			finalResponseArray.add(finalResponse);
			int resultSize = 0;
			// remote response
			for(int i = 0; i < successResponse.size(); i++){
				JSONObject tempResponse = (JSONObject)successResponse.get(i);
				if(!tempResponse.containsKey("response")){
					if(tempResponse.containsKey("resultSize")){
						resultSize += (int)tempResponse.get("resultSize");
					}
					else{       //tempResponse is a resource
						finalResponseArray.add(tempResponse);
					}
				}
			}
			// local response
			JSONObject localFinalResponse = (JSONObject) localResponseArray.get(0);
			if(localFinalResponse.get("response").equals("success")){
				for(int i = 1; i< localResponseArray.size(); i++){
					JSONObject tempResponse = (JSONObject)localResponseArray.get(i);
					if(tempResponse.containsKey("resultSize")){
						resultSize += (int)tempResponse.get("resultSize");
					}
					else{       //tempResponse is a resource
						finalResponseArray.add(tempResponse);
					}
				}
			}  //else error
			
			JSONObject resultSizeJSON = new JSONObject();
			resultSizeJSON.put("resultSize", resultSize);
			finalResponseArray.add(resultSizeJSON);
		}
		return finalResponseArray;
	}
	
	

	
	//this function can only be used if the local query "success"
	@SuppressWarnings("unchecked")
	private JSONArray transferQueryCommand(String ip, int port, JSONObject command){
		JSONArray responseArray = new JSONArray();
		JSONParser parser = new JSONParser();
		try(Socket socket = new Socket(ip,port)){
			DataInputStream input = new DataInputStream(socket.getInputStream());
			DataOutputStream output = new DataOutputStream(socket.getOutputStream());
			output.writeUTF(command.toJSONString());
			output.flush();
			boolean inputEnd = false;
			while(!inputEnd){
				if(input.available() > 0){
					JSONObject response = (JSONObject) parser.parse(input.readUTF());
					responseArray.add(response);
					if(response.containsKey("resultSize")){
						inputEnd = true;
					}
				}
			}
		}catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return responseArray;
	}
}
