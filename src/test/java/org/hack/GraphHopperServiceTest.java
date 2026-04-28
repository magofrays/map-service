package org.hack;

import org.hack.dto.PointDto;
import org.hack.dto.RouteResult;
import org.hack.service.GraphHopperService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
class GraphHopperServiceTest {

    @Autowired
    private GraphHopperService graphHopperService;

    @Test
    void calculateRouteBetweenTwoPoints_ShouldReturnValidRoute() {
        // Координаты в пределах вашей карты (Санкт-Петербург)
        double fromLat = 59.9343;
        double fromLon = 30.3351;
        double toLat = 59.9390;
        double toLon = 30.3158;

        RouteResult result = graphHopperService.calculateRoute(
                fromLat, fromLon, toLat, toLon);

        assertNotNull(result);
        assertTrue(result.distance() > 0, "Дистанция должна быть > 0");
        assertTrue(result.time() > 0, "Время должно быть > 0");
        assertFalse(result.points().isEmpty(), "Должны быть точки маршрута");
        assertFalse(result.instructions().isEmpty(), "Должны быть инструкции");

        System.out.println("Дистанция: " + result.distance() + " м");
        System.out.println("Время: " + result.time() + " сек");
        System.out.println("Количество точек: " + result.points().size());
        System.out.println("Количество инструкций: " + result.instructions().size());
    }

    @Test
    void calculateRouteWithMultiplePoints_ShouldReturnValidRoute() {
        List<PointDto> points = List.of(
                new PointDto(59.9343, 30.3351),
                new PointDto(59.9390, 30.3158),
                new PointDto(59.9440, 30.3200)
        );

        RouteResult result = graphHopperService.calculateRoute(points);

        assertNotNull(result);
        assertTrue(result.distance() > 0);
        assertTrue(result.points().size() >= points.size());

        System.out.println("Маршрут через " + points.size() + " точки:");
        System.out.println("Дистанция: " + result.distance() + " м");
        System.out.println("Время: " + result.time() + " сек");
    }

    @Test
    void calculateRoute_WithSamePoint_ShouldReturnMinimalRoute() {
        double lat = 59.9343;
        double lon = 30.3351;

        RouteResult result = graphHopperService.calculateRoute(lat, lon, lat, lon);

        assertNotNull(result);
        // Маршрут из точки в ту же точку должен быть 0 или очень маленьким
        assertTrue(result.distance() >= 0);
    }

    @Test
    void calculateRoute_WithFarPoints_ShouldReturnLongRoute() {
        // Тест для проверки что сервис вообще работает
        double fromLat = 59.9343;
        double fromLon = 30.3351;
        double toLat = 59.9999;
        double toLon = 30.3999;

        RouteResult result = graphHopperService.calculateRoute(
                fromLat, fromLon, toLat, toLon);

        assertNotNull(result);
        assertTrue(result.distance() > 1000, "Дальний маршрут должен быть > 1000 м");
    }
}