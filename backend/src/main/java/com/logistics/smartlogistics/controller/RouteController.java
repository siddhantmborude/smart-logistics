package com.logistics.smartlogistics.controller;

import com.logistics.smartlogistics.dto.RouteRequest;
import com.logistics.smartlogistics.dto.RouteResponse;
import com.logistics.smartlogistics.model.Location;
import com.logistics.smartlogistics.service.RouteService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class RouteController {
    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping("/locations")
    public List<Location> locations() {
        return routeService.getLocations();
    }

    @PostMapping("/routes/{algorithm}")
    public RouteResponse route(@PathVariable String algorithm, @RequestBody RouteRequest request) {
        return routeService.solve(algorithm, request);
    }
}
