# Project Notes

## Main DAA objectives

1. Model delivery points as a weighted graph.
2. Implement Greedy Nearest Neighbor.
3. Implement TSP using Branch and Bound.
4. Implement Dijkstra's shortest path.
5. Compare distance and execution time.
6. Explain time/space complexity during viva.

## Complexity

- Greedy nearest-neighbor: O(n^2)
- Dijkstra with adjacency matrix: O(V^2)
- TSP Branch & Bound: worst case remains O(n!), but pruning can reduce the practical search.
- TSP brute force: O(n!)

## Viva points

Branch & Bound maintains a best-known solution and a lower bound for partial routes. A partial route is discarded when its estimated minimum possible cost cannot improve the current best solution.
