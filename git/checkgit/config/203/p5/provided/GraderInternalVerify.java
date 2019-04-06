
import java.io.File;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.ToIntBiFunction;
import java.util.function.Function;
import edu.calpoly.spritely.Tile;

public class GraderInternalVerify {

    public static void main(String[] args) throws Exception {
        if (DebugGrid.ENABLED) {
            System.out.println("*****  Error:  DebugGrid.ENABLED is true ***");
            System.exit(1);
        }
	if (args.length > 0 && "-run".equals(args[0])) {
	    Main.main(new String[0]);
	} else if (args.length == -1) { 
	    int passed = (new TestCases()).runTests();
            PathingStrategy strategy = new AStarPathingStrategy() {
		@Override
		protected double costEstimate(Point a, Point b, int steps) {
		    return super.costEstimate(a, b, steps);
		}
	    };
	    Point p = null;
            p = new Point(2, 3);
            int x = p.getX();
            int y = p.getY();
	    Predicate<Point> canPassThrough = null;
	    Function<Point, List<Point>> potentialNeighbors = null;
	    ToIntBiFunction<Point, Point> stepsTo = null;
	    List<Point> res = strategy.computePath(p, p, canPassThrough,
						   potentialNeighbors, stepsTo);
            EventSchedule s = new EventSchedule();
            double d = s.getCurrentTime();
            Action a = null;
            s.scheduleEvent(new Object(), a, 3.0);
            s.unscheduleAllEvents(new Object());
            s.processEvents(3.7);
            int size = s.size();
	} else {
	    Main.main(new String[] { "-test" });
	}
    }
}
