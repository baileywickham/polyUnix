
import java.io.File;

public class GraderInternalVerify2 {

    public static void main(String[] args) {
        if (args.length > 0 && "-run".equals(args[0])) {
            Main.main(args);
        }
        if (args.length == -1) {    // never
            Point p = new Point(1.0, 1.0);
            double x = p.getX();
            double y = p.getY();
            double r = p.getRadius();
            double a = p.getAngle();
            Point p2 = p.rotate90();
        }
    }
}
