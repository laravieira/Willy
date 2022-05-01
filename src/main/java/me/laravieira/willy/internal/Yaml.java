package me.laravieira.willy.internal;

import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;

import java.io.*;
import java.util.*;

public class Yaml {
	private final Map<String, Object> yaml = new HashMap<>();

	Yaml(String path, boolean resource) {
		try {
			InputStream stream;
			if(resource)
				stream = Objects.requireNonNull(Yaml.class.getClassLoader().getResource(path)).openStream();
			else
				stream = new FileInputStream(path);
			Load load = new Load(LoadSettings.builder().build());
			yaml.putAll((Map<String, Object>)load.loadFromInputStream(stream));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	Yaml(Object object) {
		yaml.putAll((Map<String, Object>)object);
	}

	private Object parse(String path) {
		String[] keywords = path.split("\\.");
		Object object = this.yaml;
		for(String keyword : keywords) {
			if(object instanceof Map) {
				Yaml yaml = new Yaml(object);
				object = yaml.has(keyword)?yaml.asObject(keyword):object;
			}
		}
		return object;
	}

	public boolean has(String path) {
		String[] keywords = path.split("\\.");
		Object object = this.yaml;
		boolean hasKey = false;
		for(String keyword : keywords) {
			if(object instanceof Map) {
				if(((Map<?, ?>) object).containsKey(keyword)) {
					object = ((Map<?, ?>) object).get(keyword);
					hasKey = true;
				}else
					hasKey = false;
			}
		}
		return hasKey;
	}

	public Object asObject(String keyword) {
		return yaml.get(keyword);
	}
	public Yaml get(String keyword) {
		return new Yaml(parse(keyword));
	}
	public String asString(String keyword) {
		return (String)parse(keyword);
	}
	public int asInt(String keyword) {
		return (int)parse(keyword);
	}
	public long asLong(String keyword) {return Long.parseLong(""+parse(keyword));}
	public float asFloat(String keyword) {
		return (float)parse(keyword);
	}
	public boolean asBoolean(String keyword) {
		return (boolean)parse(keyword);
	}
	public List asList(String keyword) {
		return (List)parse(keyword);
	}

	public boolean isString(String keyword) {
		return parse(keyword) instanceof String;
	}
	public boolean isInt(String keyword) {
		return parse(keyword) instanceof Integer;
	}
	public boolean isLong(String keyword) {
		return parse(keyword) instanceof Long;
	}
	public boolean isFloat(String keyword) {
		return parse(keyword) instanceof Float;
	}
	public boolean isBoolean(String keyword) {
		return parse(keyword) instanceof Boolean;
	}
	public boolean isList(String keyword) {
		return parse(keyword) instanceof List;
	}
}
