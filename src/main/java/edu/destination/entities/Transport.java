package edu.destination.entities;


import java.util.Objects;

public class Transport {

    private int id;
    private String typeTransport;
    private int voyageId; // clé étrangère vers voyage

    public Transport() {}

    public Transport(String typeTransport, int voyageId) {
        this.typeTransport = typeTransport;
        this.voyageId = voyageId;
    }

    public Transport(int id, String typeTransport, int voyageId) {
        this.id = id;
        this.typeTransport = typeTransport;
        this.voyageId = voyageId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTypeTransport() { return typeTransport; }
    public void setTypeTransport(String typeTransport) { this.typeTransport = typeTransport; }
    public int getVoyageId() { return voyageId; }
    public void setVoyageId(int voyageId) { this.voyageId = voyageId; }

    @Override
    public String toString() {
        return "Transport{id=" + id + ", typeTransport='" + typeTransport + "', voyageId=" + voyageId + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Transport t)) return false;
        return id == t.id && voyageId == t.voyageId && Objects.equals(typeTransport, t.typeTransport);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, typeTransport, voyageId);
    }
}