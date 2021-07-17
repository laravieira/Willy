package me.laravieira.willy.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.error.YAMLException;

public class Yaml {
	public Map<String, Object> yamlMap = null;
	public List<Object> yamlList = null;
	public Object yamlObject = null;
	
	@SuppressWarnings("unchecked")
	public Yaml(File file) throws YAMLException, FileNotFoundException {
		if(file.exists() && file.isFile())
			yamlObject = new org.yaml.snakeyaml.Yaml().load(new FileReader(file));
		if(yamlObject instanceof List<?>)
			yamlList = (List<Object>) yamlObject;
		if(yamlObject instanceof Map<?,?>)
			yamlMap = (Map<String, Object>) yamlObject;
	}

	@SuppressWarnings("unchecked")
	public Yaml(Object object) throws YAMLException {
		yamlObject = object;
		if(yamlObject instanceof List<?>)
			yamlList = (List<Object>)yamlObject;
		if(yamlObject instanceof Map<?,?>)
			yamlMap = (Map<String, Object>)yamlObject;
	}
	
	@SuppressWarnings("unchecked")
	public Yaml(String data) throws YAMLException {
		yamlObject = new org.yaml.snakeyaml.Yaml().load(data);
		if(yamlObject instanceof List<?>)
			yamlList = (List<Object>)yamlObject;
		if(yamlObject instanceof Map<?,?>)
			yamlMap = (Map<String, Object>)yamlObject;
	}
	
	public boolean has(String key) {
		if(yamlMap == null) return false;
		if(yamlMap.containsKey(key)) {
			if(yamlMap.get(key) == null)
				return false;
			return true;
		}else return false;
	}
	
	public boolean isMap() {
		if(yamlMap == null)
			return false;
		return true;
	}
	
	public boolean isList() {
		if(yamlList == null)
			return false;
		return true;
	}

	public boolean isNull() {
		if(yamlObject == null)
			return true;
		return false;
	}
	
	public boolean isString() {
		if(yamlObject instanceof String)
			return true;
		return false;
	}

	public boolean isBoolean() {
		if(yamlObject instanceof Boolean)
			return true;
		return false;
	}

	public boolean isInt() {
		if(yamlObject instanceof Integer)
			return true;
		return false;
	}

	public boolean isLong() {
		if(yamlObject instanceof Long)
			return true;
		return false;
	}

	public boolean isDouble() {
		if(yamlObject instanceof Double)
			return true;
		return false;
	}

	public Object getObject(String key) {
		if(yamlMap == null) return null;
		return yamlMap.get(key);
	}

	public Yaml get(String key) {
		if(yamlMap == null) return null;
		return new Yaml(yamlMap.get(key));
	}
	
	public Object asObject() {
		return yamlObject;
	}
	
	public Map<String, Object> asObjectMap() {
		return yamlMap;
	}
	
	public Map<String, Yaml> asMap() {
		Map<String, Yaml> map = new HashMap<String, Yaml>();
		for(Map.Entry<String, Object> entry : yamlMap.entrySet())
			map.put(entry.getKey(), new Yaml(entry.getValue()));
		return map;
	}
	
	public List<Object> asObjectList() {
		return yamlList;
	}

	public List<String> asStringList() {
		List<String> list = new ArrayList<String>();
		for(Object value : yamlList)
			if(value instanceof String)
				list.add((String)value);
		return list;
	}

	public List<Boolean> asBooleanList() {
		List<Boolean> list = new ArrayList<Boolean>();
		for(Object value : yamlList)
			if(value instanceof Boolean)
				list.add((Boolean)value);
		return list;
	}

	public List<Integer> asIntList() {
		List<Integer> list = new ArrayList<Integer>();
		for(Object value : yamlList)
			if(value instanceof Integer)
				list.add((Integer)value);
		return list;
	}

	public List<Long> asLongList() {
		List<Long> list = new ArrayList<Long>();
		for(Object value : yamlList)
			if(value instanceof Long)
				list.add((Long)value);
		return list;
	}

	public List<Double> asDoubleList() {
		List<Double> list = new ArrayList<Double>();
		for(Object value : yamlList)
			if(value instanceof Double)
				list.add((Double)value);
		return list;
	}
	
	public String asString() {
		if(yamlObject instanceof String)
			return (String)yamlObject;
		return null;
	}
	
	public Boolean asBoolean() {
		if(yamlObject instanceof Boolean)
			return (Boolean)yamlObject;
		return null;
	}

	public Integer asInt() {
		if(yamlObject instanceof Integer)
			return (Integer)yamlObject;
		return null;
	}

	public Long asLong() {
		if(yamlObject instanceof Long)
			return (Long)yamlObject;
		return null;
	}
	
	public Double asDouble() {
		if(yamlObject instanceof Double)
			return (Double)yamlObject;
		return null;
	}
}

