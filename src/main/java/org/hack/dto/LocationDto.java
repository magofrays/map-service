package org.hack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.codehaus.commons.nullanalysis.NotNull;

import java.time.Instant;
import java.util.List;

public record LocationDto(
        String locationId,
        String userId,
        @NotBlank(message = "Название не должно быть пустым")
        String title,
        String description,
        @Size(min = 2, max = 2, message = "Координат должно быть 2")
        double[] coordinates,
        List<String> tags
) {}