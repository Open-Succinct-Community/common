package in.succinct.json;

import org.json.simple.JSONObject;

public class JSONObjectWrapper extends JSONAwareWrapper<JSONObject> {
    protected JSONObjectWrapper(JSONObject value) {
        super(value);
    }

    protected JSONObjectWrapper(String payload) {
        super(payload);
    }
}
