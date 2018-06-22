package EZshare;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Resource {
	private String name="";
	private String description="";
	private String tags="";
	private String uri="";
	private String channel="";
	private String owner="";
	private String ezserver=null;
	
/*
	public Resource(String name, String description, String[] tags, URI uri, String channel, String owner,
			String ezsever) {
		super();
		try{
			this.name = pureStr(name);
			this.description = pureStr(description);
			for(int i = 0; i<tags.length;i++){
				this.tags[i] = pureStr(tags[i]);
			}
			this.uri = uri;
			this.channel = pureStr(channel);
			if(owner == "*"){
				this.owner = "";
			}else{
				this.owner = pureStr(owner);
			}
			this.ezsever = pureStr(ezsever);
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
	}
*/		
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getTags() {
		return tags;
	}
	public void setTags(String tags) {
	//	for(int i = 0; i<tags.length;i++){
		this.tags = tags;
	//	}
	}
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
		
	}
	public String getChannel() {
		return channel;
	}
	public void setChannel(String channel) {
		this.channel = channel;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public String getEzserver() {
		return ezserver;
	}
	public void setEzserver(String ezserver) {
		this.ezserver = ezserver;
	}

	@SuppressWarnings("unchecked")
	public JSONObject toJSONObject(){
		JSONObject resJSON = new JSONObject();
		resJSON.put("name", this.name);
		
//		String[] tagsList = this.tags.split(",");
//		JSONArray tagsArray = new JSONArray();
//		for(int i = 0; i<tagsList.length; i++){
//			tagsArray.add(i, tagsList[i]);
//		}
//		resJSON.put("tags", tagsArray);
		JSONArray tagsArray = new JSONArray();
		if(!this.tags.equals("")){
			String[] tagsList = this.tags.split(",");
			for(int i = 0; i<tagsList.length; i++){
				tagsArray.add(i, tagsList[i]);
			}
			resJSON.put("tags", tagsArray);
		}
		else{
			resJSON.put("tags", tagsArray);
		}
		
		
		resJSON.put("description", this.description);
		resJSON.put("uri", this.uri);
		resJSON.put("channel", this.channel);
		resJSON.put("owner", this.owner);
		resJSON.put("ezserver", this.ezserver);
		return resJSON;
	}

	
}
