package com.cosmin.fitness_tracker_api.component;

import com.cosmin.fitness_tracker_api.Enum.Status;
import com.cosmin.fitness_tracker_api.repository.TrainingGoalRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;

@Component
public class ScheduleComponent {

    private final TrainingGoalRepository trainingGoalRepository;

    public ScheduleComponent(TrainingGoalRepository trainingGoalRepository) {
        this.trainingGoalRepository = trainingGoalRepository;
    }

    @Scheduled(cron = "0 0 0 * * *" , zone = "Europe/Bucharest")
    @Transactional
    public void updateGoals() {
        trainingGoalRepository.updateTrainingGoalStatus(LocalDate.now(ZoneId.of("Europe/Bucharest")), Status.EXPIRED, Status.ACTIVE);
    }

}
