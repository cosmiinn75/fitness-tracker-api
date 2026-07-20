package com.cosmin.fitness_tracker_api.Service;

import com.cosmin.fitness_tracker_api.DTO.ExerciseDefinitionRequest;
import com.cosmin.fitness_tracker_api.DTO.ExerciseDefinitionResponse;
import com.cosmin.fitness_tracker_api.Exception.ExerciseDefinitionNotFoundException;
import com.cosmin.fitness_tracker_api.Exception.NameAlreadyExistsException;
import com.cosmin.fitness_tracker_api.Model.ExerciseDefinition;
import com.cosmin.fitness_tracker_api.Repository.ExerciseDefinitionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ExerciseDefinitionService {

    private final ExerciseDefinitionRepository exerciseDefinitionRepository;

    public ExerciseDefinitionService(ExerciseDefinitionRepository exerciseDefinitionRepository) {
        this.exerciseDefinitionRepository = exerciseDefinitionRepository;
    }

    @Transactional
    public ExerciseDefinitionResponse addExerciseDefinition(ExerciseDefinitionRequest request) {
        if (exerciseDefinitionRepository.existsByName(request.exerciseName())) {
            throw new NameAlreadyExistsException("Exercise name already exists");
        }

        ExerciseDefinition exerciseDefinition = new ExerciseDefinition();
        exerciseDefinition.setName(request.exerciseName());
        exerciseDefinition.setMuscleGroup(request.muscleGroup());

        ExerciseDefinition savedExerciseDefinition = exerciseDefinitionRepository.save(exerciseDefinition);

        return toExerciseDefinitionResponse(savedExerciseDefinition);
    }

    @Transactional(readOnly = true)
    public List<ExerciseDefinitionResponse> findAllExerciseDefinitions() {
        return exerciseDefinitionRepository.findAll()
                .stream()
                .map(this::toExerciseDefinitionResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ExerciseDefinitionResponse findExerciseDefinitionById(Long id) {
        ExerciseDefinition exerciseDefinition = exerciseDefinitionRepository.findById(id)
                .orElseThrow(() -> new ExerciseDefinitionNotFoundException("Exercise definition not found"));

        return toExerciseDefinitionResponse(exerciseDefinition);
    }

    @Transactional
    public ExerciseDefinitionResponse updateExerciseDefinition(Long id, ExerciseDefinitionRequest request) {
        ExerciseDefinition exerciseDefinition = exerciseDefinitionRepository.findById(id)
                .orElseThrow(() -> new ExerciseDefinitionNotFoundException("Exercise definition not found"));

        if(!exerciseDefinition.getName().equals(request.exerciseName()) && exerciseDefinitionRepository.existsByNameIgnoreCase((request.exerciseName()))) {
            throw new  NameAlreadyExistsException("Exercise name already exists");
        }


        exerciseDefinition.setName(request.exerciseName());
        exerciseDefinition.setMuscleGroup(request.muscleGroup());

        ExerciseDefinition savedExerciseDefinition = exerciseDefinitionRepository.save(exerciseDefinition);

        return toExerciseDefinitionResponse(savedExerciseDefinition);
    }



    private ExerciseDefinitionResponse toExerciseDefinitionResponse(ExerciseDefinition exerciseDefinition) {
        return new ExerciseDefinitionResponse(
                exerciseDefinition.getId(),
                exerciseDefinition.getName(),
                exerciseDefinition.getMuscleGroup()
        );
    }
}