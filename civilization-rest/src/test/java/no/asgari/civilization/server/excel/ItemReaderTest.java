package no.asgari.civilization.server.excel;

import no.asgari.civilization.server.model.GameType;
import no.asgari.civilization.server.model.Tech;
import org.junit.Test;

import java.io.IOException;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertNull;

public class ItemReaderTest {

    @Test
    public void readItemsFromExcel() throws IOException {
        ItemReader itemReader = new ItemReader();

        assertNull(itemReader.shuffledCivs);
        assertNull(itemReader.shuffledCultureI);
        assertNull(itemReader.shuffledCultureII);
        assertNull(itemReader.shuffledCultureIII);

        itemReader.readItemsFromExcel(GameType.WAW);

        assertThat(itemReader.shuffledCivs).isNotEmpty();
        assertThat(itemReader.shuffledCultureI).isNotEmpty();
        assertThat(itemReader.shuffledCultureII).isNotEmpty();
        assertThat(itemReader.shuffledCultureIII).isNotEmpty();
        assertThat(itemReader.shuffledGPs).isNotEmpty();
        assertThat(itemReader.shuffledHuts).isNotEmpty();
        assertThat(itemReader.shuffledVillages).isNotEmpty();
        assertThat(itemReader.modernWonders).isNotEmpty();
        assertThat(itemReader.medievalWonders).isNotEmpty();
        assertThat(itemReader.ancientWonders).isNotEmpty();
        assertThat(itemReader.shuffledTiles).isNotEmpty();
        assertThat(itemReader.shuffledCityStates).isNotEmpty();
        assertThat(itemReader.allTechs).isNotEmpty();
        assertThat(itemReader.allTechs).contains(Tech.SPACE_FLIGHT);
        assertThat(itemReader.allTechs).isSorted();
    }
}
