

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.lang.reflect.Type;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class GraderInternalVerify {


    public static void main(String[] args) {
        try {
            if (args.length == -1) {        
                // We don't actually want to run the program
                MakeImage.main(args);

                MakeImage mi = new MakeImage() {
                    @Override public void transformPoints() {
                        List<Point> p = points;
                    }
                };
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
