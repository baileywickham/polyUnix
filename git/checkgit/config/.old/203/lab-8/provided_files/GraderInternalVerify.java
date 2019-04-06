

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.lang.reflect.Type;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class GraderInternalVerify {

    private static String[] myMapInitial = new String[] {
        "import java.util.List;",
        "import java.util.ArrayList;",
        "",
        "// You may not use any library classes other than List and ArrayList,",
        "// above, to implement your map.  If the string \"java.\" or \"javax.\"",
        "// occurs in this file after this, your submission will be rejected.",
        "",
        ""
    };

    public static void checkMyMapSource() throws Exception {
        File f = new File("MyMap.java");
        List<String> theirSource = Files.readAllLines(f.toPath());
        int i;
        for (i = 0; i < myMapInitial.length; i++) {
            if (!(myMapInitial[i].equals(theirSource.get(i)))) {
                throw new RuntimeException("MyMap.java source differs on line "
                                           + (i+i));
            }
        }
        while (i < theirSource.size()) {
            String line = theirSource.get(i);
            if (line.contains("java.")) {
                throw new RuntimeException("MyMap.java contains \"java.\""
                                            + " on line " + (i+1));
            }
            if (line.contains("javax.")) {
                throw new RuntimeException("MyMap.java contains \"javax.\""
                                            + " on line " + (i+1));
            }
            i++;
        }
    }

    public static void main(String[] args) {
        try {
            checkMyMapSource();
            if (args.length == -1) {        
                // We don't actually want to run the program
                Lab8Main.main(args);
                MyMap<String, Double> map = new MyMap<String, Double>(20);
                map.put("foo", 3.7);
                double d = map.get("foo");
                List<MyMapEntry<String, Double>> l = map.getEntries();
                List<List<MyMapEntry<String, Double>>> l2 = map.getBuckets();
            }
        } catch (Exception ex) {
            System.out.println();
            ex.printStackTrace();
            System.out.println();
            System.out.println("*** Error checking your submission!");
            System.out.println();
            System.exit(1);
        }
        System.out.println("        * Passed. *");
        System.exit(0);
    }
}
