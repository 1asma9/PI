package edu.destination.entities;

import java.util.Objects;

public class Image {

    private int id;
    private String urlImage;
    private int destinationId;

    public Image() {}

    public Image(String urlImage, int destinationId) {
        this.urlImage = urlImage;
        this.destinationId = destinationId;
    }

    public Image(int id, String urlImage, int destinationId) {
        this.id = id;
        this.urlImage = urlImage;
        this.destinationId = destinationId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUrlImage() { return urlImage; }
    public void setUrlImage(String urlImage) { this.urlImage = urlImage; }
    public int getDestinationId() { return destinationId; }
    public void setDestinationId(int destinationId) { this.destinationId = destinationId; }

    @Override
    public String toString() {
        return "Image{id=" + id + ", urlImage='" + urlImage + "', destinationId=" + destinationId + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Image)) return false;
        Image image = (Image) o;
        return id == image.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}