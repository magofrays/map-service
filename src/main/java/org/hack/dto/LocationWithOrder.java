package org.hack.dto;

public record LocationWithOrder(
        LocationDto location,
        int order
) {}