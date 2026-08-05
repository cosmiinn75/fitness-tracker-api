package com.cosmin.fitness_tracker_api.model;


import com.cosmin.fitness_tracker_api.Enum.ExerciseType;
import com.cosmin.fitness_tracker_api.Enum.MuscleGroup;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Entity
@Setter
@Table(name = "exercise_definitions")
public class ExerciseDefinition {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "normalized_name", nullable = false)
    private String normalizedName;

    @Enumerated(EnumType.STRING)
    @Column(name = "exercise_type" , nullable = false)
    private ExerciseType exerciseType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(nullable = false)
    private boolean archived = false;

    @Enumerated(EnumType.STRING)
    private MuscleGroup muscleGroup;



    public ExerciseDefinition() {
    }


}
