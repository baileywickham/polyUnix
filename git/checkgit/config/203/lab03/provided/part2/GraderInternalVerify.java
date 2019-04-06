
import java.util.ArrayList;
import java.util.List;

public class GraderInternalVerify {

    public static void main(String[] args) {
        if (args.length > 0 && "-run".equals(args[0])) {
            Main.main(args);
        } else if (args.length == -1) {
            Circle c = new Circle(new Point(0.0, 0.0), 1.0);
            Point pt = c.getCenter();
            double d = c.getRadius();
            d = pt.getX();
            d = pt.getY();
            List<Point> pl = new ArrayList<>();
            Polygon p = new Polygon(pl);
            pl = p.getPoints();
            Rectangle r = new Rectangle(pt, pt);
            pt = r.getTopLeft();
            pt = r.getBottomRight();
            d = c.perimeter();
            d = r.perimeter();
            d = p.perimeter();
            d = Bigger.whichIsBigger(c, r, p);
        }
    }
}
