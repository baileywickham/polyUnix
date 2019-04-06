
import java.util.Comparator;
import java.util.List;

public class GraderInternalVerify {

    public static void main(String[] args) {
        if (args.length > 0 && "-run".equals(args[0])) {
            Main.main(args);
        } else if (args.length == -1) {
            Comparator<Song> ac = new ArtistComparator();
            Comparator<Song> cc = new ComposedComparator(ac, ac);
	    Comparator<Song> c;
	    c = Comparators.sortByArtistThenTitleThenYearComparator;
	    c = Comparators.composedComparator;
	    c = Comparators.twoFieldComparator;
	    c = Comparators.keyExtractorYearComparator;
	    c = Comparators.lambdaTitleComparator;
	    c = Comparators.artistComparator;
        }
    }
}
