package org.hack.entity;

import com.arangodb.springframework.annotation.ArangoId;
import com.arangodb.springframework.annotation.Document;
import lombok.Builder;
import lombok.Data;
import org.hack.dto.LocationWithOrder;
import org.hack.dto.RouteResult;
import org.springframework.data.annotation.Id;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@Document("paths")
public class Path {
    @Id
    private String key;

    @ArangoId
    private String arangoId;

    private Integer locationsAmount;
    private String userId;
    private String title;
    private String taskId;
    private List<LocationWithOrder> locations;
    private RouteResult routeResult;
    private Instant createdAt;
    private Instant updatedAt;
}
