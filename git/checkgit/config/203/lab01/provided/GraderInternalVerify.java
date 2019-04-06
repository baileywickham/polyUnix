
import java.io.File;

public class GraderInternalVerify {

    private static void checkFor(String name) {
        File f = new File(name);
        if (!f.exists()) {
            System.out.println();
            System.out.println("***** ERROR  ****");
            System.out.println("I did not find the file " + f.getName() + " in your turnin directory.");
            System.out.println();
            System.out.println();
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        checkFor("../setup/email.txt");
        checkFor("../setup/name.txt");
        if (args.length > 0 && "-run".equals(args[0])) {
            Lab01.main(args);
        }
    }
}
