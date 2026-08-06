package com.cosmin.fitness_tracker_api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workout_template_exercises")
@Getter
@Setter
public class WorkoutTemplateExercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "exercise_number", nullable = false)
    private Integer exerciseNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workout_template_id", nullable = false)
    private WorkoutTemplate workoutTemplate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exercise_definition_id", nullable = false)
    private ExerciseDefinition exerciseDefinition;

    @OneToMany(
            mappedBy = "workoutTemplateExercise",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("setNumber ASC")
    @BatchSize(size = 50)
    private List<WorkoutTemplateSet> templateSets = new ArrayList<>();

    public WorkoutTemplateExercise() {
    }

    public void addTemplateSet(WorkoutTemplateSet templateSet) {
        templateSets.add(templateSet);
        templateSet.setWorkoutTemplateExercise(this);
    }



}