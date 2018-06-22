package EZshare;

import java.net.URISyntaxException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.json.simple.JSONObject;

public class Test {

	@SuppressWarnings({ "unchecked" })
	public static void main(String[] args) throws URISyntaxException, ParseException {
		try{
			// TODO Auto-generated method stub
			Options option = new Options();
			//System.out.println(str.replace("\0",""));
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
			command.put("command", "PUBLISH");
			
			ArgumentParser ap = new ArgumentParser();
			Resource resource = ap.setResource(commandLine);
			
			command.put("resource",resource.toJSONObject());
			
			//System.out.println(command.toJSONString());;
			System.out.println(command);
		}catch(MissingArgumentException e){
			e.printStackTrace();
		}catch(Exception e1){
			e1.printStackTrace();
		}
		
	}

}
