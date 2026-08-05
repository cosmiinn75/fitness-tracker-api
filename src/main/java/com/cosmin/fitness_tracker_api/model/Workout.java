package com.cosmin.fitness_tracker_api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workouts")
@Getter
@Setter
public class Workout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String workoutName;

    private LocalDate date;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;


    @OneToMany(mappedBy = "workout" , cascade = CascadeType.ALL , orphanRemoval = true)
    private List<WorkoutExercise> workoutExercises = new ArrayList<>();

    public Workout() {
    }


}
