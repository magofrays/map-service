package org.hack.configuration;

import com.graphhopper.GraphHopper;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.Profile;
import com.graphhopper.json.Statement;
import com.graphhopper.util.CustomModel;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Slf4j
@Configuration
public class GraphHopperConfiguration {

    @Value("${graphhopper.osm-file}")
    private String osmFile;

    @Value("${graphhopper.graph-location}")
    private String graphLocation;

    @Bean
    public GraphHopper graphHopper() {
        log.info("Init GraphHopper для пешеходных маршрутов...");
        String osmPath = osmFile;
        log.info("OSM-file: {}", osmPath);

        CustomModel footModel = new CustomModel();

        footModel.getSpeed().add(
                Statement.If("true", Statement.Op.LIMIT, "5")
        );
        footModel.getPriority().add(
                Statement.If("true", Statement.Op.MULTIPLY, "1.0")
        );

        Profile footProfile = new Profile("foot")
                .setCustomModel(footModel);

        GraphHopper hopper = new GraphHopper();
        hopper.setOSMFile(osmPath);
        hopper.setGraphHopperLocation(graphLocation);
        hopper.setProfiles(List.of(footProfile));
        hopper.getCHPreparationHandler()
                .setCHProfiles(List.of(new CHProfile("foot")));

        log.info("Загрузка графа...");
        long start = System.currentTimeMillis();
        hopper.importOrLoad();
        log.info("Граф загружен за {} секунд",
                (System.currentTimeMillis() - start) / 1000);

        return hopper;
    }

    @PreDestroy
    public void destroy() {
        log.info("GraphHopper destroying");
    }
}