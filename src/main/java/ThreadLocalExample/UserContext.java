package ThreadLocalExample;
public class UserContext {
    private static final ThreadLocal<String> currentUser =
            new ThreadLocal<String>() {
        @Override
        protected String initialValue() {
            return "Guest"; // Default user
        }
    };

    public static String getCurrentUser() {
        return currentUser.get();
    }

    public static void setCurrentUser(String user) {
        currentUser.set(user);
    }

    public static void clear() {
        currentUser.remove(); // Important for cleanup
    }

    public static void main(String[] args) throws InterruptedException {
        Runnable task1 = () -> {
            UserContext.setCurrentUser("Alice");
            System.out.println(Thread.currentThread().getName() + ": Current User = " + UserContext.getCurrentUser());
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            UserContext.clear();
        };

        Runnable task2 = () -> {
            System.out.println(Thread.currentThread().getName() + ": Current User = " + UserContext.getCurrentUser());
            UserContext.setCurrentUser("Bob");
            System.out.println(Thread.currentThread().getName() + ": Current User = " + UserContext.getCurrentUser());
            UserContext.clear();
        };

        Thread threadA = new Thread(task1, "Thread-A");
        Thread threadB = new Thread(task2, "Thread-B");

        threadA.start();
        threadB.start();

        threadA.join();
        threadB.join();

        System.out.println(Thread.currentThread().getName() + ": Current User after threads = " + UserContext.getCurrentUser());
    }
}