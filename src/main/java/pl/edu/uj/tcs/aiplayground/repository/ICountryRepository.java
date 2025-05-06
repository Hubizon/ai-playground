package pl.edu.uj.tcs.aiplayground.repository;

import pl.edu.uj.tcs.jooq.tables.records.CountriesRecord;

import java.util.List;

public interface ICountryRepository {
    List<CountriesRecord> getCountries();

    Integer getCountryIdByName(String countryName);

    CountriesRecord getCountryById(Integer countryId);
}
