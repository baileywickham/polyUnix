
import java.io.File;
import java.util.List;
import edu.calpoly.spritely.Tile;

public class GraderInternalVerify {

    public static void main(String[] args) throws Exception {
	if (args.length > 0 && "-run".equals(args[0])) {
	    Main.main(new String[0]);
	} else if (args.length == -1) { 
	    List<Tile> tl;
	    tl = VirtualWorld.blacksmithTiles;
	    tl = VirtualWorld.blobTiles;
	    tl = VirtualWorld.minerTiles;
	    tl = VirtualWorld.obstacleTiles;
	    tl = VirtualWorld.oreTiles;
	    tl = VirtualWorld.quakeTiles;
	    tl = VirtualWorld.veinTiles;
	    tl = VirtualWorld.minerFullTiles;
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
