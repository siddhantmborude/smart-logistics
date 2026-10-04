package com.logistics.smartlogistics.model;

import jakarta.persistence.*;

@Entity
@Table(name = "distances",
       uniqueConstraints = @UniqueConstraint(columnNames = {"source_id", "destination_id"}))
public class Distance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "source_id")
    private Location source;

    @ManyToOne(optional = false)
    @JoinColumn(name = "destination_id")
    private Location destination;

    @Column(nullable = false)
    private double distance;

    public Distance() {}

    public Distance(Location source, Location destination, double distance) {
        this.source = source;
        this.destination = destination;
        this.distance = distance;
    }

    public Long getId() { return id; }
    public Location getSource() { return source; }
    public Location getDestination() { return destination; }
    public double getDistance() { return distance; }

    public void setId(Long id) { this.id = id; }
    public void setSource(Location source) { this.source = source; }
    public void setDestination(Location destination) { this.destination = destination; }
    public void setDistance(double distance) { this.distance = distance; }
}
