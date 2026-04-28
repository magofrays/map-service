package org.hack.service;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import com.graphhopper.util.Instruction;
import com.graphhopper.util.Translation;
import com.graphhopper.util.TranslationMap;
import com.graphhopper.util.shapes.GHPoint;
import com.graphhopper.util.shapes.GHPoint3D;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hack.dto.InstructionInfo;
import org.hack.dto.PointDto;
import org.hack.dto.RouteResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class GraphHopperService {

    private final GraphHopper graphHopper;
    private final Translation translation = new TranslationMap().doImport().get("ru");

    public RouteResult calculateRoute(double fromLat, double fromLon,
                                      double toLat, double toLon) {
        GHRequest request = new GHRequest(fromLat, fromLon, toLat, toLon)
                .setProfile("foot")
                .setLocale(new Locale("ru"));

        return executeRoute(request);
    }

    public RouteResult calculateRoute(List<PointDto> points) {
        List<GHPoint> ghPoints = points.stream()
                .map(p -> new GHPoint(p.lat(), p.lon()))
                .toList();

        GHRequest request = new GHRequest(ghPoints)
                .setProfile("foot")
                .setLocale(new Locale("ru"));

        return executeRoute(request);
    }

    private RouteResult executeRoute(GHRequest request) {
        GHResponse response = graphHopper.route(request);

        if (response.hasErrors()) {
            String error = response.getErrors().get(0).getMessage();
            throw new RuntimeException("Ошибка маршрута: " + error);
        }

        ResponsePath path = response.getBest();

        // Извлекаем точки
        List<PointDto> points = new ArrayList<>();
        for (GHPoint3D p : path.getPoints()) {
            points.add(new PointDto(p.getLat(), p.getLon()));
        }

        // Извлекаем инструкции
        List<InstructionInfo> instructions = new ArrayList<>();
        for (Instruction inst : path.getInstructions()) {
            instructions.add(new InstructionInfo(
                    inst.getDistance(),
                    inst.getTime() / 1000.0,
                    inst.getTurnDescription(translation),  // Используем Translation
                    inst.getName()
            ));
        }

        return new RouteResult(
                path.getDistance(),
                path.getTime() / 1000.0,
                points,
                instructions
        );
    }
}