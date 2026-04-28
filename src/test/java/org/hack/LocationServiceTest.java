package org.hack;

import org.hack.dto.*;
import org.hack.dto.request.GeneratePathRequest;
import org.hack.dto.request.LocationSearchRequest;
import org.hack.dto.response.GeneratePathResponse;
import org.hack.dto.response.LocationSearchResponse;
import org.hack.repository.LocationRepository;
import org.hack.service.LocationService;
import org.hack.service.PathService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
class LocationServiceTest {

    @Autowired
    private LocationService locationService;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private PathService pathService;

    private static final String TEST_USER_ID = "test-user-123";
    // Исправлено: lat (широта) должна быть в пределах 68.5-81.1, lon (долгота) в пределах 19.6-54.2
    private static final double TEST_LAT = 59.9343;  // широта (остается)
    private static final double TEST_LON = 30.3351;  // долгота (остается)
    private static final double[] TEST_COORDS = {59.9343, 30.3351};  // Исправлено: [lat, lon] а не [lon, lat]

    @BeforeEach
    void setUp() {
        locationRepository.findByUserId(TEST_USER_ID)
                .forEach(loc -> locationRepository.delete(loc));
    }

    @Test
    void createLocation_WithValidData_ShouldReturnCreatedLocation() {
        LocationDto dto = new LocationDto(null, null, "Эрмитаж",
                "Главный музей Санкт-Петербурга", TEST_COORDS,
                List.of("музей", "искусство", "история"));

        LocationDto created = locationService.create(dto, TEST_USER_ID);

        assertNotNull(created);
        assertNotNull(created.locationId());
        assertEquals(TEST_USER_ID, created.userId());
        assertEquals("Эрмитаж", created.title());
        assertTrue(created.tags().containsAll(List.of("музей", "искусство", "история")));
        System.out.println("✅ Создана локация: " + created.locationId());
    }

    @Test
    void searchLocations_ByCoordinatesAndRadius_ShouldReturnNearbyLocations() {
        LocationDto loc1 = locationService.create(
                new LocationDto(null, null, "Точка 1", "", TEST_COORDS, List.of("тест")), TEST_USER_ID);

        // Точка 2 - ближе, в пределах 500 метров
        LocationDto loc2 = locationService.create(
                new LocationDto(null, null, "Точка 2", "", new double[]{59.9355, 30.3320}, List.of("тест")), TEST_USER_ID);

        LocationSearchRequest request = new LocationSearchRequest(
                10, TEST_LAT, TEST_LON, 1000.0, List.of("тест"), false, false, null);

        LocationSearchResponse response = locationService.searchLocations(request, "");

        assertNotNull(response);
        assertTrue(response.locations().stream().map(LocationDto::locationId).toList()
                .containsAll(List.of(loc1.locationId(), loc2.locationId())));
        System.out.println("✅ Найдено поблизости: " + response.locations().size());
    }

    @Test
    void searchLocations_ByCoordinatesOnly_ShouldReturnAllNearby() {
        locationService.create(
                new LocationDto(null, null, "Точка А", "", TEST_COORDS, List.of("точка")), TEST_USER_ID);
        locationService.create(
                new LocationDto(null, null, "Точка Б", "", new double[]{TEST_LAT + 0.005, TEST_LON + 0.005}, List.of("другая")), TEST_USER_ID);  // Исправлено: [lat+0.005, lon+0.005]

        LocationSearchRequest request = new LocationSearchRequest(
                10, TEST_LAT, TEST_LON, 2000.0, null, false, false, null);

        LocationSearchResponse response = locationService.searchLocations(request, "");

        assertEquals(2, response.locations().size());
        System.out.println("✅ Все локации в радиусе: " + response.locations().size());
    }

    @Test
    void searchLocations_ByTagsOnly_ShouldReturnFilteredLocations() {
        locationService.create(
                new LocationDto(null, null, "Музей 1", "", TEST_COORDS, List.of("музей", "искусство")), TEST_USER_ID);
        locationService.create(
                new LocationDto(null, null, "Парк", "", new double[]{TEST_LAT + 0.01, TEST_LON + 0.01}, List.of("природа")), TEST_USER_ID);  // Исправлено: [lat+0.01, lon+0.01]
        locationService.create(
                new LocationDto(null, null, "Музей 2", "", new double[]{TEST_LAT - 0.01, TEST_LON - 0.01}, List.of("музей", "история")), TEST_USER_ID);  // Исправлено: [lat-0.01, lon-0.01]

        LocationSearchRequest request = new LocationSearchRequest(
                10, null, null, null, List.of("музей"), false, false, null);

        LocationSearchResponse response = locationService.searchLocations(request, "");

        assertEquals(2, response.locations().size());
        response.locations().forEach(loc -> assertTrue(loc.tags().contains("музей")));
        System.out.println("✅ Найдено музеев: " + response.locations().size());
    }

    @Test
    void searchLocations_WithLimit_ShouldReturnLimitedResults() {
        for (int i = 0; i < 5; i++) {
            locationService.create(
                    new LocationDto(null, null, "Локация " + i, "",
                            new double[]{TEST_LAT + (i * 0.001), TEST_LON + (i * 0.001)}, List.of("тест")), TEST_USER_ID);  // Исправлено: [lat+i*0.001, lon+i*0.001]
        }

        LocationSearchRequest request = new LocationSearchRequest(
                3, null, null, null, List.of("тест"), false, false, null);

        LocationSearchResponse response = locationService.searchLocations(request, "");

        assertEquals(3, response.locations().size());
        System.out.println("✅ Лимит 3: получено " + response.locations().size());
    }

    @Test
    void searchLocations_ByCoordinatesAndLimit_ShouldReturnLimitedNearby() {
        for (int i = 0; i < 5; i++) {
            locationService.create(
                    new LocationDto(null, null, "Точка " + i, "",
                            new double[]{TEST_LAT + (i * 0.002), TEST_LON + (i * 0.002)}, List.of("рядом")), TEST_USER_ID);  // Исправлено: [lat+i*0.002, lon+i*0.002]
        }

        LocationSearchRequest request = new LocationSearchRequest(
                2, TEST_LAT, TEST_LON, 3000.0, null, false, false, null);

        LocationSearchResponse response = locationService.searchLocations(request, "");

        assertEquals(2, response.locations().size());
        System.out.println("✅ Лимит 2 в радиусе: " + response.locations().size());
    }

    @Test
    void searchLocations_ByUserIds_ShouldReturnTheirLocations() {
        String anotherUserId = "another-user-456";
        locationService.create(
                new LocationDto(null, null, "Моя", "", TEST_COORDS, List.of()), TEST_USER_ID);
        locationService.create(
                new LocationDto(null, null, "Чужая", "", new double[]{TEST_LAT + 0.02, TEST_LON + 0.02}, List.of()), anotherUserId);  // Исправлено: [lat+0.02, lon+0.02]

        try {
            LocationSearchRequest request = new LocationSearchRequest(
                    10, null, null, null, null, false, false, List.of(TEST_USER_ID, anotherUserId));

            LocationSearchResponse response = locationService.searchLocations(request, "");

            assertEquals(2, response.locations().size());
            System.out.println("✅ Локации пользователей: " + response.locations().size());
        } finally {
            locationRepository.findByUserId(anotherUserId)
                    .forEach(loc -> locationRepository.delete(loc));
        }
    }

    @Test
    void updateLocation_WithValidData_ShouldUpdateSuccessfully() {
        LocationDto created = locationService.create(
                new LocationDto(null, null, "Оригинал", "Описание", TEST_COORDS, List.of("тег1")), TEST_USER_ID);

        LocationDto updateDto = new LocationDto(created.locationId(), null,
                "Обновлено", "Новое описание", new double[]{59.9400, 30.3200}, List.of("тег1", "тег2"));  // Исправлено: [lat, lon]

        LocationDto updated = locationService.update(updateDto, TEST_USER_ID);

        assertEquals("Обновлено", updated.title());
        assertEquals("Новое описание", updated.description());
        assertTrue(updated.tags().containsAll(List.of("тег1", "тег2")));
        System.out.println("✅ Локация обновлена: " + updated.title());
    }

    @Test
    void updateLocation_WithWrongUser_ShouldThrowException() {
        LocationDto created = locationService.create(
                new LocationDto(null, null, "Тест", "", TEST_COORDS, List.of()), TEST_USER_ID);

        LocationDto updateDto = new LocationDto(created.locationId(), null, "Хак", "", TEST_COORDS, List.of());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                locationService.update(updateDto, "wrong-user"));

        assertTrue(exception.getMessage().contains("не имеет право"));
        System.out.println("✅ Защита от чужого: " + exception.getMessage());
    }

    @Test
    void deleteLocation_WithValidId_ShouldDeleteSuccessfully() {
        LocationDto created = locationService.create(
                new LocationDto(null, null, "Удалить", "", TEST_COORDS, List.of()), TEST_USER_ID);

        locationService.delete(created.locationId(), TEST_USER_ID);

        assertTrue(locationService.findById(created.locationId()).isEmpty());
        System.out.println("✅ Локация удалена: " + created.locationId());
    }

    @Test
    void generatePath_WithMultipleLocations_ShouldReturnValidRoute() {
        LocationDto loc1 = locationService.create(
                new LocationDto(null, null, "Старт", "", TEST_COORDS, List.of()), TEST_USER_ID);
        LocationDto loc2 = locationService.create(
                new LocationDto(null, null, "Финиш", "", new double[]{59.9390, 30.3158}, List.of()), TEST_USER_ID);  // Исправлено: [lat, lon]

        GeneratePathRequest request = new GeneratePathRequest("t",
                List.of(new LocationWithOrder(loc1, 0), new LocationWithOrder(loc2, 1)), "task-123");

        GeneratePathResponse response = locationService.generatePath(request, TEST_USER_ID);

        assertNotNull(response);
        assertEquals(2, response.locationsAmount());
        assertTrue(response.overallTime() > 0);
        System.out.println("✅ Маршрут: " + response.routeResult().distance() + "м, " + response.overallTime() + "с");
    }

    @Test
    void generatePath_WithTaskId_ShouldSavePath() {
        LocationDto loc1 = locationService.create(
                new LocationDto(null, null, "А", "", TEST_COORDS, List.of()), TEST_USER_ID);
        LocationDto loc2 = locationService.create(
                new LocationDto(null, null, "Б", "", new double[]{59.9390, 30.3158}, List.of()), TEST_USER_ID);  // Исправлено: [lat, lon]

        String taskId = "task-789";
        GeneratePathRequest request = new GeneratePathRequest("t",
                List.of(new LocationWithOrder(loc1, 0), new LocationWithOrder(loc2, 1)), taskId);

        locationService.generatePath(request, TEST_USER_ID);

        PathDto savedPath = pathService.getPathByTaskId(taskId);
        assertNotNull(savedPath);
        assertEquals(taskId, savedPath.taskId());
        System.out.println("✅ Путь сохранен: " + taskId);
    }
}