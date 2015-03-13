package no.asgari.civilization.server;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;
import no.asgari.civilization.server.model.Item;
import no.asgari.civilization.server.model.PBF;
import no.asgari.civilization.server.mongodb.AbstractMongoDBTest;
import org.junit.Ignore;
import org.junit.Test;

@Ignore("No need to run this test on each build")
public class ParallelStreamTest extends AbstractMongoDBTest {

    private static final int NR_OF_LOOPS = 100_000;

    @Test
    public void testNormalStream() throws Exception {
        PBF pbf = pbfCollection.findOneById(pbfId);

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
        PBF pbf = pbfCollection.findOneById(pbfId);

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

}
