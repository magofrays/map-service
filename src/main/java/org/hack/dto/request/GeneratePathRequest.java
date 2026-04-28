package org.hack.dto.request;

import jakarta.validation.constraints.NotBlank;
import org.codehaus.commons.nullanalysis.NotNull;
import org.hack.dto.LocationWithOrder;

import java.util.List;

public record GeneratePathRequest(
        @NotBlank
        String title,
        @NotNull
        List<LocationWithOrder> locations,
        @NotNull
        String taskId
) {
}