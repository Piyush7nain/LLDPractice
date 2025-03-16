package singleton;


import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;

public class SingletonTest {

    @Test
    public void testSingletonInstance() {
        Singleton instance1 = Singleton.getInstance();
        Singleton instance2 = Singleton.getInstance();
        assertSame(instance1, instance2);
    }

    @Test
    public void testSingletonThreadSafety() throws InterruptedException, ExecutionException {
        Callable<Singleton> task = Singleton::getInstance;
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Future<Singleton> future1 = executor.submit(task);
        Future<Singleton> future2 = executor.submit(task);

        Singleton instance1 = future1.get();
        Singleton instance2 = future2.get();

        assertSame(instance1, instance2); // "Instances from different threads should be the same";
        executor.shutdown();
    }

    @Test
    public void testLazyInitialization() {
        Singleton instanceBefore = Singleton.getInstance();
        assertNotNull(instanceBefore); // "Instance should be initialized lazily");
    }

    @Test
    public void testSingletonSerialization() throws IOException, ClassNotFoundException {
        Singleton instance1 = Singleton.getInstance();

        // Serialize
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(instance1);
        oos.close();

        // Deserialize
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Singleton instance2 = (Singleton) ois.readObject();

        assertSame(instance1, instance2);// "Deserialized instance should be the same");
    }

    @Test
    public void testSingletonAgainstReflection() {
        Singleton instance1 = Singleton.getInstance();

        Exception exception = assertThrows(NoSuchMethodException.class, () -> {
            Constructor<Singleton> constructor = Singleton.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        });

    }
}
