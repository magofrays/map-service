package org.hack.repository;

import com.arangodb.springframework.repository.ArangoRepository;
import org.hack.entity.Path;

import java.util.List;
import java.util.Optional;

public interface PathRepository extends ArangoRepository<Path, String> {
    List<Path> findByUserId(String userId);
    Optional<Path> findByTaskId(String taskId);
}
