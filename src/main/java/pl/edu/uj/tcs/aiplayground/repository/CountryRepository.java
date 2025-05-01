package pl.edu.uj.tcs.aiplayground.repository;

import org.example.jooq.tables.records.CountriesRecord;
import org.jooq.DSLContext;

import java.util.List;

import static org.example.jooq.Tables.COUNTRIES;

public class CountryRepository implements ICountryRepository {
    private final DSLContext dsl;

    public CountryRepository(DSLContext dslContext) {
        this.dsl = dslContext;
    }

    public List<CountriesRecord> getCountriesNames() {
        return dsl.select(COUNTRIES.NAME)
                .from(COUNTRIES)
                .fetchInto(CountriesRecord.class);
    }

    public Integer getCountryIdByName(String countryName) {
        return dsl.select(COUNTRIES.ID)
                .from(COUNTRIES)
                .where(COUNTRIES.NAME.eq(countryName))
                .fetchOneInto(Integer.class);
    }

    public CountriesRecord getCountryById(Integer countryId) {
        return dsl.selectFrom(COUNTRIES)
                .where(COUNTRIES.ID.eq(countryId))
                .fetchOneInto(CountriesRecord.class);
    }
}
