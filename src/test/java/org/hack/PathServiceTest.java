package org.hack;

import org.hack.dto.LocationDto;
import org.hack.dto.LocationWithOrder;
import org.hack.dto.PathDto;
import org.hack.dto.RouteResult;
import org.hack.repository.PathRepository;
import org.hack.service.PathService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
class PathServiceTest {

    @Autowired
    private PathService pathService;

    @Autowired
    private PathRepository pathRepository;

    private static final String TEST_USER_ID = "test-user-123";
    private static final String OTHER_USER_ID = "other-user-456";
    private static final String TEST_TASK_ID = "task-123";
    private static final String TEST_TITLE = "Мой маршрут";

    private PathDto testPathDto;
    private RouteResult testRouteResult;
    private List<LocationWithOrder> testLocations;

    @BeforeEach
    void setUp() {
        // Очищаем тестовые данные
        pathRepository.findByUserId(TEST_USER_ID)
                .forEach(path -> pathRepository.delete(path));
        pathRepository.findByUserId(OTHER_USER_ID)
                .forEach(path -> pathRepository.delete(path));

        // Создаём тестовый RouteResult
        testRouteResult = RouteResult.builder()
                .distance(1500.5)
                .time(120.0)
                .build();

        // Создаём тестовые локации
        testLocations = List.of(
                new LocationWithOrder(
                        new LocationDto("loc-1", TEST_USER_ID, "Старт", "", new double[]{59.9343, 30.3351}, List.of("тест")),
                        0
                ),
                new LocationWithOrder(
                        new LocationDto("loc-2", TEST_USER_ID, "Финиш", "", new double[]{59.9390, 30.3158}, List.of("тест")),
                        1
                )
        );

        // Создаём тестовый PathDto
        testPathDto = PathDto.builder()
                .taskId(TEST_TASK_ID)
                .title(TEST_TITLE)
                .userId(TEST_USER_ID)
                .routeResult(testRouteResult)
                .locationsAmount(2)
                .locations(testLocations)
                .build();
    }

    @Test
    void savePath_ShouldSaveSuccessfully() {
        // Act
        pathService.savePath(testPathDto);

        // Assert
        PathDto savedPath = pathService.getPathByTaskId(TEST_TASK_ID);
        assertNotNull(savedPath);
        assertEquals(TEST_USER_ID, savedPath.userId());
        assertEquals(TEST_TASK_ID, savedPath.taskId());
        assertEquals(TEST_TITLE, savedPath.title());
        assertEquals(2, savedPath.locationsAmount());
        assertEquals(testRouteResult.distance(), savedPath.routeResult().distance());
        assertEquals(testRouteResult.time(), savedPath.routeResult().time());

        System.out.println("✅ Путь сохранен: " + savedPath.taskId());
    }

    @Test
    void getPathByTaskId_WithValidTaskId_ShouldReturnPath() {
        // Arrange
        pathService.savePath(testPathDto);

        // Act
        PathDto found = pathService.getPathByTaskId(TEST_TASK_ID);

        // Assert
        assertNotNull(found);
        assertEquals(TEST_TASK_ID, found.taskId());
        assertEquals(TEST_USER_ID, found.userId());
        assertEquals(TEST_TITLE, found.title());
        assertEquals(2, found.locationsAmount());

        System.out.println("✅ Найден путь по taskId: " + found.taskId());
    }

    @Test
    void getPathByTaskId_WithInvalidTaskId_ShouldThrowException() {
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> pathService.getPathByTaskId("non-existent-task"));

        assertEquals("Путь по задаче не найден", exception.getMessage());
        System.out.println("✅ Исключение выброшено для несуществующего taskId");
    }

    @Test
    void getPaths_ShouldReturnAllUserPaths() {
        // Arrange
        pathService.savePath(testPathDto);

        PathDto secondPath = PathDto.builder()
                .taskId("task-456")
                .title("Второй маршрут")
                .userId(TEST_USER_ID)
                .routeResult(testRouteResult)
                .locationsAmount(3)
                .locations(testLocations)
                .build();
        pathService.savePath(secondPath);

        // Act
        List<PathDto> paths = pathService.getPaths(TEST_USER_ID);

        // Assert
        assertNotNull(paths);
        assertEquals(2, paths.size());
        assertTrue(paths.stream().anyMatch(p -> p.taskId().equals(TEST_TASK_ID)));
        assertTrue(paths.stream().anyMatch(p -> p.taskId().equals("task-456")));

        System.out.println("✅ Найдено путей у пользователя: " + paths.size());
    }

    @Test
    void getPaths_WithNoPaths_ShouldReturnEmptyList() {
        // Act
        List<PathDto> paths = pathService.getPaths("non-existent-user");

        // Assert
        assertNotNull(paths);
        assertTrue(paths.isEmpty());

        System.out.println("✅ Пустой список путей для пользователя без маршрутов");
    }

    @Test
    void getPathById_WithValidIdAndUser_ShouldReturnPath() {
        // Arrange
        pathService.savePath(testPathDto);
        PathDto savedPath = pathService.getPathByTaskId(TEST_TASK_ID);

        // Нам нужно получить ID пути из БД, но в PathDto нет ID
        // Поэтому получаем через репозиторий
        var pathEntity = pathRepository.findByTaskId(TEST_TASK_ID).orElseThrow();
        String pathId = pathEntity.getKey();

        // Act
        PathDto found = pathService.getPathById(pathId, TEST_USER_ID);

        // Assert
        assertNotNull(found);
        assertEquals(TEST_TASK_ID, found.taskId());
        assertEquals(TEST_USER_ID, found.userId());

        System.out.println("✅ Найден путь по ID: " + pathId);
    }

    @Test
    void getPathById_WithInvalidId_ShouldThrowException() {
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> pathService.getPathById("invalid-id", TEST_USER_ID));

        assertEquals("Path not found", exception.getMessage());
        System.out.println("✅ Исключение при неверном ID пути");
    }

    @Test
    void getPathById_WithWrongUser_ShouldThrowAccessDenied() {
        // Arrange
        pathService.savePath(testPathDto);
        var pathEntity = pathRepository.findByTaskId(TEST_TASK_ID).orElseThrow();
        String pathId = pathEntity.getKey();

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> pathService.getPathById(pathId, OTHER_USER_ID));

        assertEquals("Access denied", exception.getMessage());
        System.out.println("✅ Доступ запрещен при попытке получить чужой путь");
    }

    @Test
    void updatePath_WithValidData_ShouldUpdateSuccessfully() {
        // Arrange
        pathService.savePath(testPathDto);
        var pathEntity = pathRepository.findByTaskId(TEST_TASK_ID).orElseThrow();
        String pathId = pathEntity.getKey();

        String newTitle = "Обновленный маршрут";
        PathDto updateDto = PathDto.builder()
                .title(newTitle)
                .build();

        // Act
        PathDto updated = pathService.updatePath(pathId, updateDto, TEST_USER_ID);

        // Assert
        assertNotNull(updated);
        assertEquals(newTitle, updated.title());

        // Проверяем, что обновилось в БД
        PathDto verified = pathService.getPathById(pathId, TEST_USER_ID);
        assertEquals(newTitle, verified.title());

        System.out.println("✅ Путь обновлен: " + updated.title());
    }

    @Test
    void updatePath_WithInvalidId_ShouldThrowException() {
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> pathService.updatePath("invalid-id", testPathDto, TEST_USER_ID));

        assertEquals("Path not found", exception.getMessage());
        System.out.println("✅ Исключение при обновлении несуществующего пути");
    }

    @Test
    void updatePath_WithWrongUser_ShouldThrowAccessDenied() {
        // Arrange
        pathService.savePath(testPathDto);
        var pathEntity = pathRepository.findByTaskId(TEST_TASK_ID).orElseThrow();
        String pathId = pathEntity.getKey();

        PathDto updateDto = PathDto.builder()
                .title("Чужое обновление")
                .build();

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> pathService.updatePath(pathId, updateDto, OTHER_USER_ID));

        assertEquals("Access denied", exception.getMessage());
        System.out.println("✅ Доступ запрещен при обновлении чужого пути");
    }

    @Test
    void deletePath_WithValidIdAndUser_ShouldDeleteSuccessfully() {
        // Arrange
        pathService.savePath(testPathDto);
        var pathEntity = pathRepository.findByTaskId(TEST_TASK_ID).orElseThrow();
        String pathId = pathEntity.getKey();

        assertTrue(pathRepository.findById(pathId).isPresent());

        // Act
        pathService.deletePath(pathId, TEST_USER_ID);

        // Assert
        assertFalse(pathRepository.findById(pathId).isPresent());

        System.out.println("✅ Путь удален: " + pathId);
    }

    @Test
    void deletePath_WithInvalidId_ShouldThrowException() {
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> pathService.deletePath("invalid-id", TEST_USER_ID));

        assertEquals("Path not found", exception.getMessage());
        System.out.println("✅ Исключение при удалении несуществующего пути");
    }

    @Test
    void deletePath_WithWrongUser_ShouldThrowAccessDenied() {
        // Arrange
        pathService.savePath(testPathDto);
        var pathEntity = pathRepository.findByTaskId(TEST_TASK_ID).orElseThrow();
        String pathId = pathEntity.getKey();

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> pathService.deletePath(pathId, OTHER_USER_ID));

        assertEquals("Access denied", exception.getMessage());

        // Проверяем, что путь не был удален
        assertTrue(pathRepository.findById(pathId).isPresent());

        System.out.println("✅ Доступ запрещен при удалении чужого пути");
    }

    @Test
    void saveMultiplePaths_ShouldSaveAllSuccessfully() {
        // Arrange
        List<String> taskIds = List.of("task-1", "task-2", "task-3");

        // Act
        for (String taskId : taskIds) {
            PathDto dto = PathDto.builder()
                    .taskId(taskId)
                    .title("Маршрут " + taskId)
                    .userId(TEST_USER_ID)
                    .routeResult(testRouteResult)
                    .locationsAmount(2)
                    .locations(testLocations)
                    .build();
            pathService.savePath(dto);
        }

        // Assert
        List<PathDto> paths = pathService.getPaths(TEST_USER_ID);
        assertEquals(3, paths.size());

        for (String taskId : taskIds) {
            PathDto found = pathService.getPathByTaskId(taskId);
            assertNotNull(found);
            assertEquals("Маршрут " + taskId, found.title());
        }

        System.out.println("✅ Сохранено " + paths.size() + " путей");
    }

    @Test
    void updatePath_OnlyTitle_ShouldNotAffectOtherFields() {
        // Arrange
        pathService.savePath(testPathDto);
        var pathEntity = pathRepository.findByTaskId(TEST_TASK_ID).orElseThrow();
        String pathId = pathEntity.getKey();

        String newTitle = "Только заголовок изменен";
        PathDto updateDto = PathDto.builder()
                .title(newTitle)
                .build();

        // Act
        PathDto updated = pathService.updatePath(pathId, updateDto, TEST_USER_ID);

        // Assert
        assertEquals(newTitle, updated.title());
        assertEquals(TEST_USER_ID, updated.userId());
        assertEquals(TEST_TASK_ID, updated.taskId());
        assertEquals(testRouteResult.distance(), updated.routeResult().distance());

        System.out.println("✅ Обновлен только заголовок, остальные поля не изменились");
    }
}