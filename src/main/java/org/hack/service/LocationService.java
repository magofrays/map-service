package org.hack.service;

import lombok.RequiredArgsConstructor;
import org.hack.dto.*;
import org.hack.dto.request.GeneratePathRequest;
import org.hack.dto.request.LocationSearchRequest;
import org.hack.dto.response.GeneratePathResponse;
import org.hack.dto.response.LocationSearchResponse;
import org.hack.entity.Location;
import org.hack.repository.LocationRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;
    private final GraphHopperService graphHopperService;
    private final PathService pathService;


    public LocationSearchResponse searchLocations(LocationSearchRequest request,
                                                  String userId) {
        List<Location> tagsLocations = null;

//        if (request.tags() != null && !request.tags().isEmpty()) {
            tagsLocations = locationRepository.findNearby(
                    request.latitude(),
                    request.longitude(),
                    request.radius()
//                    request.tags()
            );
//        }
        List<Location> userLocations = null;
        if (request.addMyLocations()){
            userLocations = locationRepository.findNearby(request.latitude(),
                    request.longitude(),
                    request.radius(),
                    request.tags(),
                    userId
                    );
        }
        List<Location> interestingPlaces = null;
        if(request.addInterestingPlaces()){
            // request profile service
        }
        Set<String> ids = new HashSet<>();
        List<Location> locations = new ArrayList<>();
        if(tagsLocations != null){
            for(var tagLoc : tagsLocations){
                if(!ids.contains(tagLoc.getKey())){
                    ids.add(tagLoc.getKey());
                    locations.add(tagLoc);
                }
            }
        }
        if(userLocations != null){
            for(var userLoc : userLocations){
                if(!ids.contains(userLoc.getKey())){
                    ids.add(userLoc.getKey());
                    locations.add(userLoc);
                }
            }
        }
        if(interestingPlaces != null){
            for(var intPlace : interestingPlaces){
                if(!ids.contains(intPlace.getKey())){
                    ids.add(intPlace.getKey());
                    locations.add(intPlace);
                }
            }
        }

        List<LocationDto> locationDtos = locations.stream()
                .limit(request.limit())
                .map(this::toDto)
                .toList();

        return new LocationSearchResponse(locationDtos);
    }

    public LocationDto create(LocationDto dto, String userId) {
        checkLocation(dto);
        Location location = toEntity(dto);
        location.setKey(UUID.randomUUID().toString());
        location.setUserId(userId);
        location.setCreatedAt(Instant.now());
        location.setUpdatedAt(Instant.now());

        Location saved = locationRepository.save(location);
        return toDto(saved);
    }


    public LocationDto update(LocationDto dto, String userId) {
        checkLocation(dto);
        Location existing = locationRepository.findById(dto.locationId())
                .orElseThrow(() -> new RuntimeException("Локация не найдена: " + dto.locationId()));
        if (!existing.getUserId().equals(userId)) {
            throw new RuntimeException("Пользователь не имеет право обновлять локацию: " + dto.locationId());
        }
        existing.setTitle(dto.title());
        existing.setDescription(dto.description());
        existing.setCoordinates(dto.coordinates());
        existing.setTags(dto.tags());
        existing.setUpdatedAt(Instant.now());

        Location saved = locationRepository.save(existing);
        return toDto(saved);
    }


    public void delete(String id, String userId) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Локация не найдена: " + id));
        if (!location.getUserId().equals(userId)) {
            throw new RuntimeException("Пользователь не имеет право обновлять локацию: " + id);
        }
        locationRepository.deleteById(id);
    }

    public GeneratePathResponse generatePath(GeneratePathRequest request, String userId) {
        List<LocationWithOrder> sortedLocations = request.locations().stream()
                .sorted(Comparator.comparingInt(LocationWithOrder::order))
                .toList();
        List<Location> sortLocat = new ArrayList<>();
        for (var location : sortedLocations) {
            Location locationEnt = locationRepository.findById(location.location().locationId())
                    .orElseThrow(
                            () -> new RuntimeException("Позиции не существует: " + location.location().locationId()
                            ));
            sortLocat.add(locationEnt);

        }
        List<PointDto> points = sortLocat.stream()
                .map(loc -> new PointDto(
                        loc.getCoordinates()[0],
                        loc.getCoordinates()[1]
                ))
                .toList();

        RouteResult route = graphHopperService.calculateRoute(points);
        PathDto pathDto = pathService.savePath(PathDto.builder()
                .locations(sortedLocations)
                .title(request.title())
                .userId(userId)
                .taskId(request.taskId())
                .locationsAmount(sortedLocations.size())
                .routeResult(route)
                .build());

        return new GeneratePathResponse(
                pathDto.id(),
                sortedLocations.size(),
                route.time(),
                sortedLocations,
                route
        );
    }

    public Optional<LocationDto> findById(String id) {
        return locationRepository.findById(id).map(this::toDto);
    }

    private LocationDto toDto(Location location) {
        return new LocationDto(
                location.getKey(),
                location.getUserId(),
                location.getTitle(),
                location.getDescription(),
                location.getCoordinates(),
                location.getTags()
        );
    }

    private Location toEntity(LocationDto dto) {
        return Location.builder()
                .key(dto.locationId())
                .userId(dto.userId())
                .title(dto.title())
                .description(dto.description())
                .coordinates(dto.coordinates())
                .tags(dto.tags())
                .build();
    }
    public void checkLocation(LocationDto dto){
        if(Math.abs(dto.coordinates()[0]) > 90 || Math.abs(dto.coordinates()[1]) > 180){
            throw new RuntimeException("Некорректные координаты. Ширина должна быть от -90 до 90. Высота должно быть от -180 до 180");
        }
    }
    public Long saveDump(List<LocationDto> locations) {
        locations.forEach(this::checkLocation);
        List<Location> entities = locations.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());

        Iterable<Location> saved = locationRepository.saveAll(entities);

        return StreamSupport.stream(saved.spliterator(), false).count();
    }
}