package com.cosmin.fitness_tracker_api.model;

import com.cosmin.fitness_tracker_api.Enum.Status;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;


@Entity
@Table(name = "training_goals")
@Getter
@Setter
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


}
