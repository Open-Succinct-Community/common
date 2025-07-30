package in.succinct.json;

import com.venky.core.util.ObjectUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

public class JSONComm {
    private static volatile JSONComm sSoleInstance;
    
    //private constructor.
    private JSONComm() {
        //Prevent form the reflection api.
        if (sSoleInstance != null) {
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
    }
    
    public static JSONComm getInstance() {
        if (sSoleInstance == null) { //if there is no instance available... create new one
            synchronized (JSONComm.class) {
                if (sSoleInstance == null) sSoleInstance = new JSONComm();
            }
        }
        
        return sSoleInstance;
    }
    
    //Make singleton from serialize and deserialize operation.
    protected JSONComm readResolve() {
        return getInstance();
    }
    
    /* Compare slightly differnt jsons to get precise difference"
    
     */
    public JSONAware subtract(JSONAware from, JSONAware what){
        if (what == null || from == null){
            return from;
        }else if (from.getClass() != what.getClass()){
            return from;
        }else if ( from instanceof  JSONObject){
            JSONObject result = new JSONObject();
            subtract((JSONObject) from,(JSONObject) what, result);
            return result;
        }else if (from instanceof JSONArray){
            JSONArray result = new JSONArray();
            subtract((JSONArray) from,(JSONArray) what, result);
            return result;
        }else {
            throw new RuntimeException("Unknown JSONAware type found!");
        }
        
    }
    public void subtract(JSONObject from, JSONObject what, JSONObject result){
        from.forEach((k1,v1)->{
            Object v2 = what.get(k1);
            if (v1 == v2){
                //Both null will go here.
                return;
            }else if (v1 == null || v2 == null){
                result.put(k1,v1);
            }else if (v1.getClass() != v2.getClass()){
                result.put(k1,v1);
            }else {
                //V1 and v2 are same class
                if (!(v1 instanceof JSONAware)){
                    if (!ObjectUtil.equals(v1,v2)){
                        result.put(k1,v1);
                    }
                }else {
                    if (v1 instanceof JSONObject){
                        result.put(k1,new JSONObject());
                        subtract((JSONObject) v1,(JSONObject) v2,(JSONObject) result.get(k1));
                        if (((JSONObject) result.get(k1)).isEmpty()){
                            result.remove(k1);
                        }
                    }else if (v1 instanceof JSONArray){
                        result.put(k1,new JSONArray());
                        subtract((JSONArray) v1,(JSONArray) v2,(JSONArray) result.get(k1));
                        if (((JSONArray) result.get(k1)).isEmpty()){
                            result.remove(k1);
                        }
                    }else {
                        throw new RuntimeException("Unknown JSONAware type found!");
                    }
                }
            }
        });
        
    }
    public void subtract(JSONArray from, JSONArray what, JSONArray result){
        for (int i = 0  ; i < from.size() ; i ++){
            Object o1 = from.get(i);
            if (i < what.size() ){
                Object o2 = what.get(i);
                if (o1 == o2){
                    continue;
                }else if (o1 == null || o2 == null){
                    result.add(o1);
                }else if (o1.getClass() != o2.getClass()){
                    result.add(o1);
                }else {
                    if (!(o1 instanceof JSONAware)){
                        if (!ObjectUtil.equals(o1,o2)){
                            result.add(o1);
                        }
                    }else if (o1 instanceof JSONObject){
                        JSONObject o = new JSONObject();
                        subtract((JSONObject) o1,(JSONObject) o2, o);
                        if (!o.isEmpty()) {
                            result.add(o);
                        }
                    }else if (o1 instanceof JSONArray){
                        JSONArray o = new JSONArray();
                        subtract((JSONArray) o1,(JSONArray) o2,o);
                        if (!o.isEmpty()) {
                            result.add(o);
                        }
                    }else {
                        
                        throw new RuntimeException("Unknown JSONAware type %s found!".formatted(o1.getClass().getName()));
                    }
                }
            }else {
                result.add(o1);
            }
        }
        
    }
}
