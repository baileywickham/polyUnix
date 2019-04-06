
import java.io.File;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.ToIntBiFunction;
import java.util.function.Function;
import edu.calpoly.spritely.Tile;

public class GraderInternalVerify {

    public static void main(String[] args) throws Exception {
	if (args.length > 0 && "-run".equals(args[0])) {
	    Main.main(new String[0]);
	} else {
	    Main.main(new String[] { "-test" });
	}
    }
}
