package org.cheplay.dto;

public class MovieWatch {
    public String movieId;
    public String title;
    public String genre;
    public Integer year;
    public Integer duration;
    public Integer watchCount;

    public MovieWatch() {}

    public MovieWatch(
        String movieId,
        String title,
        String genre,
        Integer year,
        Integer duration,
        Integer watchCount
    ) {
        this.movieId = movieId;
        this.title = title;
        this.genre = genre;
        this.year = year;
        this.duration = duration;
        this.watchCount = watchCount;
    }
}

