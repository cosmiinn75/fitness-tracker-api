    package com.cosmin.fitness_tracker_api.ServiceTest;


    import com.cosmin.fitness_tracker_api.DTO.PagedResponse;
    import com.cosmin.fitness_tracker_api.DTO.TrainingGoalRequest;
    import com.cosmin.fitness_tracker_api.DTO.TrainingGoalResponse;
    import com.cosmin.fitness_tracker_api.Enum.ExerciseType;
    import com.cosmin.fitness_tracker_api.Enum.MuscleGroup;
    import com.cosmin.fitness_tracker_api.Enum.Status;
    import com.cosmin.fitness_tracker_api.exception.ActiveTrainingGoalAlreadyExistsException;
    import com.cosmin.fitness_tracker_api.exception.ExerciseDefinitionNotFoundException;
    import com.cosmin.fitness_tracker_api.exception.TrainingGoalNotFoundException;
    import com.cosmin.fitness_tracker_api.mapper.TrainingGoalMapper;
    import com.cosmin.fitness_tracker_api.model.*;
    import com.cosmin.fitness_tracker_api.repository.ExerciseDefinitionRepository;
    import com.cosmin.fitness_tracker_api.repository.TrainingGoalRepository;
    import com.cosmin.fitness_tracker_api.repository.UserRepository;
    import com.cosmin.fitness_tracker_api.security.CurrentUserProvider;
    import com.cosmin.fitness_tracker_api.service.TrainingGoalService;
    import org.junit.jupiter.api.BeforeEach;
    import org.junit.jupiter.api.Test;
    import org.junit.jupiter.api.extension.ExtendWith;
    import org.mockito.InjectMocks;
    import org.mockito.Mock;
    import org.mockito.Spy;
    import org.mockito.junit.jupiter.MockitoExtension;
    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.PageImpl;
    import org.springframework.data.domain.PageRequest;
    import org.springframework.data.domain.Pageable;

    import java.time.LocalDate;
    import java.util.List;
    import java.util.Optional;
    import java.util.Set;

    import static org.junit.jupiter.api.Assertions.assertEquals;
    import static org.junit.jupiter.api.Assertions.assertThrows;
    import static org.mockito.ArgumentMatchers.any;
    import static org.mockito.Mockito.*;


    @ExtendWith(MockitoExtension.class)
    public class TrainingGoalServiceTest {

        @Mock
        TrainingGoalRepository trainingGoalRepository;

        @Mock
        ExerciseDefinitionRepository exerciseDefinitionRepository;

        @Mock
        UserRepository userRepository;

        @Mock
        CurrentUserProvider currentUserProvider;

        @Spy
        private TrainingGoalMapper trainingGoalMapper =
                new TrainingGoalMapper();

        @InjectMocks
        TrainingGoalService trainingGoalService;

        @Test
        void createTrainingGoal_WithValidRequest_Should_Create_TrainingGoal() {



            User user = new User();
            user.setId(1L);
            user.setUsername("cosmin");

            ExerciseDefinition exerciseDefinition = new ExerciseDefinition();
            exerciseDefinition.setName("Bench Press");
            exerciseDefinition.setMuscleGroup(MuscleGroup.CHEST);

            TrainingGoalRequest trainingGoalRequest = new TrainingGoalRequest(
                    1L,
                    100.00,
                    5,
                    LocalDate.of(2027,5,6)
            );

        when(userRepository.findByUsername("cosmin")).thenReturn(Optional.of(user));


        when(exerciseDefinitionRepository.findByIdAccessible(1L,"cosmin", ExerciseType.SYSTEM)).thenReturn(Optional.of(exerciseDefinition));
        when(trainingGoalRepository.existsByUserUsernameAndExerciseDefinitionIdAndStatus("cosmin",1L, Status.ACTIVE))
                .thenReturn(false);

        TrainingGoal trainingGoal = new  TrainingGoal();
        trainingGoal.setExerciseDefinition(exerciseDefinition);
        trainingGoal.setUser(user);
        trainingGoal.setCreatedAt(LocalDate.now());
        trainingGoal.setTargetWeight(100.00);
        trainingGoal.setTargetReps(5);
        trainingGoal.setStatus(Status.ACTIVE);
        trainingGoal.setId(1L);

            when(trainingGoalRepository.save(any(TrainingGoal.class)))
                    .thenAnswer(invocation -> {
                        TrainingGoal savedGoal = invocation.getArgument(0);
                        savedGoal.setId(1L);
                        return savedGoal;
                    });


        TrainingGoalResponse response = trainingGoalService.createTrainingGoal(trainingGoalRequest);

            assertEquals(1L,response.id());
            assertEquals(100.00,response.targetWeight());
            assertEquals(5,response.targetReps());
            assertEquals(Status.ACTIVE,response.status());
            assertEquals("Bench Press",response.exerciseName());


            verify(userRepository).findByUsername("cosmin");
            verify(exerciseDefinitionRepository).findByIdAccessible(1L,"cosmin", ExerciseType.SYSTEM);
        }


        @Test
        void createTrainingGoal_WhenExerciseDefinitionDoesNotExist_ShouldThrowExerciseDefinitionNotFoundException() {


            User user = new User();
            user.setId(1L);
            user.setUsername("cosmin");

            TrainingGoalRequest request = new TrainingGoalRequest(
                    99L,
                    100.0,
                    5,
                    LocalDate.now().plusMonths(1)
            );

            when(userRepository.findByUsername("cosmin"))
                    .thenReturn(Optional.of(user));

            when(exerciseDefinitionRepository.findByIdAccessible(99L,"cosmin", ExerciseType.SYSTEM))
                    .thenReturn(Optional.empty());

            assertThrows(
                    ExerciseDefinitionNotFoundException.class,
                    () -> trainingGoalService.createTrainingGoal(request)
            );

            verify(exerciseDefinitionRepository).findByIdAccessible(99L,"cosmin", ExerciseType.SYSTEM);
            verify(trainingGoalRepository, never())
                    .save(any(TrainingGoal.class));
        }

        @Test
        void createTrainingGoal_WhenActiveGoalAlreadyExists_ShouldThrowActiveTrainingGoalAlreadyExistsException() {


            User user = new User();
            user.setId(1L);
            user.setUsername("cosmin");

            ExerciseDefinition exerciseDefinition = new ExerciseDefinition();
            exerciseDefinition.setId(1L);
            exerciseDefinition.setName("Bench Press");
            exerciseDefinition.setMuscleGroup(MuscleGroup.CHEST);

            TrainingGoalRequest request = new TrainingGoalRequest(
                    1L,
                    100.0,
                    5,
                    LocalDate.now().plusMonths(1)
            );

            when(userRepository.findByUsername("cosmin"))
                    .thenReturn(Optional.of(user));

            when(exerciseDefinitionRepository.findByIdAccessible(1L,"cosmin", ExerciseType.SYSTEM))
                    .thenReturn(Optional.of(exerciseDefinition));

            when(trainingGoalRepository
                    .existsByUserUsernameAndExerciseDefinitionIdAndStatus(
                            "cosmin",
                            1L,
                            Status.ACTIVE
                    ))
                    .thenReturn(true);

            assertThrows(
                    ActiveTrainingGoalAlreadyExistsException.class,
                    () -> trainingGoalService.createTrainingGoal(request)
            );

            verify(trainingGoalRepository, never())
                    .save(any(TrainingGoal.class));
        }



        @Test
        void getTrainingGoals_WithValidPageSize_Should_Return_TrainingGoals() {


            User user = new User();
            user.setId(1L);
            user.setUsername("cosmin");

            int page =0;
            int size = 2;

            ExerciseDefinition exerciseDefinition = new ExerciseDefinition();
            exerciseDefinition.setName("Bench Press");
            exerciseDefinition.setMuscleGroup(MuscleGroup.CHEST);

            ExerciseDefinition exerciseDefinition2 = new ExerciseDefinition();
            exerciseDefinition2.setName("Lat Pulldown");
            exerciseDefinition2.setMuscleGroup(MuscleGroup.BACK);

            TrainingGoal trainingGoal = new TrainingGoal();
            trainingGoal.setExerciseDefinition(exerciseDefinition);
            trainingGoal.setUser(user);
            trainingGoal.setCreatedAt(LocalDate.now());
            trainingGoal.setTargetWeight(100.00);
            trainingGoal.setTargetReps(5);
            trainingGoal.setStatus(Status.ACTIVE);
            trainingGoal.setId(1L);

            TrainingGoal trainingGoal2 = new TrainingGoal();
            trainingGoal2.setExerciseDefinition(exerciseDefinition2);
            trainingGoal2.setUser(user);
            trainingGoal2.setCreatedAt(LocalDate.now());
            trainingGoal2.setTargetWeight(100.00);
            trainingGoal2.setTargetReps(10);
            trainingGoal2.setStatus(Status.ACTIVE);

            Pageable pageable = PageRequest.of(page, size);
            Page<TrainingGoal> trainingGoalPage = new PageImpl<>(
                    List.of(trainingGoal,trainingGoal2),
                    pageable,
                    2
            );

            when(trainingGoalRepository.findPageWithExerciseDefinitionByUserUsername("cosmin",pageable)).thenReturn(trainingGoalPage);

            PagedResponse<TrainingGoalResponse> response = trainingGoalService.getTrainingGoals(page,size);

            TrainingGoalResponse response1 = response.content().getFirst();
            TrainingGoalResponse response2 = response.content().getLast();

            assertEquals(2,response.totalElements());

            assertEquals("Bench Press",response1.exerciseName());
            assertEquals(Status.ACTIVE,response1.status());
            assertEquals(5,response1.targetReps());
            assertEquals(100.00,response1.targetWeight());

            assertEquals("Lat Pulldown",response2.exerciseName());
            assertEquals(Status.ACTIVE,response2.status());
            assertEquals(10,response2.targetReps());
            assertEquals(100.00,response2.targetWeight());


            verify(trainingGoalRepository).findPageWithExerciseDefinitionByUserUsername("cosmin",pageable);
        }

        @Test
        void cancelTrainingGoal_WithValidIndex_Should_Cancel_TrainingGoal() {

            User user = new User();
            user.setId(1L);
            user.setUsername("cosmin");

            Long trainingGoalId = 1L;

            ExerciseDefinition exerciseDefinition = new ExerciseDefinition();
            exerciseDefinition.setName("Bench Press");
            exerciseDefinition.setMuscleGroup(MuscleGroup.BACK);

            TrainingGoal trainingGoal = new TrainingGoal();
            trainingGoal.setExerciseDefinition(exerciseDefinition);
            trainingGoal.setUser(user);
            trainingGoal.setCreatedAt(LocalDate.now());
            trainingGoal.setTargetWeight(100.00);
            trainingGoal.setTargetReps(5);
            trainingGoal.setStatus(Status.ACTIVE);
            trainingGoal.setTargetDate(LocalDate.now().plusDays(1));
            trainingGoal.setId(1L);

            when(trainingGoalRepository.findByUserUsernameAndId("cosmin",trainingGoalId)).thenReturn(Optional.of(trainingGoal));



            TrainingGoalResponse response = trainingGoalService.cancelTrainingGoal(trainingGoalId);

            assertEquals(Status.CANCELLED,response.status());
            assertEquals(5,response.targetReps());
            assertEquals(100.00,response.targetWeight());

            verify(trainingGoalRepository).findByUserUsernameAndId("cosmin",trainingGoalId);
        }

        @Test
        void cancelTrainingGoal_WhenGoalDoesNotExist_ShouldThrowTrainingGoalNotFoundException() {


            when(trainingGoalRepository.findByUserUsernameAndId("cosmin", 99L))
                    .thenReturn(Optional.empty());

            assertThrows(
                    TrainingGoalNotFoundException.class,
                    () -> trainingGoalService.cancelTrainingGoal(99L)
            );

            verify(trainingGoalRepository)
                    .findByUserUsernameAndId("cosmin", 99L);
        }

        @Test
        void completeGoalsFromWorkout_WhenOneSetReachesBothTargets_ShouldCompleteGoal() {
            ExerciseDefinition exerciseDefinition = exerciseDefinition(1L);
            TrainingGoal trainingGoal = activeGoal(exerciseDefinition);

            Workout workout = workout(
                    LocalDate.of(2026, 7, 24),
                    exerciseDefinition,
                    exerciseSet(100.0, 5)
            );

            when(trainingGoalRepository
                    .findByUserUsernameAndExerciseDefinitionIdInAndStatus(
                            "cosmin",
                            Set.of(1L),
                            Status.ACTIVE
                    ))
                    .thenReturn(List.of(trainingGoal));

            int goalsCompleted =
                    trainingGoalService.completeGoalsFromWorkout(workout);

            assertEquals(1, goalsCompleted);
            assertEquals(Status.COMPLETED, trainingGoal.getStatus());

            verify(trainingGoalRepository)
                    .findByUserUsernameAndExerciseDefinitionIdInAndStatus(
                            "cosmin",
                            Set.of(1L),
                            Status.ACTIVE
                    );

            verify(trainingGoalRepository, never())
                    .save(any(TrainingGoal.class));
        }

        @Test
        void completeGoalsFromWorkout_WhenTargetsAreReachedByDifferentSets_ShouldNotCompleteGoal() {
            ExerciseDefinition exerciseDefinition = exerciseDefinition(1L);
            TrainingGoal trainingGoal = activeGoal(exerciseDefinition);

            Workout workout = workout(
                    LocalDate.of(2026, 7, 24),
                    exerciseDefinition,
                    exerciseSet(105.0, 4),
                    exerciseSet(95.0, 6)
            );

            when(trainingGoalRepository
                    .findByUserUsernameAndExerciseDefinitionIdInAndStatus(
                            "cosmin",
                            Set.of(1L),
                            Status.ACTIVE
                    ))
                    .thenReturn(List.of(trainingGoal));

            int goalsCompleted =
                    trainingGoalService.completeGoalsFromWorkout(workout);

            assertEquals(0, goalsCompleted);
            assertEquals(Status.ACTIVE, trainingGoal.getStatus());

            verify(trainingGoalRepository)
                    .findByUserUsernameAndExerciseDefinitionIdInAndStatus(
                            "cosmin",
                            Set.of(1L),
                            Status.ACTIVE
                    );

            verify(trainingGoalRepository, never())
                    .save(any(TrainingGoal.class));
        }

        @Test
        void completeGoalsFromWorkout_WhenWorkoutPredatesGoal_ShouldNotCompleteGoal() {
            ExerciseDefinition exerciseDefinition = exerciseDefinition(1L);
            TrainingGoal trainingGoal = activeGoal(exerciseDefinition);
            trainingGoal.setCreatedAt(LocalDate.of(2026, 7, 25));

            Workout workout = workout(
                    LocalDate.of(2026, 7, 24),
                    exerciseDefinition,
                    exerciseSet(110.0, 8)
            );

            when(trainingGoalRepository
                    .findByUserUsernameAndExerciseDefinitionIdInAndStatus(
                            "cosmin",
                            Set.of(1L),
                            Status.ACTIVE
                    ))
                    .thenReturn(List.of(trainingGoal));

            int goalsCompleted =
                    trainingGoalService.completeGoalsFromWorkout(workout);

            assertEquals(0, goalsCompleted);
            assertEquals(Status.ACTIVE, trainingGoal.getStatus());

            verify(trainingGoalRepository)
                    .findByUserUsernameAndExerciseDefinitionIdInAndStatus(
                            "cosmin",
                            Set.of(1L),
                            Status.ACTIVE
                    );

            verify(trainingGoalRepository, never())
                    .save(any(TrainingGoal.class));
        }


        @BeforeEach
        void setup(){
            when(currentUserProvider.getCurrentUsername())
                    .thenReturn("cosmin");
        }

        private ExerciseDefinition exerciseDefinition(Long id) {
            ExerciseDefinition exerciseDefinition = new ExerciseDefinition();
            exerciseDefinition.setId(id);
            exerciseDefinition.setName("Bench Press");

            return exerciseDefinition;
        }

        private TrainingGoal activeGoal(
                ExerciseDefinition exerciseDefinition
        ) {
            TrainingGoal trainingGoal = new TrainingGoal();

            trainingGoal.setExerciseDefinition(exerciseDefinition);
            trainingGoal.setCreatedAt(LocalDate.of(2026, 7, 1));
            trainingGoal.setTargetDate(LocalDate.of(2026, 8, 1));
            trainingGoal.setTargetWeight(100.0);
            trainingGoal.setTargetReps(5);
            trainingGoal.setStatus(Status.ACTIVE);

            return trainingGoal;
        }

        private Workout workout(
                LocalDate date,
                ExerciseDefinition exerciseDefinition,
                ExerciseSet... exerciseSets
        ) {
            WorkoutExercise workoutExercise = new WorkoutExercise();

            workoutExercise.setExerciseDefinition(exerciseDefinition);
            workoutExercise.setExerciseSets(List.of(exerciseSets));

            Workout workout = new Workout();

            workout.setDate(date);
            workout.setWorkoutExercises(List.of(workoutExercise));

            return workout;
        }

        private ExerciseSet exerciseSet(double weight, int reps) {
            ExerciseSet exerciseSet = new ExerciseSet();

            exerciseSet.setWeight(weight);
            exerciseSet.setReps(reps);

            return exerciseSet;
        }
    }
