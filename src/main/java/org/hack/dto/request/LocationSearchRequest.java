package org.hack.dto.request;

import org.codehaus.commons.nullanalysis.NotNull;

import java.util.List;

public record LocationSearchRequest(
        int limit,
        @NotNull
        Double latitude,
        @NotNull
        Double longitude,
        @NotNull
        Double radius,
        List<String> tags, // different tags
        boolean addInterestingPlaces, // our tags
        boolean addMyLocations,
        List<String> relatedUserIds
) {}