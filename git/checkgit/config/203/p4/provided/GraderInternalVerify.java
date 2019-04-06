
import java.io.File;
import java.util.List;
import edu.calpoly.spritely.Tile;

public class GraderInternalVerify {

    public static void main(String[] args) throws Exception {
	Class.forName("Action");
	Class.forName("Blacksmith");
	Class.forName("Entity");
	Class.forName("Event");
	Class.forName("EventSchedule");
	Class.forName("EventTimeComparator");
	Class.forName("Main");
	Class.forName("MinerFull");
	Class.forName("MinerNotFull");
	Class.forName("Obstacle");
	Class.forName("Ore");
	Class.forName("OreBlob");
	Class.forName("Point");
	Class.forName("Quake");
	Class.forName("TestCases");
	Class.forName("Vein");
	Class.forName("VirtualWorld");
	Class.forName("WorldModel");
	if (args.length > 0 && "-run".equals(args[0])) {
	    Main.main(new String[0]);
	} else if (args.length == -1) { 
	    int passed = (new TestCases()).runTests();
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
