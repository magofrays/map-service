package org.hack.repository;

import com.arangodb.springframework.annotation.Query;
import com.arangodb.springframework.repository.ArangoRepository;
import org.hack.entity.Location;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationRepository extends ArangoRepository<Location, String> {

    @Query("FOR l IN locations " +
            "FILTER GEO_DISTANCE([@lng, @lat], l.coordinates) <= @radius " +
            "FILTER @tags == null OR LENGTH(@tags) == 0 OR LENGTH(INTERSECTION(l.tags, @tags)) > 0 " +
            "SORT GEO_DISTANCE([@lng, @lat], l.coordinates) ASC " +
            "RETURN l")
    List<Location> findNearby(
            @Param("lat") double lat,
            @Param("lng") double lon,
            @Param("radius") double radius,
            @Param("tags") List<String> tags
    );

    @Query("FOR l IN locations " +
            "FILTER GEO_DISTANCE([@lng, @lat], l.coordinates) <= @radius " +
            "FILTER (@tags == null OR LENGTH(@tags) == 0 OR LENGTH(INTERSECTION(l.tags, @tags)) > 0) " +
            "FILTER (@userId == null OR @userId == '' OR l.userId == @userId) " +
            "SORT GEO_DISTANCE([@lng, @lat], l.coordinates) ASC " +
            "RETURN l")
    List<Location> findNearby(
            @Param("lat") double lat,
            @Param("lng") double lon,
            @Param("radius") double radius,
            @Param("tags") List<String> tags,
            @Param("userId") String userId
    );

    @Query("FOR l IN locations " +
            "FILTER GEO_DISTANCE([@lng, @lat], l.coordinates) <= @radius " +
            "SORT GEO_DISTANCE([@lng, @lat], l.coordinates) ASC " +
            "RETURN l")
    List<Location> findNearby(
            @Param("lat") double lat,
            @Param("lng") double lon,
            @Param("radius") double radius
    );

    @Query("FOR l IN locations " +
            "FILTER @tag IN l.tags " +
            "RETURN l")
    List<Location> findByTagsContaining(@Param("tag") String tag);

    List<Location> findByUserId(String userId);

}