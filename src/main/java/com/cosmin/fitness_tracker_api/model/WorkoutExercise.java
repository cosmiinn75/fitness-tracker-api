package com.cosmin.fitness_tracker_api.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workout_exercises")
@Getter
@Setter
public class WorkoutExercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "workout_id")
    private Workout workout;

    @ManyToOne
    @JoinColumn(name = "exercise_definition_id")
    private ExerciseDefinition exerciseDefinition;


    @OneToMany(mappedBy = "workoutExercise" , cascade = CascadeType.ALL , orphanRemoval = true)
    private List<ExerciseSet> exerciseSets = new ArrayList<>();


    private Integer exerciseNumber;

    public WorkoutExercise() {
    }

}
