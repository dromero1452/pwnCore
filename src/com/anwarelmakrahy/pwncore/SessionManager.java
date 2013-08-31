package com.anwarelmakrahy.pwncore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.anwarelmakrahy.pwncore.ConsoleSession.ConsoleSessionParams;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SessionManager {

	private Context context;
	
	private int incConsoleIDs = 1000;
	private Map<String, ConsoleSession> consoleSessions = new HashMap<String, ConsoleSession>();
	
	private String currentConsoleWindowId = null;
	
	SessionManager(Context context) {
		this.context = context;
	}
	
	public ConsoleSession getNewConsole() {		
		String id = Integer.toString(incConsoleIDs++);
		ConsoleSession newConsole = new ConsoleSession(context, id);
		consoleSessions.put(id, newConsole);			
		Intent tmpIntent = new Intent();
		tmpIntent.setAction(StaticsClass.PWNCORE_CONSOLE_CREATE);
		tmpIntent.putExtra("id", id);
		context.sendBroadcast(tmpIntent);		
		return newConsole;
	}
	
	public ConsoleSession getNewConsole(ConsoleSessionParams params) {			
		String id = Integer.toString(incConsoleIDs++);
		ConsoleSession newConsole = new ConsoleSession(
				context, 
				id,
				params);
		
		consoleSessions.put(id, newConsole);			
		Intent tmpIntent = new Intent();
		tmpIntent.setAction(StaticsClass.PWNCORE_CONSOLE_CREATE);
		tmpIntent.putExtra("id", id);
		context.sendBroadcast(tmpIntent);		
		return newConsole;
	}
	
	public void notifyNewConsole(String id, String msfId, String prompt) {
		if (consoleSessions.containsKey(id)) {
			consoleSessions.get(id).setPrompt(prompt);
			consoleSessions.get(id).setMsfId(msfId);
			
			if (ConsolesFragment.mConsolesListAdapter != null) {
				ConsolesFragment.consoleArray.clear();
				ConsolesFragment.consoleArray.addAll(getConsoleListArray());
				ConsolesFragment.mConsolesListAdapter.notifyDataSetChanged();
			}
		}
	}
	
	public void notifyConsoleWrite(String id) {
		if (consoleSessions.containsKey(id)) {
			consoleSessions.get(id).pingReadListener();
		}
	}
	
	public void notifyConsoleNewRead(String id, String data, String prompt, boolean busy) {
		if (consoleSessions.containsKey(id)) {
			consoleSessions.get(id).newRead(data, prompt, busy);	
		}
	}
	
	public void notifyDestroyedConsole(String Id, String msfId) {
		if (consoleSessions.containsKey(Id))
			consoleSessions.remove(Id);
	}
	
	public void switchConsoleWindow(String id, Activity activity) {
		if (consoleSessions.containsKey(id)) {
			ConsoleSession c = consoleSessions.get(id);
			
			if (currentConsoleWindowId != null)
				consoleSessions.get(currentConsoleWindowId).setWindowActive(false, null);
		
			c.setWindowActive(true, activity);
			currentConsoleWindowId = id;
		}
	}
	
	public void notifyJobCreated(String id) {
		Log.d("notifyJobCreated", id);
	}
	
	public void closeConsoleWindow(String id) {
		if (consoleSessions.containsKey(id)) {
			consoleSessions.get(id).setWindowActive(false, null);
			currentConsoleWindowId = null;
		}
	}

	public void destroyConsole(ConsoleSession c) {
		c.destroy();
		if (currentConsoleWindowId == c.getId())
			currentConsoleWindowId = null;
		consoleSessions.remove(c.getId());
		
		if (ConsolesFragment.mConsolesListAdapter != null) {
			ConsolesFragment.consoleArray.clear();
			ConsolesFragment.consoleArray.addAll(getConsoleListArray());
			ConsolesFragment.mConsolesListAdapter.notifyDataSetChanged();
		}
	}
	
	public ConsoleSession getConsole(String id) {
		if (id == null) return null;
		if (consoleSessions.containsKey(id)) {
			return consoleSessions.get(id);
		}
		return null;
	}
	
	public List<String> getConsoleListArray() {
		List<String> list = new ArrayList<String>();
		
		for (int i=0; i<consoleSessions.size(); i++)
			list.add("[" + consoleSessions.get(consoleSessions.keySet().
					toArray()[i].toString()).getId() + "] " + 
					consoleSessions.get(consoleSessions.keySet().
							toArray()[i].toString()).getTitle());

		return list;
	}
}
