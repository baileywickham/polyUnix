

import java.io.File;
import java.lang.reflect.Type;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;


public class GraderInternalVerifySignatures{

    public static void main(String[] args) {
        try {
            // We don't actually want to run the program
            if (args.length == -1) {        
                    // Generate a compiler error if desired signatures not there
                VirtualWorld.main(args);     
                PathingStrategy strategy = new AStarPathingStrategy();
                final List<Point> pointList = null;
                Point pt = null;
                List<Point> res =
                    strategy.computePath(pt, pt, 
                                        (Point p) -> true,
                                        (Point p) -> pointList);
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
