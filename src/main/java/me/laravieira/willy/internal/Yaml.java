package me.laravieira.willy.internal;

import org.jetbrains.annotations.NotNull;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;

import java.io.*;
import java.util.*;

@SuppressWarnings("unused")
public class Yaml {
	private final Map<String, Object> yaml = new HashMap<>();

	@SuppressWarnings("unchecked")
	Yaml(String path, boolean resource) {
		try {
			InputStream stream;
			if(resource)
				stream = Objects.requireNonNull(Yaml.class.getClassLoader().getResource(path)).openStream();
			else
				stream = new FileInputStream(path);
			Load load = new Load(LoadSettings.builder().build());
			if(load.loadFromInputStream(stream) instanceof Map map)
				yaml.putAll(map);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	Yaml(Object object) {
		if(object instanceof Map map)
			yaml.putAll(map);
	}

	private Object parse(@NotNull String path) {
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

	public boolean has(@NotNull String path) {
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

	public List<Object> asList(String keyword) {
		if(parse(keyword) instanceof List list)
			return new ArrayList<Object>(list);
		return new ArrayList<>();
	}

	@SuppressWarnings("unchecked")
	public List<String> getStringList(String keyword) {
		if(parse(keyword) instanceof List list)
			return new ArrayList<String>(list);
		return new ArrayList<>();
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
