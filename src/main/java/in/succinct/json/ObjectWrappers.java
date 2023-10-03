package in.succinct.json;

import org.json.simple.JSONArray;

import java.lang.reflect.ParameterizedType;
import java.util.Comparator;
import java.util.Iterator;

@SuppressWarnings("unchecked")
public class ObjectWrappers<T> extends JSONAwareWrapper<JSONArray> implements Iterable<T>{
    private final Class<T> clazz ;
    public Class<T> getElementType(){
        return clazz;
    }
    protected ObjectWrappers(JSONArray value) {
        super(value);
        ParameterizedType pt = (ParameterizedType)getClass().getGenericSuperclass();
        this.clazz = (Class<T>) pt.getActualTypeArguments()[0];
    }

    protected ObjectWrappers(String payload) {
        super(payload);
        ParameterizedType pt = (ParameterizedType)getClass().getGenericSuperclass();
        this.clazz = (Class<T>) pt.getActualTypeArguments()[0];
    }
    public T get(int index){
        return  super.get(index,clazz);
    }

    public int size(){
        return getInner().size();
    }
    public boolean isEmpty(){
        return getInner().isEmpty();
    }

    @Override
    @SuppressWarnings("all")
    public boolean equals(Object o) {
        if (o == null){
            return false;
        }
        if (!(o instanceof ObjectWrappers)){
            return false;
        }
        return super.equals(o) && (((ObjectWrappers)o).clazz == clazz);
    }


    @Override
    public Iterator<T> iterator() {
        return super.iterator(clazz);
    }

    public T min(Comparator<T> comparator){
        return super.min(comparator,clazz);
    }
}
