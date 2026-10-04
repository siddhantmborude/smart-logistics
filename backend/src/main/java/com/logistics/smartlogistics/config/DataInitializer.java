package com.logistics.smartlogistics.config;

import com.logistics.smartlogistics.model.Distance;
import com.logistics.smartlogistics.model.Location;
import com.logistics.smartlogistics.repository.DistanceRepository;
import com.logistics.smartlogistics.repository.LocationRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner seed(LocationRepository locationRepo, DistanceRepository distanceRepo) {
        return args -> {
            if (locationRepo.count() > 0) return;

            Location warehouse = new Location("Warehouse", 18.5204, 73.8567);
            Location shivajinagar = new Location("Shivajinagar", 18.5308, 73.8470);
            Location kothrud = new Location("Kothrud", 18.5074, 73.8077);
            Location baner = new Location("Baner", 18.5590, 73.7868);
            Location wakad = new Location("Wakad", 18.5970, 73.7898);
            Location hadapsar = new Location("Hadapsar", 18.5089, 73.9259);

            List<Location> locations = locationRepo.saveAll(
                    List.of(warehouse, shivajinagar, kothrud, baner, wakad, hadapsar));

            double[][] d = {
                    {0, 6, 8, 12, 15, 10},
                    {6, 0, 7, 10, 13, 9},
                    {8, 7, 0, 6, 10, 11},
                    {12, 10, 6, 0, 5, 16},
                    {15, 13, 10, 5, 0, 18},
                    {10, 9, 11, 16, 18, 0}
            };

            for (int i = 0; i < locations.size(); i++) {
                for (int j = 0; j < locations.size(); j++) {
                    if (i != j) {
                        distanceRepo.save(new Distance(locations.get(i), locations.get(j), d[i][j]));
                    }
                }
            }
        };
    }
}
