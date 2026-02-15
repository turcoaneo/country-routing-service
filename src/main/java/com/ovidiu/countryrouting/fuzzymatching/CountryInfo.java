package com.ovidiu.countryrouting.fuzzymatching;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CountryInfo {
    private String cca2;
    private String ccn3;
    private String cca3;
    private String cioc;
    private String name;
    private List<String> borders;

}
