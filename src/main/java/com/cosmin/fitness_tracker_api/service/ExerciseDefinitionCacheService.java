package com.cosmin.fitness_tracker_api.service;

import com.cosmin.fitness_tracker_api.DTO.ExerciseDefinitionResponse;
import com.cosmin.fitness_tracker_api.Enum.ExerciseType;
import com.cosmin.fitness_tracker_api.exception.ExerciseDefinitionNotFoundException;
import com.cosmin.fitness_tracker_api.mapper.ExerciseDefinitionMapper;
import com.cosmin.fitness_tracker_api.model.ExerciseDefinition;
import com.cosmin.fitness_tracker_api.repository.ExerciseDefinitionRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ExerciseDefinitionCacheService {

    private final ExerciseDefinitionRepository exerciseDefinitionRepository;
    private final ExerciseDefinitionMapper exerciseDefinitionMapper;

    public ExerciseDefinitionCacheService(ExerciseDefinitionRepository exerciseDefinitionRepository, ExerciseDefinitionMapper exerciseDefinitionMapper) {
        this.exerciseDefinitionRepository = exerciseDefinitionRepository;
        this.exerciseDefinitionMapper = exerciseDefinitionMapper;
    }


    @Cacheable(
            cacheNames = "exerciseDefinitions",
            key = "#username"
    )
    @Transactional(readOnly = true)
    public List<ExerciseDefinitionResponse> findAll(String username){
        return new ArrayList<>(
                exerciseDefinitionRepository.findAllAccessible(
                        username, ExerciseType.SYSTEM
                ).stream().map(
                        exerciseDefinitionMapper::toResponse
                ).toList()
        );
    }

    @Cacheable(cacheNames = "exerciseDefinition",
            key = "#username + ':' + #id"
    )
    @Transactional(readOnly = true)
    public ExerciseDefinitionResponse findById(String username,Long id){
        return exerciseDefinitionMapper.toResponse(
                exerciseDefinitionRepository.
                        findByIdAccessible(id,username,ExerciseType.SYSTEM)
                        .orElseThrow(() ->
                                new ExerciseDefinitionNotFoundException("Exercise definition with id " + id + " not found")
                        ));

    }


    @Caching(
            evict = {
                    @CacheEvict(
                            cacheNames = "exerciseDefinitions",
                            key = "#username"
                    ),
                    @CacheEvict(
                            cacheNames = "exerciseDefinition",
                            key = "#username + ':' + #id"
                    )
            }
    )
    public void evictListAndExercise(String username, Long id){

    }



    @CacheEvict(
            cacheNames = "exerciseDefinitions",
            key = "#username"
    )
    public void evictList(String username){

    }



}
