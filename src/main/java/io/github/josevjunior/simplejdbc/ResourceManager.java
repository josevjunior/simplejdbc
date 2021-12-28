
package io.github.josevjunior.simplejdbc;

import java.util.HashMap;
import java.util.Map;

public class ResourceManager {
    
    private static ThreadLocal<Map<Object, Object>> resources = new ThreadLocal(){
        protected Object initialValue() {
            return new HashMap<Object, Object>();
        }
    };
    
    private static final ResourceManager INSTANCE = new ResourceManager();

    public static ResourceManager getInstance() {
        return INSTANCE;
    }
    
    public static <T> T get(Object key, Class<T> clazz) {
        return (T) resources.get().get(key);
    }
    
    public static void put(Object key, Object resource) {
        resources.get().put(key, resource);
    }
    
    public static void remove(Object key) {
        resources.get().remove(key);
    }
    
    public static void clear() {
        resources.get().clear();
        resources.remove();
    }
    
}
