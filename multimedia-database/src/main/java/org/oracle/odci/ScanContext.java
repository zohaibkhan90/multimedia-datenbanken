package org.oracle.odci;

import java.util.HashMap;
import java.util.Map;

public class ScanContext {
	
	String key;

	Map<String, ContextData> contextMap = new HashMap<String, ContextData>();
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
	
	public ContextData get(String key) {
		return contextMap.get(key);
	}
	
	public void save(String key, ContextData contextData) {
		contextMap.put(key, contextData);
	}
	
	public void remove(String key) {
		contextMap.remove(key);
	}

}
