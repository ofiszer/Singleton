import java.util.concurrent.atomic.AtomicInteger;
import java.io.Serializable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

class Singleton implements Serializable {
    private static volatile Singleton instance;
    //Zastosowanie volatile do zmiennej instance zapewni
    //poprawną synchronizację i jednocześnie optymalną wydajność
    //sprawia, że zmienna może być zmieniana przez różne wątki.
    //Słowo kluczowe volatile jest przydatne w przypadku zmiennych,
    //które są współdzielone między wątkami, ale nie są modyfikowane w sposób,
    //który wymaga synchronizacji za pomocą blokad.
    private static int singletonNumber = 0;
    private static final long serialVersionUID = 1L;
    //private static final ThreadLocal<Singleton> threadLocalInstance = ThreadLocal.withInitial(Singleton::new);
    //private static final ThreadLocal<Singleton> threadLocalInstance = ThreadLocal.withInitial(Singleton::getInstance);

    private static final ThreadLocal<Singleton> threadLocalInstance = ThreadLocal.withInitial(() -> {
        synchronized (Singleton.class) {
            if (instance == null) {
                instance = new Singleton();
            }
        }
        return new Singleton();
    });
    //private static int singletonNumber = 0;

    private Singleton() {
        //singletonNumber++;
        //System.out.println("Singleton numer: " + singletonNumber);
    }

    public static Singleton getInstance() {
        /*if (instance == null) {
            synchronized (Singleton.class) {
                if (instance == null) {
                    instance = new Singleton();
                }
            }
        }
        return instance;*/
        return threadLocalInstance.get();
    }

    public static Singleton getInstanceThreadLocal() {
        return threadLocalInstance.get();
    }

    // Metoda wywoływana podczas deserializacji, aby zachować jedyną instancję Singletona
    protected Object readResolve() {
        return getInstance();
    }
    protected Object readResolveThreadLocal() {
        return getInstanceThreadLocal();
    }
}
/*
class Singleton {
    private static volatile Singleton instance;
    private static int singletonNumber = 0;
    Singleton() {
        singletonNumber++;
        System.out.println("Singleton Number: " + singletonNumber);
    }

    public static Singleton getInstance() {
        if (instance == null) {
            synchronized (Singleton.class) {
                if (instance == null) {
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }
}
*/


public class VariableContainer11 {
    private static volatile VariableContainer11 instance;
    //Zastosowanie volatile do zmiennej instance zapewni
    //poprawną synchronizację i jednocześnie optymalną wydajność
    //sprawia, że zmienna może być zmieniana przez różne wątki.
    //Słowo kluczowe volatile jest przydatne w przypadku zmiennych,
    //które są współdzielone między wątkami, ale nie są modyfikowane w sposób,
    //który wymaga synchronizacji za pomocą blokad.

    //private static final ThreadLocal<VariableContainer11> threadLocalInstance = ThreadLocal.withInitial(VariableContainer11::new);
    private static final ThreadLocal<Integer> variable = new ThreadLocal<Integer>() {
        @Override
        protected Integer initialValue() {
            return 0;
        }
    };
    private AtomicInteger atomicVariable = new AtomicInteger(0);

    private VariableContainer11() {
    }


    public static VariableContainer11 getInstance() {
        if (instance == null) {
            synchronized (VariableContainer11.class) {
                if (instance == null) {
                    instance = new VariableContainer11();
                }
            }
        }
        return instance;
    }


    public void increaseBy(int howMuch) {
        variable.set(variable.get() + howMuch);
        atomicVariable.addAndGet(howMuch);
    }

    public int get() {
        return variable.get();
    }

    public static void main(String[] args) {
        // Print thread names before and after running the test
        System.out.println("Wątek główny: " + Thread.currentThread().getName());
        // Performance Test
        long startTime = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            VariableContainer11 container = VariableContainer11.getInstance();
            container.increaseBy(5000);
            int value = container.get();
        }
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);
        System.out.println("Czas na 1000 operacji: " + duration / 1000000 + " ms");


        // Tworzymy 10 wątków, gdzie każdy pobiera instancję Singletona
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                //System.out.println("Nowy wątek");
                Singleton singleton = Singleton.getInstance();
                System.out.println(Thread.currentThread().getName() + ": " + singleton);
                //System.out.println("Koniec nowego wątku");
            }).start();
        }
        /*System.out.println("Additional singletons created during runtime:");
        for (int i = 0; i < 5; i++) {
            Singleton additionalSingleton = Singleton.getInstance();
        }*/
        System.out.println("Test bezpieczeństwa wątków:");
        Runnable task = () -> {
            VariableContainer11 container = VariableContainer11.getInstance();
            container.increaseBy(5000);
            System.out.println("Wątek: " + Thread.currentThread().getName() + " zinkrementował zmienną.");
        };

        for (int i = 0; i < 10; i++) {
            new Thread(task).start();
        }
        //testy serializacji i deserializacji
        try {
            testSerializationDeserialization();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void testSerializationDeserialization() throws IOException, ClassNotFoundException {
        //Serializacja
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
        Singleton singleton1 = Singleton.getInstance();
        objectOutputStream.writeObject(singleton1);

        //Deserializacja
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
        Singleton singleton2 = (Singleton) objectInputStream.readObject();

        singleton2.readResolve();
        //Sprawdź czy obiekt po deserializacji jest taki sam jak obiekt przed serializacją
        System.out.println("Singleton 1: " + singleton1);
        System.out.println("Singleton 2: " + singleton2);

        Singleton singleton3 = Singleton.getInstance();
        Singleton singleton4 = Singleton.getInstance();

        if (singleton3 == singleton4) {
            System.out.println("Udało się utworzyć tylko jeden singleton.");
        } else {
            System.out.println("Utworzono więcej niż jeden singleton.");
        }
    }
}
