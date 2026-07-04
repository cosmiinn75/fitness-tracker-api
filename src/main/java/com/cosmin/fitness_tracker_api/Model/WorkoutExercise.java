package com.cosmin.fitness_tracker_api.Model;


import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workout_exercises")
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


    public List<ExerciseSet> getExerciseSets() {
        return exerciseSets;
    }

    public void setExerciseSets(List<ExerciseSet> exerciseSets) {
        this.exerciseSets = exerciseSets;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Workout getWorkout() {
        return workout;
    }

    public void setWorkout(Workout workout) {
        this.workout = workout;
    }

    public ExerciseDefinition getExerciseDefinition() {
        return exerciseDefinition;
    }

    public void setExerciseDefinition(ExerciseDefinition exerciseDefinition) {
        this.exerciseDefinition = exerciseDefinition;
    }

    public Integer getExerciseNumber() {
        return exerciseNumber;
    }

    public void setExerciseNumber(Integer exerciseNumber) {
        this.exerciseNumber = exerciseNumber;
    }
}
