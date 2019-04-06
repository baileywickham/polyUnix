
import java.io.File;

public class GraderInternalVerify {

    public static void main(String[] args) {
        if (args.length > 0 && "-run".equals(args[0])) {
            Main.main(args);
        }
    }
}
