package test.com.wallace.javapow.readers;

import com.wallace.javapow.readers.CsvReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import test.com.wallace.javapow.readers.models.PersonModel;

import java.util.List;

public class CsvReaderTests {

    @Test
    public void readCsvFile() {
        List<PersonModel> personsList = new CsvReader<PersonModel>(PersonModel.class).read();
        Assertions.assertNotNull(personsList);
        Assertions.assertEquals(3, personsList.size());
    }
}
