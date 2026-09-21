package com.midland.saloon.Setting.Model;

import org.springframework.stereotype.Component;

import java.util.Map;
@Component
public class BranchCodeHelper {
    private  final Map<String, String> REGION_CODES = Map.ofEntries(
            Map.entry("ARUSHA", "ARU"),
            Map.entry("DAR ES SALAAM", "DAR"),
            Map.entry("DODOMA", "DOD"),
            Map.entry("GEITA", "GEI"),
            Map.entry("IRINGA", "IRI"),
            Map.entry("KAGERA", "KAG"),
            Map.entry("KATAVI", "KAT"),
            Map.entry("KIGOMA", "KIG"),
            Map.entry("KILIMANJARO", "KIL"),
            Map.entry("LINDI", "LIN"),
            Map.entry("MANYARA", "MAN"),
            Map.entry("MARA", "MAR"),
            Map.entry("MBEYA", "MBE"),
            Map.entry("MOROGORO", "MOR"),
            Map.entry("MTWARA", "MTW"),
            Map.entry("MWANZA", "MWA"),
            Map.entry("NJOMBE", "NJO"),
            Map.entry("PWANI", "PWA"),
            Map.entry("RUKWA", "RUK"),
            Map.entry("RUVUMA", "RUV"),
            Map.entry("SHINYANGA", "SHI"),
            Map.entry("SIMIYU", "SIM"),
            Map.entry("SINGIDA", "SIN"),
            Map.entry("SONGWE", "SON"),
            Map.entry("TABORA", "TAB"),
            Map.entry("TANGA", "TAN")
    );
    public String generateRegionCode(String region) {

        if (region == null || region.isBlank()) {
            throw new IllegalArgumentException(
                    "Region cannot be empty"
            );
        }

        String normalizedRegion =
                region.trim().toUpperCase();

        String code =
                REGION_CODES.get(normalizedRegion);

        if (code == null) {
            throw new IllegalArgumentException(
                    "Region code not configured for: "
                            + region
            );
        }

        return code;
    }


    public String generateBranchCode(
            String region,
            long sequence
    ) {

        String regionCode =
                generateRegionCode(region);

        return String.format(
                "%s-%03d",
                regionCode,
                sequence
        );
    }
}
