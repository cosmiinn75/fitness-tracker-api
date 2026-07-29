package com.cosmin.fitness_tracker_api.Model;

import jakarta.persistence.*;

@Entity
@Table(name = "workout_template_sets")
public class WorkoutTemplateSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "set_number", nullable = false)
    private Integer setNumber;

    @Column(name = "target_weight")
    private Double targetWeight;

    @Column(name = "target_reps", nullable = false)
    private Integer targetReps;

    @Column(name = "target_rir")
    private Integer targetRir;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "workout_template_exercise_id",
            nullable = false
    )
    private WorkoutTemplateExercise workoutTemplateExercise;

    public WorkoutTemplateSet() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getSetNumber() {
        return setNumber;
    }

    public void setSetNumber(Integer setNumber) {
        this.setNumber = setNumber;
    }

    public Double getTargetWeight() {
        return targetWeight;
    }

    public void setTargetWeight(Double targetWeight) {
        this.targetWeight = targetWeight;
    }

    public Integer getTargetReps() {
        return targetReps;
    }

    public void setTargetReps(Integer targetReps) {
        this.targetReps = targetReps;
    }

    public Integer getTargetRir() {
        return targetRir;
    }

    public void setTargetRir(Integer targetRir) {
        this.targetRir = targetRir;
    }

    public WorkoutTemplateExercise getWorkoutTemplateExercise() {
        return workoutTemplateExercise;
    }

    public void setWorkoutTemplateExercise(
            WorkoutTemplateExercise workoutTemplateExercise
    ) {
        this.workoutTemplateExercise = workoutTemplateExercise;
    }
}