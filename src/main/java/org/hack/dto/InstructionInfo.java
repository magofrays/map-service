package org.hack.dto;


public record InstructionInfo(
        double distance,
        double time,
        String description,
        String street
) {}