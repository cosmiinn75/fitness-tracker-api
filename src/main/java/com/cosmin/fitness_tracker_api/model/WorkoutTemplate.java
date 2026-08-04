package com.cosmin.fitness_tracker_api.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workout_templates")
public class WorkoutTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "template_name", nullable = false, length = 50)
    private String templateName;

    @Column(name = "normalized_name", nullable = false, length = 50)
    private String normalizedName;

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(
            mappedBy = "workoutTemplate",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("exerciseNumber ASC")
    private List<WorkoutTemplateExercise> templateExercises =
            new ArrayList<>();

    public WorkoutTemplate() {
    }

    public void addTemplateExercise(
            WorkoutTemplateExercise templateExercise
    ) {
        templateExercises.add(templateExercise);
        templateExercise.setWorkoutTemplate(this);
    }

    public void removeTemplateExercise(
            WorkoutTemplateExercise templateExercise
    ) {
        templateExercises.remove(templateExercise);
        templateExercise.setWorkoutTemplate(null);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getNormalizedName() {
        return normalizedName;
    }

    public void setNormalizedName(String normalizedName) {
        this.normalizedName = normalizedName;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<WorkoutTemplateExercise> getTemplateExercises() {
        return templateExercises;
    }

    public void setTemplateExercises(
            List<WorkoutTemplateExercise> templateExercises
    ) {
        this.templateExercises = templateExercises;
    }
}