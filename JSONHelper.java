package helpers;

import org.json.simple.JSONObject;
import helpers.Type;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JSONHelper {

	// jsonString is used tp Stringify the JSON object that uses name,key and value
	// to retrieve
	public static String parse(String jsonString, String key) {
		JSONParser parser = new JSONParser();
		try {
			JSONObject jsonObject = (JSONObject) parser.parse(jsonString);
			return jsonObject.get(key).toString();
		} catch (ParseException e) {
			return null;
		}
	}

	// this is an overloaded method which uses type, ip, port, messsage and returns
	// the JSON objected wrapped.
	@SuppressWarnings("unchecked")
	public static JSONObject makeJson(Type type, String ip, int port, String message) {
		JSONObject jsonObject = makeJson(type, ip, port);
		jsonObject.put("message", message);
		return jsonObject;
	}

	// this is an overloaded method which uses type, ip, port, messsage and returns
	// the JSON objected wrapped.
	@SuppressWarnings("unchecked")
	public static JSONObject makeJson(Type type, String ip, int port) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("type", type.name());
		jsonObject.put("ip", ip);
		jsonObject.put("port", port);
		return jsonObject;
	}
}
