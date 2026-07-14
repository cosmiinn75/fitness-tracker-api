package com.cosmin.fitness_tracker_api.DTO;



import java.time.LocalDate;

public record VolumeProgressResponse(
        LocalDate startDate,
        LocalDate endDate,
        Double volume
) {
}
