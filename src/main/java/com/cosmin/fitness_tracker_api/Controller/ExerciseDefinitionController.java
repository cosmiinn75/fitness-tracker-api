package com.cosmin.fitness_tracker_api.Controller;

import com.cosmin.fitness_tracker_api.DTO.ExerciseDefinitionRequest;
import com.cosmin.fitness_tracker_api.DTO.ExerciseDefinitionResponse;
import com.cosmin.fitness_tracker_api.Service.ExerciseDefinitionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exercises")
public class ExerciseDefinitionController {

    private final ExerciseDefinitionService exerciseDefinitionService;

    public ExerciseDefinitionController(ExerciseDefinitionService exerciseDefinitionService) {
        this.exerciseDefinitionService = exerciseDefinitionService;
    }

    @GetMapping
    public List<ExerciseDefinitionResponse> getAllExercises(){
        return exerciseDefinitionService.findAllExerciseDefinitions();
    }

    @GetMapping("/{id}")
    public ExerciseDefinitionResponse getExercise(@PathVariable Long id){
        return exerciseDefinitionService.findExerciseDefinitionById(id);
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExerciseDefinitionResponse createExercise(@RequestBody @Valid ExerciseDefinitionRequest exerciseDefinitionRequest){
        return exerciseDefinitionService.addExerciseDefinition(exerciseDefinitionRequest);
    }

    @PutMapping("/{id}")
    public ExerciseDefinitionResponse updateExercise(@PathVariable Long id, @RequestBody @Valid ExerciseDefinitionRequest exerciseDefinitionRequest){
        return exerciseDefinitionService.updateExerciseDefinition(id, exerciseDefinitionRequest);
    }


}
