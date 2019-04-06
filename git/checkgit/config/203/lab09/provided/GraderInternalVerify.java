
import java.util.Comparator;
import java.util.List;

public class GraderInternalVerify {

    public static void main(String[] args) {
        if (args.length > 0 && "-run".equals(args[0])) {
            Main.main(args);
        } else if (args.length == -1) {
	    MyMap<String, Integer> map = new MyMap<>(0);
	    Integer myInt = 3;
	    map.put("foo", myInt);
	    myInt = map.get("foo");
	    List<MyMapEntry<String, Integer>> l = map.getEntries();
	    List<List<MyMapEntry<String, Integer>>> l2 = map.getBuckets();
        }
    }
}
