
import java.io.File;
import java.lang.reflect.Type;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class GraderInternalCheckHiddenFields {

    private static boolean warned = false;

    public static void checkFields(Class c) throws Exception {
        for (Field f : c.getDeclaredFields()) {
            if ((f.getModifiers() & Modifier.STATIC) != 0) {
                continue;
            }
            checkHiddenBy(c.getSuperclass(), f);
        }
    }

    public static void checkHiddenBy(Class c, Field field) throws Exception {
        if (c == null) {
            return;
        }
        Field hidden = null;
        try {
            hidden = c.getDeclaredField(field.getName());
        } catch (NoSuchFieldException ignored) {
        }
        if (hidden != null && (hidden.getModifiers() & Modifier.STATIC) == 0) {
            String message = "** Warning:  ";
            if (isPrivate(field) || isPrivate(hidden)) {
                message = "Note:  ";
                if (!hidden.getClass().getName().contains(".")) {
                    message = null;
                }
            }
            if (message != null) {
                if (!warned) {
                    warned = true;
                    System.out.println();
                }
                System.out.println(
                        message + "Fields " + fieldName(field) + " and "
                        + fieldName(hidden) + " have the same name.");
            }
        }
        checkHiddenBy(c.getSuperclass(), field);
    }

    public static boolean isPrivate(Field f) {
        return (f.getModifiers() & Modifier.PRIVATE) == 1;
    }

    public static String fieldName(Field f) {
        return f.getDeclaringClass().getName() + "."  + f.getName();
    }

    public static void run() throws Exception {
        File dir = new File(".");
        for (File f : dir.listFiles()) {
            String name = f.getName();
            if (name.endsWith(".class") && !name.contains("$")) {
                checkFields(Class.forName(name.substring(0, name.length()-6)));
            }
        }
        if (warned) {
            System.out.println("==>  See https://docs.oracle.com/javase/tutorial/java/IandI/hidevariables.html");
            System.out.println();
        }
    }
}
