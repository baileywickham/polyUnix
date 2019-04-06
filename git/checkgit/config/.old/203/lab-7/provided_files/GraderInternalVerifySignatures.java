

import java.io.File;
import java.lang.reflect.Type;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class GraderInternalVerifySignatures{

    public static void main(String[] args) {
        try {
            if (args.length == -1) {        // We don't actually want to run the program
                PathingMain.main(args);     // Generate a compiler error if not there
            }
            GraderInternalCheckHiddenFields.run();
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
