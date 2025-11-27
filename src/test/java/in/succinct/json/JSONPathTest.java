package in.succinct.json;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;

public class JSONPathTest {
    @Test
    public void test(){
        JSONAwareWrapper<JSONObject> w = new JSONObjectWrapper(new JSONObject(){{
            put("a", new JSONObject(){{
                put("b",new JSONArray(){{
                    add(1);
                    add(2);
                }});
                put("c",new JSONArray(){{
                    add("d");
                }});
            }});
            
        }});
        Assert.assertEquals(w.getPathValue("a.b[0]"),Integer.valueOf("1"));
        Assert.assertEquals(w.getPathValue("a.b[1]"),Integer.valueOf("2"));
        Assert.assertNull(w.getPathValue("a.b[2]"));
        Assert.assertThrows(NumberFormatException.class, () -> {
            w.getPathValue("a.b.c");
        });
        Assert.assertTrue((int)w.getPathValue("a.b[0]") > 0);
        Assert.assertEquals(w.getPathValue("a.c[0]"),"d");
        Assert.assertEquals(w.getPathValue("a.c.0"),"d");
        Assert.assertNull(w.getPathValue("a.c.2"));
        System.out.println((Object)w.getPathValue("."));
    }
}
