package edu.campus.library.domain.room;

import edu.campus.library.domain.Identifiable;

import java.io.Serializable;

/** A bookable study or meeting room. staffOnly rooms are protected by a proxy. */
public record Room(String id, String name, int capacity, boolean staffOnly)
        implements Identifiable, Serializable {
}
