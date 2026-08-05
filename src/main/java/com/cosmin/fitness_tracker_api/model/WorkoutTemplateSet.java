package com.cosmin.fitness_tracker_api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "workout_template_sets")
@Getter
@Setter
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


}