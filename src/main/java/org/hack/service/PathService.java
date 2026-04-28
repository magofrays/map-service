package org.hack.service;

import lombok.RequiredArgsConstructor;
import org.hack.dto.PathDto;
import org.hack.entity.Path;
import org.hack.repository.PathRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PathService {
    private final PathRepository pathRepository;

    public List<PathDto> getPaths(String userId){
        return pathRepository.findByUserId(userId).stream().map(this::toPathDto).toList();
    }

    public PathDto getPathByTaskId(String taskId){
        return toPathDto(
                pathRepository.findByTaskId(taskId).orElseThrow(() -> new RuntimeException("Путь по задаче не найден"))
        );
    }

    public PathDto savePath(PathDto pathDto){
        Path path = toPath(pathDto);
        path.setKey(UUID.randomUUID().toString());
        path = pathRepository.save(path);
        return toPathDto(path);
    }

    public PathDto getPathById(String pathId, String userId) {
        Path path = pathRepository.findById(pathId)
                .orElseThrow(() -> new RuntimeException("Path not found"));

        if (!path.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        return toPathDto(path);
    }

    public PathDto updatePath(String pathId, PathDto pathDto, String userId) {
        Path existingPath = pathRepository.findById(pathId)
                .orElseThrow(() -> new RuntimeException("Path not found"));

        if (!existingPath.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        existingPath.setTitle(pathDto.title());
        existingPath.setUpdatedAt(Instant.now());

        pathRepository.save(existingPath);
        return toPathDto(existingPath);
    }

    public void deletePath(String pathId, String userId) {
        Path path = pathRepository.findById(pathId)
                .orElseThrow(() -> new RuntimeException("Path not found"));

        if (!path.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        pathRepository.delete(path);
    }


    public Path toPath(PathDto pathDto){
        return Path.builder()
                .userId(pathDto.userId())
                .title(pathDto.title())
                .taskId(pathDto.taskId())
                .routeResult(pathDto.routeResult())
                .locationsAmount(pathDto.locationsAmount())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public PathDto toPathDto(Path path){
        return PathDto.builder()
                .id(path.getKey())
                .taskId(path.getTaskId())
                .title(path.getTitle())
                .userId(path.getUserId())
                .routeResult(path.getRouteResult())
                .locationsAmount(path.getLocationsAmount())
                .locations(path.getLocations())
                .build();
    }
}
