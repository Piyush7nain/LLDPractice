package singleton;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

public class Singleton implements Serializable {
    private String id;
    private static volatile Singleton instance;
    private Singleton(String id) {
        this.id = id;
    }

    // Double-Checked Locking
    public static Singleton getInstance(){
        if(instance == null){
            synchronized(Singleton.class){
                if(instance == null){
                    String id= UUID.randomUUID().toString();
                    instance = new Singleton(id);
                }
            }
        }
        return instance;
    }
    public String getId() {
        return id;
    }
    // To maintain singleton during deserialization
    @Serial
    protected Object readResolve() {
        return getInstance();
    }
}
