package edu.destination.entities;

import java.util.Objects;

public class Transport {

    private int idTransport;
    private String typeTransport;
    private int idDestination; // clé étrangère

    public Transport() {}

    public Transport(String typeTransport, int idDestination) {
        this.typeTransport = typeTransport;
        this.idDestination = idDestination;
    }

    public Transport(int idTransport, String typeTransport, int idDestination) {
        this.idTransport = idTransport;
        this.typeTransport = typeTransport;
        this.idDestination = idDestination;
    }

    public int getIdTransport() {
        return idTransport;
    }

    public void setIdTransport(int idTransport) {
        this.idTransport = idTransport;
    }

    public String getTypeTransport() {
        return typeTransport;
    }

    public void setTypeTransport(String typeTransport) {
        this.typeTransport = typeTransport;
    }



    public int getIdDestination() {
        return idDestination;
    }

    public void setIdDestination(int idDestination) {
        this.idDestination = idDestination;
    }

    @Override
    public String toString() {
        return "Transport{" +
                "idTransport=" + idTransport +
                ", typeTransport='" + typeTransport + '\'' +
                ", idDestination=" + idDestination +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Transport transport)) return false;
        return idTransport == transport.idTransport  && idDestination == transport.idDestination && Objects.equals(typeTransport, transport.typeTransport);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idTransport, typeTransport,  idDestination);
    }
}
