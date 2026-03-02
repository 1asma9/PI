package edu.destination.entities;

import java.util.Objects;

public class DestinationImage {

    private int idImage;
    private String urlImage;
    private int idDestination; // clé étrangère

    public DestinationImage() {}

    public DestinationImage(String urlImage, int idDestination) {
        this.urlImage = urlImage;
        this.idDestination = idDestination;
    }

    public DestinationImage(int idImage, String urlImage, int idDestination) {
        this.idImage = idImage;
        this.urlImage = urlImage;
        this.idDestination = idDestination;
    }

    public int getIdImage() {
        return idImage;
    }

    public void setIdImage(int idImage) {
        this.idImage = idImage;
    }

    public String getUrlImage() {
        return urlImage;
    }

    public void setUrlImage(String urlImage) {
        this.urlImage = urlImage;
    }

    public int getIdDestination() {
        return idDestination;
    }

    public void setIdDestination(int idDestination) {
        this.idDestination = idDestination;
    }

    @Override
    public String toString() {
        return "Image{" +
                "idImage=" + idImage +
                ", urlImage='" + urlImage + '\'' +
                ", idDestination=" + idDestination +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DestinationImage)) return false;
        DestinationImage image = (DestinationImage) o;
        return idImage == image.idImage;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idImage);
    }
}
