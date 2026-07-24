package com.cosmin.fitness_tracker_api.Model;

import com.cosmin.fitness_tracker_api.Enum.Status;
import jakarta.persistence.*;

import java.time.LocalDate;


@Entity
@Table(name = "training_goals")
public class TrainingGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id" , nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exercise_definition_id" , nullable = false )
    private ExerciseDefinition exerciseDefinition;

    @Column(name = "target_weight", nullable = false)
    private Double targetWeight;

    @Column(name = "target_reps" , nullable = false)
    private Integer targetReps;

    @Column(name = "target_date", nullable = false)
    private LocalDate targetDate;

    @Column(name = "created_at", nullable = false,updatable = false)
    private LocalDate createdAt;


    @Column(name = "status",nullable = false,length = 20)
    @Enumerated(value = EnumType.STRING)
    private Status status =  Status.ACTIVE;



    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public TrainingGoal() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
    }

    public Integer getTargetReps() {
        return targetReps;
    }

    public void setTargetReps(Integer targetReps) {
        this.targetReps = targetReps;
    }

    public Double getTargetWeight() {
        return targetWeight;
    }

    public void setTargetWeight(Double targetWeight) {
        this.targetWeight = targetWeight;
    }

    public ExerciseDefinition getExerciseDefinition() {
        return exerciseDefinition;
    }

    public void setExerciseDefinition(ExerciseDefinition exerciseDefinition) {
        this.exerciseDefinition = exerciseDefinition;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
