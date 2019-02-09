package no.asgari.civilization.server;

import no.asgari.civilization.server.mongodb.AbstractCivilizationTest;
import org.junit.Ignore;

@Ignore("No need to run this test on each build")
public class ParallelStreamTest extends AbstractCivilizationTest {

    private static final int NR_OF_LOOPS = 100_000;
/*
    @Test
    public void testNormalStream() throws Exception {
        PBF pbf = getApp().pbfRepository.findById(getApp().pbfId);

        List<Item> list = new ArrayList<>();

        for (int i = 0; i < NR_OF_LOOPS; i++) {
            list.addAll(pbf.getItems());
        }

        assertThat(list.size()).isEqualTo(pbf.getItems().size() * NR_OF_LOOPS);

        Stopwatch started = Stopwatch.createStarted();
        list.stream()
                .filter(p -> p.getSheetName() == SheetName.CITY_STATES)
                .count();

        started.stop();
        System.out.println("Normal stream used " + started.elapsed(TimeUnit.MILLISECONDS) + " milli seconds");
    }

    @Test
    public void testNormalParallelStream() throws Exception {
        PBF pbf = getApp().pbfRepository.findById(getApp().pbfId);

        List<Item> list = new ArrayList<>();

        for (int i = 0; i < NR_OF_LOOPS; i++) {
            list.addAll(pbf.getItems());
        }

        assertThat(list.size()).isEqualTo(pbf.getItems().size() * NR_OF_LOOPS);
        Stopwatch started = Stopwatch.createStarted();
        list.parallelStream()
                .filter(p -> p.getSheetName() == SheetName.CIV)
                .count();
        started.stop();

        System.out.println("Parallel stream used " + started.elapsed(TimeUnit.MILLISECONDS) + " milli seconds");
    }
*/
}
