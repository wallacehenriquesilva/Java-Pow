package test.com.wallace.javapow.readers.models;

import com.wallace.javapow.annotations.Csv;
import com.wallace.javapow.annotations.CsvBooleanColumn;
import com.wallace.javapow.annotations.CsvCollectionColumn;
import com.wallace.javapow.annotations.CsvColumn;
import com.wallace.javapow.annotations.CsvDateColumn;
import com.wallace.javapow.enums.ColumnTypeEnum;

import java.time.LocalDateTime;
import java.util.List;

@Csv(path = "test1.csv")
public class PersonModel {

    @CsvColumn(name = "name", type = ColumnTypeEnum.STRING)
    private String name;

    @CsvColumn(name = "years", type = ColumnTypeEnum.INTEGER)
    private int years;

    @CsvBooleanColumn(name = "developer", trueValues = {"1", "sim", "yes"}, isCaseSensitive = false)
    private boolean isDeveloper;

    @CsvDateColumn(name = "date", pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registerDate;

    @CsvCollectionColumn(name = "nicks", collectionDelimiterRegex = ",")
    private List<String> nickNamesList;

    @CsvCollectionColumn(name = "values", collectionDelimiterRegex = ",")
    private Integer[] values;

    @CsvCollectionColumn(name = "bools", collectionDelimiterRegex = ",")
    private Boolean[] bools;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getYears() {
        return years;
    }

    public void setYears(int years) {
        this.years = years;
    }

    public boolean isDeveloper() {
        return isDeveloper;
    }

    public void setDeveloper(boolean developer) {
        isDeveloper = developer;
    }

    public LocalDateTime getRegisterDate() {
        return registerDate;
    }

    public void setRegisterDate(LocalDateTime registerDate) {
        this.registerDate = registerDate;
    }

    public Integer[] getValues() {
        return values;
    }

    public void setValues(Integer[] values) {
        this.values = values;
    }

    public Boolean[] getBools() {
        return bools;
    }

    public void setBools(Boolean[] bools) {
        this.bools = bools;
    }

    public List<String> getNickNamesList() {
        return nickNamesList;
    }

    public void setNickNamesList(List<String> nickNamesList) {
        this.nickNamesList = nickNamesList;
    }
}
