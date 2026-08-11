INSERT INTO exercise_definitions
    (name, normalized_name, exercise_type, owner_id, archived, muscle_group)
SELECT
    exercises.name,
    exercises.normalized_name,
    'SYSTEM',
    NULL,
    FALSE,
    exercises.muscle_group
FROM (
    SELECT 'Barbell Bench Press' AS name, 'barbell bench press' AS normalized_name, 'CHEST' AS muscle_group
    UNION ALL SELECT 'Incline Barbell Bench Press', 'incline barbell bench press', 'CHEST'
    UNION ALL SELECT 'Dumbbell Bench Press', 'dumbbell bench press', 'CHEST'
    UNION ALL SELECT 'Incline Dumbbell Bench Press', 'incline dumbbell bench press', 'CHEST'
    UNION ALL SELECT 'Cable Chest Fly', 'cable chest fly', 'CHEST'
    UNION ALL SELECT 'Push-Up', 'push-up', 'CHEST'

    UNION ALL SELECT 'Pull-Up', 'pull-up', 'BACK'
    UNION ALL SELECT 'Lat Pulldown', 'lat pulldown', 'BACK'
    UNION ALL SELECT 'Barbell Row', 'barbell row', 'BACK'
    UNION ALL SELECT 'One-Arm Dumbbell Row', 'one-arm dumbbell row', 'BACK'
    UNION ALL SELECT 'Seated Cable Row', 'seated cable row', 'BACK'
    UNION ALL SELECT 'Deadlift', 'deadlift', 'BACK'

    UNION ALL SELECT 'Overhead Press', 'overhead press', 'SHOULDERS'
    UNION ALL SELECT 'Dumbbell Shoulder Press', 'dumbbell shoulder press', 'SHOULDERS'
    UNION ALL SELECT 'Lateral Raise', 'lateral raise', 'SHOULDERS'
    UNION ALL SELECT 'Rear Delt Fly', 'rear delt fly', 'SHOULDERS'
    UNION ALL SELECT 'Face Pull', 'face pull', 'SHOULDERS'

    UNION ALL SELECT 'Barbell Curl', 'barbell curl', 'ARMS'
    UNION ALL SELECT 'Dumbbell Curl', 'dumbbell curl', 'ARMS'
    UNION ALL SELECT 'Hammer Curl', 'hammer curl', 'ARMS'
    UNION ALL SELECT 'Preacher Curl', 'preacher curl', 'ARMS'
    UNION ALL SELECT 'Triceps Pushdown', 'triceps pushdown', 'ARMS'
    UNION ALL SELECT 'Skull Crusher', 'skull crusher', 'ARMS'
    UNION ALL SELECT 'Dips', 'dips', 'ARMS'

    UNION ALL SELECT 'Barbell Squat', 'barbell squat', 'LEGS'
    UNION ALL SELECT 'Leg Press', 'leg press', 'LEGS'
    UNION ALL SELECT 'Romanian Deadlift', 'romanian deadlift', 'LEGS'
    UNION ALL SELECT 'Leg Curl', 'leg curl', 'LEGS'
    UNION ALL SELECT 'Leg Extension', 'leg extension', 'LEGS'
    UNION ALL SELECT 'Bulgarian Split Squat', 'bulgarian split squat', 'LEGS'
    UNION ALL SELECT 'Standing Calf Raise', 'standing calf raise', 'LEGS'

    UNION ALL SELECT 'Plank', 'plank', 'CORE'
    UNION ALL SELECT 'Hanging Leg Raise', 'hanging leg raise', 'CORE'
    UNION ALL SELECT 'Cable Crunch', 'cable crunch', 'CORE'
    UNION ALL SELECT 'Ab Wheel Rollout', 'ab wheel rollout', 'CORE'
) AS exercises
WHERE NOT EXISTS (
    SELECT 1
    FROM exercise_definitions existing
    WHERE existing.normalized_name = exercises.normalized_name
);