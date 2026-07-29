package com.cosmin.fitness_tracker_api.Model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workout_template_exercises")
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
    private List<WorkoutTemplateSet> templateSets = new ArrayList<>();

    public WorkoutTemplateExercise() {
    }

    public void addTemplateSet(WorkoutTemplateSet templateSet) {
        templateSets.add(templateSet);
        templateSet.setWorkoutTemplateExercise(this);
    }

    public void removeTemplateSet(WorkoutTemplateSet templateSet) {
        templateSets.remove(templateSet);
        templateSet.setWorkoutTemplateExercise(null);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getExerciseNumber() {
        return exerciseNumber;
    }

    public void setExerciseNumber(Integer exerciseNumber) {
        this.exerciseNumber = exerciseNumber;
    }

    public WorkoutTemplate getWorkoutTemplate() {
        return workoutTemplate;
    }

    public void setWorkoutTemplate(WorkoutTemplate workoutTemplate) {
        this.workoutTemplate = workoutTemplate;
    }

    public ExerciseDefinition getExerciseDefinition() {
        return exerciseDefinition;
    }

    public void setExerciseDefinition(
            ExerciseDefinition exerciseDefinition
    ) {
        this.exerciseDefinition = exerciseDefinition;
    }

    public List<WorkoutTemplateSet> getTemplateSets() {
        return templateSets;
    }

    public void setTemplateSets(List<WorkoutTemplateSet> templateSets) {
        this.templateSets = templateSets;
    }
}