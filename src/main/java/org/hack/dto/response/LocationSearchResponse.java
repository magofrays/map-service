package org.hack.dto.response;

import org.hack.dto.LocationDto;

import java.util.List;

public record LocationSearchResponse(
        List<LocationDto> locations
) {}