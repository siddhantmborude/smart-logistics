package com.logistics.smartlogistics.service;

import com.logistics.smartlogistics.dto.RouteRequest;
import com.logistics.smartlogistics.dto.RouteResponse;
import com.logistics.smartlogistics.model.Distance;
import com.logistics.smartlogistics.model.Location;
import com.logistics.smartlogistics.repository.DistanceRepository;
import com.logistics.smartlogistics.repository.LocationRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RouteService {

    private final LocationRepository locationRepository;
    private final DistanceRepository distanceRepository;

    public RouteService(
            LocationRepository locationRepository,
            DistanceRepository distanceRepository) {

        this.locationRepository = locationRepository;
        this.distanceRepository = distanceRepository;
    }

    public List<Location> getLocations() {
        return locationRepository.findAll();
    }

    public RouteResponse solve(String algorithm, RouteRequest request) {

        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null.");
        }

        if (request.getStartId() == null) {
            throw new IllegalArgumentException("Starting location is required.");
        }

        String selectedAlgorithm = algorithm.toLowerCase();

        /*
         * =========================================================
         * DIJKSTRA
         * =========================================================
         *
         * Dijkstra uses:
         *   startId       -> source
         *   destinationId -> destination
         *
         * It uses the complete location graph so that an
         * intermediate location can be part of the shortest path.
         */
        if (selectedAlgorithm.equals("dijkstra")) {

            if (request.getDestinationId() == null) {
                throw new IllegalArgumentException(
                        "Destination location is required for Dijkstra."
                );
            }

            if (request.getStartId().equals(request.getDestinationId())) {
                throw new IllegalArgumentException(
                        "Starting location and destination must be different."
                );
            }

            List<Location> locations = locationRepository.findAll();

            Map<Long, Integer> index = new HashMap<>();

            for (int i = 0; i < locations.size(); i++) {
                index.put(locations.get(i).getId(), i);
            }

            Integer startIndex = index.get(request.getStartId());
            Integer targetIndex = index.get(request.getDestinationId());

            if (startIndex == null) {
                throw new IllegalArgumentException(
                        "Starting location not found: "
                                + request.getStartId()
                );
            }

            if (targetIndex == null) {
                throw new IllegalArgumentException(
                        "Destination location not found: "
                                + request.getDestinationId()
                );
            }

            double[][] graph =
                    new double[locations.size()][locations.size()];

            for (double[] row : graph) {
                Arrays.fill(row, Double.POSITIVE_INFINITY);
            }

            for (int i = 0; i < graph.length; i++) {
                graph[i][i] = 0;
            }

            for (Distance d : distanceRepository.findAll()) {

                if (d.getSource() == null ||
                        d.getDestination() == null) {
                    continue;
                }

                Integer a = index.get(d.getSource().getId());
                Integer b = index.get(d.getDestination().getId());

                if (a != null && b != null) {
                    graph[a][b] = d.getDistance();
                }
            }

            long begin = System.nanoTime();

            RouteAlgorithms.Result result =
                    RouteAlgorithms.dijkstra(
                            graph,
                            startIndex,
                            targetIndex
                    );

            long elapsed = System.nanoTime() - begin;

            List<Long> routeIds = result.route()
                    .stream()
                    .map(i -> locations.get(i).getId())
                    .toList();

            List<String> routeNames = result.route()
                    .stream()
                    .map(i -> locations.get(i).getName())
                    .toList();

            return new RouteResponse(
                    algorithm,
                    routeIds,
                    routeNames,
                    result.cost(),
                    elapsed,
                    result.nodesExplored()
            );
        }

        /*
         * =========================================================
         * GREEDY / BRANCH & BOUND
         * =========================================================
         */

        if (request.getLocationIds() == null) {
            throw new IllegalArgumentException(
                    "Delivery locations are required."
            );
        }

        List<Long> ids = request.getLocationIds()
                .stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));

        // Make sure starting location is included.
        if (!ids.contains(request.getStartId())) {
            ids.add(0, request.getStartId());
        }

        if (ids.size() < 2) {
            throw new IllegalArgumentException(
                    "Select at least two locations."
            );
        }

        List<Location> locations = ids.stream()
                .map(id -> locationRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Location not found: " + id
                                )))
                .collect(Collectors.toList());

        Map<Long, Integer> index = new HashMap<>();

        for (int i = 0; i < locations.size(); i++) {
            index.put(locations.get(i).getId(), i);
        }

        double[][] graph =
                new double[locations.size()][locations.size()];

        for (double[] row : graph) {
            Arrays.fill(row, Double.POSITIVE_INFINITY);
        }

        for (int i = 0; i < graph.length; i++) {
            graph[i][i] = 0;
        }

        for (Distance d : distanceRepository.findAll()) {

            if (d.getSource() == null ||
                    d.getDestination() == null) {
                continue;
            }

            Integer a = index.get(d.getSource().getId());
            Integer b = index.get(d.getDestination().getId());

            if (a != null && b != null) {
                graph[a][b] = d.getDistance();
            }
        }

        Integer startIndex = index.get(request.getStartId());

        if (startIndex == null) {
            throw new IllegalArgumentException(
                    "Starting location not found: "
                            + request.getStartId()
            );
        }

        long begin = System.nanoTime();

        RouteAlgorithms.Result result;

        switch (selectedAlgorithm) {

            case "greedy" ->
                    result = RouteAlgorithms.greedy(
                            graph,
                            startIndex
                    );

            case "branch-bound",
                 "branchandbound",
                 "branch_and_bound" ->
                    result = RouteAlgorithms.branchAndBound(
                            graph,
                            startIndex
                    );

            default ->
                    throw new IllegalArgumentException(
                            "Unknown algorithm: " + algorithm
                    );
        }

        long elapsed = System.nanoTime() - begin;

        List<Long> routeIds = result.route()
                .stream()
                .map(i -> locations.get(i).getId())
                .toList();

        List<String> routeNames = result.route()
                .stream()
                .map(i -> locations.get(i).getName())
                .toList();

        return new RouteResponse(
                algorithm,
                routeIds,
                routeNames,
                result.cost(),
                elapsed,
                result.nodesExplored()
        );
    }
}