package com.cosmin.fitness_tracker_api.Controller;

import com.cosmin.fitness_tracker_api.DTO.PersonalRecordResponse;
import com.cosmin.fitness_tracker_api.DTO.VolumeProgressResponse;
import com.cosmin.fitness_tracker_api.DTO.WorkoutVolumeResponse;
import com.cosmin.fitness_tracker_api.Service.ProgressService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/progress")
public class ProgressController {

    private final ProgressService progressService;
    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    @GetMapping("/weekly-volume")
    public VolumeProgressResponse  getWeeklyVolume() {
        return progressService.getWeeklyVolume();
    }

    @GetMapping("/monthly-volume")
    public VolumeProgressResponse  getMonthlyVolume() {
        return progressService.getMonthlyVolume();
    }

    @GetMapping("/workouts/{workoutId}/volume")
    public WorkoutVolumeResponse getWorkoutVolume(@PathVariable Long workoutId) {
        return progressService.getWorkoutVolumeById(workoutId);
    }

    @GetMapping("/exercises/{exerciseDefinitionId}/personal-record")
    public PersonalRecordResponse getPersonalRecord(@PathVariable Long exerciseDefinitionId) {
        return progressService.getPersonalRecordById(exerciseDefinitionId);
    }

}
