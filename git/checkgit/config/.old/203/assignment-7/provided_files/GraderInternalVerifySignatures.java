

import java.io.File;
import java.lang.reflect.Type;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;


public class GraderInternalVerifySignatures{

    public static void checkFileExists(String name) {
        File f = new File(name);
        if (!f.exists()) {
            throw new RuntimeException("Did not find " + f);
        }
    }

    public static void checkDirectoryExists(String name) {
        File f = new File(name);
        if (!f.exists() || !f.isDirectory()) {
            throw new RuntimeException("Did not find directory " + f);
        }
    }


    public static void main(String[] args) {
        boolean runMode = args.length > 0 && "-run".equals(args[0]);
        try {
            // We do want to run the program
            checkFileExists("WORLD_EVENT.txt");
            checkFileExists("gaia.sav");
            checkFileExists("imagelist");
            checkDirectoryExists("images");
            GraderInternalCheckHiddenFields.run();
            if (runMode) {
                String[] newArgs = new String[] { "-fastest" };
                VirtualWorld.main(newArgs);     
            }
        } catch (Exception ex) {
            System.out.println();
            ex.printStackTrace();
            System.out.println();
            System.out.println("*** Error checking your submission!");
            System.out.println();
            System.exit(1);
        }
        if (!runMode) {
            System.out.println("        * Passed. *");
            System.exit(0);
        }
    }
}
