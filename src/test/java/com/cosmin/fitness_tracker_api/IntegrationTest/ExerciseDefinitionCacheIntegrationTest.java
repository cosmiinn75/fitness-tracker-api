package com.cosmin.fitness_tracker_api.IntegrationTest;

import com.cosmin.fitness_tracker_api.Enum.ExerciseType;
import com.cosmin.fitness_tracker_api.Enum.MuscleGroup;
import com.cosmin.fitness_tracker_api.model.ExerciseDefinition;
import com.cosmin.fitness_tracker_api.repository.ExerciseDefinitionRepository;
import com.cosmin.fitness_tracker_api.service.ExerciseDefinitionCacheService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {
        "spring.cache.type=redis"
})
@ActiveProfiles("test")
@Import(ExerciseDefinitionCacheIntegrationTest.ContainersConfig.class)
class ExerciseDefinitionCacheIntegrationTest {

    @Autowired
    private ExerciseDefinitionCacheService exerciseDefinitionCacheService;

    @Autowired
    private CacheManager cacheManager;

    @MockitoBean
    private ExerciseDefinitionRepository exerciseDefinitionRepository;


    @TestConfiguration(proxyBeanMethods = false)
    static class ContainersConfig {

        @Bean
        @ServiceConnection
        MySQLContainer mySQLContainer() {
            return new MySQLContainer("mysql:8.0")
                    .withDatabaseName("fitness_tracker_test")
                    .withUsername("test")
                    .withPassword("test");
        }

        @Bean
        @ServiceConnection(name = "redis")
        GenericContainer<?> redisContainer() {
            return new GenericContainer<>(
                    DockerImageName.parse("redis:8-alpine")
            ).withExposedPorts(6379);
        }
    }


    @BeforeEach
    void clearCaches() {

        Cache exerciseDefinitions =
                cacheManager.getCache("exerciseDefinitions");

        if (exerciseDefinitions != null) {
            exerciseDefinitions.clear();
        }

        Cache exerciseDefinition =
                cacheManager.getCache("exerciseDefinition");

        if (exerciseDefinition != null) {
            exerciseDefinition.clear();
        }

        reset(exerciseDefinitionRepository);
    }


    @Test
    void findAll_ShouldUseRedisCache() {

        String username = "cosmin";

        assertInstanceOf(
                RedisCacheManager.class,
                cacheManager
        );

        assertTrue(
                AopUtils.isAopProxy(exerciseDefinitionCacheService)
        );

        ExerciseDefinition exerciseDefinition =
                createExerciseDefinition();

        when(
                exerciseDefinitionRepository.findAllAccessible(
                        username,
                        ExerciseType.SYSTEM
                )
        ).thenReturn(
                List.of(exerciseDefinition)
        );



        var firstResult =
                exerciseDefinitionCacheService.findAll(username);


        var secondResult =
                exerciseDefinitionCacheService.findAll(username);


        assertEquals(1, firstResult.size());

        assertEquals(
                "Bench Press",
                firstResult.get(0).exerciseName()
        );

        assertEquals(
                firstResult,
                secondResult
        );


        verify(
                exerciseDefinitionRepository,
                times(1)
        ).findAllAccessible(
                username,
                ExerciseType.SYSTEM
        );


        Cache cache =
                cacheManager.getCache("exerciseDefinitions");

        assertNotNull(cache);

        assertNotNull(
                cache.get(username)
        );
    }


    @Test
    void evictList_ShouldRemoveCachedValue() {

        String username = "cosmin";

        ExerciseDefinition exerciseDefinition =
                createExerciseDefinition();

        when(exerciseDefinitionRepository.findAllAccessible(
                username,
                ExerciseType.SYSTEM
        )).thenReturn(List.of(exerciseDefinition));


        // 1. Populăm cache-ul
        exerciseDefinitionCacheService.findAll(username);

        Cache cache =
                cacheManager.getCache("exerciseDefinitions");

        assertNotNull(cache);
        assertNotNull(cache.get(username));


        // 2. Curățăm istoricul mock-ului
        reset(exerciseDefinitionRepository);


        when(exerciseDefinitionRepository.findAllAccessible(
                username,
                ExerciseType.SYSTEM
        )).thenReturn(List.of(exerciseDefinition));


        // 3. Evict
        exerciseDefinitionCacheService.evictList(username);


        // Cheia trebuie să fi dispărut
        assertNull(cache.get(username));


        // 4. Acum trebuie să apeleze din nou repository-ul
        exerciseDefinitionCacheService.findAll(username);


        verify(
                exerciseDefinitionRepository,
                times(1)
        ).findAllAccessible(
                username,
                ExerciseType.SYSTEM
        );
    }


    private ExerciseDefinition createExerciseDefinition() {

        ExerciseDefinition exerciseDefinition =
                new ExerciseDefinition();

        exerciseDefinition.setId(1L);
        exerciseDefinition.setName("Bench Press");
        exerciseDefinition.setNormalizedName("bench press");
        exerciseDefinition.setMuscleGroup(MuscleGroup.CHEST);
        exerciseDefinition.setExerciseType(ExerciseType.SYSTEM);
        exerciseDefinition.setArchived(false);

        return exerciseDefinition;
    }
}