
import java.util.Comparator;
import java.util.List;

public class GraderInternalVerify {

    public static void main(String[] args) {
        if (args.length > 0 && "-run".equals(args[0])) {
            System.out.println("-run not supported for this lab");
        } else if (args.length == -1) {
            String[] layout = null;
            Main.main(args);
	    TestCases tc = new TestCases(30.0, 0);
	    int r = tc.run();
	    PathingMain pm = new PathingMain(30.0, 0);
	    boolean b = pm.run();
            pm = new PathingMain(layout, 30.0, 0);
        }
    }
}
