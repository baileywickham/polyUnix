
import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class GraderInternalVerify {

    public static Class<?> findClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException ex) {
            return  null;
        }
    }

    public static void main(String[] argsIn) throws Exception {
        String[] args;
        if (argsIn.length > 0 && "-run".equals(argsIn[0])) {
            args = new String[0];
	    System.out.println("Running your main() with \"run\".");
            args = new String[] { "run" };
        } else {
            args = new String[] { "test" };
	    System.out.println("Running your main() with \"test\".");
        }
        Class<?> c = findClass("Main");
        if (c == null) {
            c = findClass("MainKt");
        }
        if (c == null) {
            throw new RuntimeException("Couldn't find main class Main or MainKt");
        }
        Method m = c.getDeclaredMethod("main", args.getClass());
        if (m == null) {
            throw new RuntimeException("Couldn't find main(String[]) method");
        } else if ((m.getModifiers() & Modifier.STATIC) == 0) {
            throw new RuntimeException("main(String[]) is not static");
        } else if ((m.getModifiers() & Modifier.PUBLIC) == 0) {
            throw new RuntimeException("main(String[]) is not static");
        }
        m.invoke((Object) null, (Object) args);
    }
}
