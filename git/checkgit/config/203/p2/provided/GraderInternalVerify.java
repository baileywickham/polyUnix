
import java.io.File;

public class GraderInternalVerify {

    public static void main(String[] args) throws Exception {
	if (args.length > 0 && "-run".equals(args[0])) {
	    Main.main(new String[0]);
	} else if (args.length < 0) {
            EventSchedule s = new EventSchedule();
            double d = s.getCurrentTime();
            Action a = null;
            s.scheduleEvent(new Object(), a, 3.0);
            s.unscheduleAllEvents(new Object());
            s.processEvents(3.7);
            int size = s.size();
	}
    }
}
