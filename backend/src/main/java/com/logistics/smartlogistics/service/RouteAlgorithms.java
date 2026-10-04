package com.logistics.smartlogistics.service;

import java.util.*;

public final class RouteAlgorithms {
    private RouteAlgorithms() {}

    public record Result(List<Integer> route, double cost, long nodesExplored) {}

    public static Result greedy(double[][] graph, int start) {
        int n = graph.length;
        boolean[] visited = new boolean[n];
        List<Integer> route = new ArrayList<>();
        int current = start;
        double cost = 0;
        long explored = 0;

        visited[current] = true;
        route.add(current);

        for (int step = 1; step < n; step++) {
            int next = -1;
            double best = Double.POSITIVE_INFINITY;

            for (int j = 0; j < n; j++) {
                if (!visited[j] && graph[current][j] < best) {
                    best = graph[current][j];
                    next = j;
                }
            }

            if (next == -1) throw new IllegalArgumentException("Graph is disconnected.");
            visited[next] = true;
            route.add(next);
            cost += best;
            current = next;
            explored++;
        }

        cost += graph[current][start];
        route.add(start);
        return new Result(route, cost, explored);
    }

    public static Result branchAndBound(double[][] graph, int start) {
        int n = graph.length;
        boolean[] visited = new boolean[n];
        int[] currentPath = new int[n + 1];
        int[] bestPath = new int[n + 1];

        currentPath[0] = start;
        visited[start] = true;

        double initialBound = firstTwoMinSum(graph) / 2.0;
        BnBState state = new BnBState(Double.POSITIVE_INFINITY, 0, 0);

        bnb(graph, start, visited, currentPath, bestPath, 1, 0, initialBound, state);

        List<Integer> route = new ArrayList<>();
        for (int i = 0; i <= n; i++) route.add(bestPath[i]);
        return new Result(route, state.bestCost, state.nodesExplored);
    }

    private static void bnb(double[][] g, int start, boolean[] visited,
                            int[] path, int[] bestPath, int level,
                            double currentCost, double bound, BnBState state) {
        int n = g.length;
        state.nodesExplored++;

        if (level == n) {
            int last = path[level - 1];
            if (g[last][start] == Double.POSITIVE_INFINITY) return;
            double total = currentCost + g[last][start];
            if (total < state.bestCost) {
                state.bestCost = total;
                System.arraycopy(path, 0, bestPath, 0, n);
                bestPath[n] = start;
            }
            return;
        }

        int current = path[level - 1];

        for (int next = 0; next < n; next++) {
            if (!visited[next] && g[current][next] < Double.POSITIVE_INFINITY) {
                double nextCost = currentCost + g[current][next];
                double nextBound = bound;

                if (level == 1) {
                    nextBound -= (firstMin(g, current) + firstMin(g, next)) / 2.0;
                } else {
                    nextBound -= (secondMin(g, current) + firstMin(g, next)) / 2.0;
                }

                if (nextCost + nextBound < state.bestCost) {
                    path[level] = next;
                    visited[next] = true;
                    bnb(g, start, visited, path, bestPath, level + 1,
                            nextCost, nextBound, state);
                    visited[next] = false;
                }
            }
        }
    }

    private static class BnBState {
        double bestCost;
        long nodesExplored;
        BnBState(double bestCost, long nodesExplored, long unused) {
            this.bestCost = bestCost;
            this.nodesExplored = nodesExplored;
        }
    }

    private static double firstTwoMinSum(double[][] g) {
        double sum = 0;
        for (int i = 0; i < g.length; i++) {
            sum += firstMin(g, i) + secondMin(g, i);
        }
        return sum;
    }

    private static double firstMin(double[][] g, int i) {
        double min = Double.POSITIVE_INFINITY;
        for (int j = 0; j < g.length; j++) {
            if (i != j) min = Math.min(min, g[i][j]);
        }
        return min;
    }

    private static double secondMin(double[][] g, int i) {
        double first = Double.POSITIVE_INFINITY;
        double second = Double.POSITIVE_INFINITY;
        for (int j = 0; j < g.length; j++) {
            if (i == j) continue;
            double value = g[i][j];
            if (value <= first) {
                second = first;
                first = value;
            } else if (value < second) {
                second = value;
            }
        }
        return second;
    }

    public static Result dijkstra(double[][] graph, int start, int target) {
        int n = graph.length;
        double[] dist = new double[n];
        int[] prev = new int[n];
        boolean[] used = new boolean[n];
        Arrays.fill(dist, Double.POSITIVE_INFINITY);
        Arrays.fill(prev, -1);
        dist[start] = 0;

        long explored = 0;
        for (int k = 0; k < n; k++) {
            int u = -1;
            double best = Double.POSITIVE_INFINITY;
            for (int i = 0; i < n; i++) {
                if (!used[i] && dist[i] < best) {
                    best = dist[i];
                    u = i;
                }
            }
            if (u == -1) break;
            used[u] = true;
            explored++;
            if (u == target) break;

            for (int v = 0; v < n; v++) {
                if (!used[v] && graph[u][v] < Double.POSITIVE_INFINITY
                        && dist[u] + graph[u][v] < dist[v]) {
                    dist[v] = dist[u] + graph[u][v];
                    prev[v] = u;
                }
            }
        }

        if (Double.isInfinite(dist[target])) {
            throw new IllegalArgumentException("No path exists.");
        }

        LinkedList<Integer> path = new LinkedList<>();
        for (int at = target; at != -1; at = prev[at]) path.addFirst(at);
        return new Result(path, dist[target], explored);
    }
}
