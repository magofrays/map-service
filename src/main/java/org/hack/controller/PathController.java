package org.hack.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.hack.dto.PathDto;
import org.hack.service.PathService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/path")
@RequiredArgsConstructor
@Tag(name = "Path Management", description = "API для управления сохраненными маршрутами")
public class PathController {

    private final PathService pathService;

    /**
     * GET /user/paths
     */
    @Operation(
            summary = "Получить все пути пользователя",
            description = "Возвращает список всех сохраненных маршрутов для текущего пользователя"
    )
    @GetMapping("/user")
    public ResponseEntity<?> getUserPaths(@AuthenticationPrincipal Jwt userToken) {
        try {
            String userId = userToken.getClaim("sub");
            List<PathDto> paths = pathService.getPaths(userId);
            return ResponseEntity.ok(Map.of("paths", paths));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error_message", e.getMessage()));
        }
    }

    @Operation(
            summary = "Получить все пути пользователя",
            description = "Возвращает список всех сохраненных маршрутов для текущего пользователя"
    )
    @GetMapping("/task/{taskId}")
    public ResponseEntity<?> getPathByTask(@PathVariable String taskId) {
        try {

            PathDto path = pathService.getPathByTaskId(taskId);
            return ResponseEntity.ok(Map.of("paths", path));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error_message", e.getMessage()));
        }
    }



    /**
     * GET /user/paths/{path_id}
     */
    @Operation(
            summary = "Получить путь по ID",
            description = "Возвращает конкретный маршрут пользователя по его идентификатору"
    )
    @GetMapping("/{path_id}")
    public ResponseEntity<?> getPathById(
            @Parameter(description = "ID маршрута", required = true)
            @PathVariable("path_id") String pathId,
            @AuthenticationPrincipal Jwt userToken) {
        try {
            String userId = userToken.getClaim("sub");
            PathDto path = pathService.getPathById(pathId, userId);
            return ResponseEntity.ok(Map.of("path", path));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(Map.of("error_message", e.getMessage()));
        }
    }

    /**
     * POST /user/paths
     */
    @Operation(
            summary = "Сохранить новый маршрут",
            description = "Создает новый маршрут на основе переданных данных"
    )
    @PostMapping("")
    public ResponseEntity<?> savePath(
            @RequestBody Map<String, PathDto> body,
            @AuthenticationPrincipal Jwt userToken) {
        try {
            PathDto pathDto = body.get("path");
            String userId = userToken.getClaim("sub");

            // Устанавливаем userId из токена для безопасности
            PathDto pathWithUser = PathDto.builder()
                    .userId(userId)
                    .title(pathDto.title())
                    .routeResult(pathDto.routeResult())
                    .locations(pathDto.locations())
                    .locationsAmount(pathDto.locationsAmount())
                    .build();

            pathService.savePath(pathWithUser);
            return ResponseEntity.ok(Map.of("path", pathWithUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error_message", e.getMessage()));
        }
    }

    /**
     * PUT /user/paths/{path_id}
     */
    @Operation(
            summary = "Обновить маршрут",
            description = "Обновляет существующий маршрут пользователя"
    )
    @PutMapping("/{path_id}")
    public ResponseEntity<?> updatePath(
            @Parameter(description = "ID маршрута", required = true)
            @PathVariable("path_id") String pathId,
            @RequestBody Map<String, PathDto> body,
            @AuthenticationPrincipal Jwt userToken) {
        try {
            PathDto pathDto = body.get("path");
            String userId = userToken.getClaim("sub");

            PathDto updated = pathService.updatePath(pathId, pathDto, userId);
            return ResponseEntity.ok(Map.of("path", updated));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(Map.of("error_message", e.getMessage()));
        }
    }

    /**
     * DELETE /user/paths/{path_id}
     */
    @Operation(
            summary = "Удалить маршрут",
            description = "Удаляет маршрут пользователя по его ID"
    )
    @DeleteMapping("/{path_id}")
    public ResponseEntity<?> deletePath(
            @Parameter(description = "ID маршрута", required = true)
            @PathVariable("path_id") String pathId,
            @AuthenticationPrincipal Jwt userToken) {
        try {
            String userId = userToken.getClaim("sub");
            pathService.deletePath(pathId, userId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(Map.of("error_message", e.getMessage()));
        }
    }




}