package edu.connexion3a8.entities;

public class BlogRating {
    private int id;
    private int blogId;
    private String userName;
    private int rating;
    private String reviewText;
    private String createdAt;

    // Constructeurs
    public BlogRating() {}

    public BlogRating(int blogId, String userName, int rating, String reviewText) {
        this.blogId = blogId;
        this.userName = userName;
        this.rating = rating;
        this.reviewText = reviewText;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getBlogId() { return blogId; }
    public void setBlogId(int blogId) { this.blogId = blogId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getReviewText() { return reviewText; }
    public void setReviewText(String reviewText) { this.reviewText = reviewText; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}