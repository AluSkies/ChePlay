package org.cheplay.dto;

public class MovieRating {
    public String movieId;
    public String title;
    public String genre;
    public Integer year;
    public Integer duration;
    public Double rating;

    public MovieRating() {}

    public MovieRating(
        String movieId,
        String title,
        String genre,
        Integer year,
        Integer duration,
        Double rating
    ) {
        this.movieId = movieId;
        this.title = title;
        this.genre = genre;
        this.year = year;
        this.duration = duration;
        this.rating = rating;
    }
}

