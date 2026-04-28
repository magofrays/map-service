package org.hack.entity;


import com.arangodb.springframework.annotation.ArangoId;
import com.arangodb.springframework.annotation.Document;
import com.arangodb.springframework.annotation.GeoIndexed;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("locations")
public class Location {
    @Id
    private String key;

    @ArangoId
    private String arangoId;

    private String userId;
    private String title;
    private String description;

    @GeoIndexed(geoJson = false)
    private double[] coordinates;

    private List<String> tags;
    private Instant createdAt;
    private Instant updatedAt;

}