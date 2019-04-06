
import java.util.Comparator;
import java.util.List;

public class GraderInternalVerify {

    public static void main(String[] args) {
        if (args.length > 0 && "-run".equals(args[0])) {
            System.out.println("-run not supported for this lab");
        } else if (args.length == -1) {
            Main.main(args);
        }
    }
}
