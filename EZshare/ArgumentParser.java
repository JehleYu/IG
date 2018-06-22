package EZshare;

import java.net.URISyntaxException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class ArgumentParser {
	@SuppressWarnings("unchecked")
	public JSONObject clientArgsParse(String[] args) throws ParseException, URISyntaxException{
		Options option = new Options();
		option.addOption("channel","channel",true,"channel" );
		option.addOption("debug","debug",false,"print debug information" );
		option.addOption("description","description",true,"resource description" );
		option.addOption("exchange","exchange",false,"exchange server list with server");
		option.addOption("fetch","fetch",false,"fetch resources from server");
		option.addOption("host","host",true,"server host, a domain name or a IP address");
		option.addOption("name","name",true,"resource name");
		option.addOption("owner","owner",true,"owner");
		option.addOption("port","port",true,"server port, an integer");
		option.addOption("publish","publish",false,"publish resource on server");
		option.addOption("query","query",false,"query for resources from server");
		option.addOption("remove","remoce",false,"remove resources from server");
		option.addOption("secret","secret",true,"secret");
		option.addOption("servers","servers",true,"server list,host1:port1,host2:port2,...");
		option.addOption("share","share",false,"share resource on server");
		option.addOption("tags","tags",true,"resource tags,tag1,tag2,tag3,...");
		option.addOption("uri","uri",true,"resource URI");
		
		CommandLineParser parser = new DefaultParser();
		CommandLine commandLine = parser.parse(option, args);
		JSONObject command = new JSONObject();
		
		if(commandLine.hasOption("publish")){
			command.put("command", "PUBLISH");
			command.put("resource",this.setResource(commandLine).toJSONObject());
		}
		
		if(commandLine.hasOption("remove")){
			command.put("command", "REMOVE");
			command.put("resource",this.setResource(commandLine).toJSONObject());
		}
		
		if(commandLine.hasOption("share")){
			command.put("command", "SHARE");
			command.put("secret", commandLine.hasOption("secret")?commandLine.getOptionValue("secret"):"");
			command.put("resource",this.setResource(commandLine).toJSONObject());
		}
		
		if(commandLine.hasOption("query")){
			command.put("command", "QUERY");
			command.put("relay", commandLine.hasOption("relay")?
					Boolean.parseBoolean(commandLine.getOptionValue("relay")):false);
			command.put("resourceTemplate",this.setResource(commandLine).toJSONObject());
		}
		
		if(commandLine.hasOption("fetch")){
			command.put("command", "FETCH");
			command.put("resourceTemplate",this.setResource(commandLine).toJSONObject());
		}
		
		if(commandLine.hasOption("exchange")){
			command.put("command", "EXCHANGE");
			JSONArray serverJSONArray = new JSONArray();
			if(commandLine.hasOption("servers")){
				String servers = commandLine.getOptionValue("servers");
				String[] serverList = servers.split(",");
				for(int i = 0; i<serverList.length ; i++){
					JSONObject singleServer = new JSONObject();
					singleServer.put("hostname", serverList[i].split(":")[0]);
					singleServer.put("port", Integer.parseInt(serverList[i].split(":")[1]));
					serverJSONArray.add(singleServer);
				}
				command.put("serverList",serverJSONArray);
			}else{
				command.put("serverList",serverJSONArray);
			}
		}
		
		command.put("debug", commandLine.hasOption("debug"));
		if(commandLine.hasOption("host")){
			command.put("host", commandLine.getOptionValue("host"));
		}
		if(commandLine.hasOption("port")){
			command.put("port", Integer.parseInt(commandLine.getOptionValue("port")));
		}
		return command;
	}
	
	
	public Resource setResource(CommandLine commandLine) throws MissingArgumentException{
		Resource resource = new Resource();
		
		resource.setName(commandLine.hasOption("name")?commandLine.getOptionValue("name"):"");
		resource.setTags(commandLine.hasOption("tags")?commandLine.getOptionValue("tags"):"");
		resource.setDescription(commandLine.hasOption("description")?commandLine.getOptionValue("description"):"");
		resource.setUri(commandLine.hasOption("uri")?commandLine.getOptionValue("uri"):"");
		resource.setChannel(commandLine.hasOption("channel")?commandLine.getOptionValue("channel"):"");
		resource.setOwner(commandLine.hasOption("owner")?commandLine.getOptionValue("owner"):"");
		resource.setEzserver(commandLine.hasOption("ezserver")?commandLine.getOptionValue("ezserver"):null);

		return resource;
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject serverArgsParse(String[] args) throws ParseException{
		Options option = new Options();
		option.addOption("a","advertisedhostname",true,"channel" );
		option.addOption("c","connectionintervallimit",true,"print debug information" );
		option.addOption("e","exchangeinterval",true,"resource description" );
		option.addOption("p","port",true,"exchange server list with server");
		option.addOption("s","secret",true,"fetch resources from server");
		option.addOption("d","debug",false,"server host, a domain name or a IP address");
		
		CommandLineParser parser = new DefaultParser();
		CommandLine commandLine = parser.parse(option, args);
		JSONObject argsJSON = new JSONObject();
		
		if(commandLine.hasOption("advertisedhostname")){
			argsJSON.put("advertisedhostname",commandLine.getOptionValue("advertisedhostname"));
		}
		if(commandLine.hasOption("connectionintervallimit")){
			argsJSON.put("connectionintervallimit",commandLine.getOptionValue("connectionintervallimit"));
		}
		if(commandLine.hasOption("exchangeinterval")){
			argsJSON.put("exchangeinterval",commandLine.getOptionValue("exchangeinterval"));
		}
		if(commandLine.hasOption("port")){
			argsJSON.put("port",commandLine.getOptionValue("port"));
		}
		if(commandLine.hasOption("secret")){
			argsJSON.put("secret",commandLine.getOptionValue("secret"));
		}
		if(commandLine.hasOption("debug")){
			argsJSON.put("debug",commandLine.getOptionValue("debug"));
		}
		return argsJSON;
	}
}
