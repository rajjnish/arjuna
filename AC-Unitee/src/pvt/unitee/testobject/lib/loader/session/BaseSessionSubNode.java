package pvt.unitee.testobject.lib.loader.session;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gson.JsonObject;

import arjunasdk.console.Console;
import pvt.batteries.hocon.HoconReader;
import pvt.batteries.hocon.HoconStringReader;
import pvt.batteries.value.DefaultStringKeyValueContainer;
import pvt.unitee.arjuna.ArjunaInternal;
import pvt.unitee.runner.lib.slots.TestSlotExecutor;
import pvt.unitee.testobject.lib.loader.group.Group;

public class BaseSessionSubNode implements SessionSubNode{
	private List<Group> groupsQueue = new ArrayList<Group>();
	private int testMethodCount = 0;
	private int groupThreads = 1;
	private int id;
	private Iterator<Group> iter = null;
	private Group group = null;
	private Session session = null;
	private SessionNode sessionNode = null;
	private String name = null;
	private DefaultStringKeyValueContainer utvars = new DefaultStringKeyValueContainer();
	
	private BaseSessionSubNode(SessionNode sessionNode, int id) throws Exception{
		this.session = sessionNode.getSession();
		this.sessionNode = sessionNode;
		this.id = id;
		this.name = this.sessionNode.getName() + "-" + this.id;
		this.utvars.cloneAdd(sessionNode.getUTV().items());
	}
	
	public BaseSessionSubNode(SessionNode sessionNode, int id, Group group) throws Exception{
		this(sessionNode, id);
		this.group = group;
		this.name += "-" + group.getName();
	}
	
	public BaseSessionSubNode(SessionNode sessionNode, int id, String groupName) throws Exception{
		this(sessionNode, id);
		this.group = ArjunaInternal.getGroupLoader().getGroup(this, groupName);
		this.group.setSessionName(this.getSession().getName());
		this.name += "-" + group.getName();
	}
	
	public BaseSessionSubNode(SessionNode sessionNode, int id, JsonObject groupObj) throws Exception{
		this(sessionNode, id);
		try{
			JsonObject utv = groupObj.getAsJsonObject("utv");
			HoconReader utvReader = new HoconStringReader(utv.toString());
			utvReader.process();
			this.utvars.add(utvReader.getProperties());
		} catch (ClassCastException e){
			this.errorUtvNotObject();
		} catch (NullPointerException e){
			// do nothing
		}
		String groupName = null;
		try{ 
			groupName = groupObj.getAsJsonPrimitive("name").getAsString();
		} catch (Exception e){
			existAsNameIsNotString();
		}
		this.group = ArjunaInternal.getGroupLoader().getGroup(this, groupName);
		this.group.setSessionName(this.getSession().getName());
		this.name += "-" + group.getName();
	}
	
	private void errorUtvNotObject(){
		Console.displayError(
				String.format(
						">>utv<< attribute in session sub node definition should be a JSON object. Fix session template file: >>%s<<",
						session.getSessionFilePath()
				));			
		Console.displayError("Exiting...");
		System.exit(1);			
}
	
	private void existAsNameIsNotString(){
		Console.displayError(
				String.format(
						">>name<< attribute in sub node definition is mandatory, must be supplied as non-null, string value. Fix session template file: >>%s<<",
						session.getSessionFilePath()
				));			
		Console.displayError("Exiting...");
		System.exit(1);			
	}
	
	public void load() throws Exception{
		group.load();
		this.testMethodCount = group.getTestMethodCount();
	}
	
	public Group getGroup(){
		return this.group;
	}

	@Override
	public int getTestMethodCount() {
		return this.testMethodCount;
	}

	@Override
	public int getId() {
		return this.id;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public TestSlotExecutor next() throws Exception {
		return group.next();
	}

	@Override
	public Session getSession() {
		return this.session;
	}
	
	@Override
	public SessionNode getSessionNode() {
		return this.sessionNode;
	}
	@Override
	public DefaultStringKeyValueContainer getUTV() {
		return this.utvars;
	}
 
}