import csv
import random
import os
from datetime import datetime, timedelta

# MODIFY THESE SIMULATION PARAMETERS
# --------------------------------
FILE_NAME = "workout.csv"
NUM_USERS = 50
DAYS_PER_USER = 100
COHORT_PROFILE = {
    "age": 23,
    "sex": "MALE", # "MALE" or "FEMALE"
    "height": 174, # in cm
    "weight": 77.7, # in kg
    "goal": "FAT_LOSS" # "FAT_LOSS" or "MUSCLE_GAIN"
}
# --------------------------------

# Constants for calorie calculations
KG_OF_FAT = 7700
CALORIES_PER_GRAM_PROTEIN_OR_FAT = 4
CALORIES_PER_GRAM_FAT = 9
TEF = 1.1  # Thermic effect of food multiplier

# Rough estimate of net calories burned per minute in each cardio zone for different weights (kg)
# Key:value pairs are weight_kg:(zone1, zone2, zone3, zone4, zone5)
CARDIO_ZONE_CALORIES_PER_MIN_FROM_WEIGHT = {
    54: (1.9, 3.8, 6.2, 8.6, 11.4),
    68: (2.4, 4.8, 7.7, 10.7, 14.3),
    82: (2.9, 5.7, 9.3, 12.9, 17.2),
    95: (3.3, 6.7, 10.8, 15, 20),
    109: (3.8, 7.6, 12.4, 17.2, 22.9)
}

# Rough estimate of net calories burned per step for different weights (kg)
# Key:value pairs are weight_kg:calories_per_step
CALORIES_PER_STEP_FROM_WEIGHT = {
    54: 0.023,
    68: 0.029,
    82: 0.034,
    95: 0.04,
    109: 0.046
}

workout_types = [
    "Push",
    "Pull",
    "Legs",
    "Upper",
    "Rest"
]


def calories_per_step(weight):
    """
    Linear interpolation functions to estimate calories burned per step and per minute of 
    cardio based on weights that fall between the sample weights.
    """
    points = sorted(CALORIES_PER_STEP_FROM_WEIGHT.items())

    # If the weight is outside the range of the sample weights, return the closest value.
    if weight <= points[0][0]:
        return points[0][1]
    if weight >= points[-1][0]:
        return points[-1][1]

    for i in range(len(points) - 1):
        w1, c1 = points[i]
        w2, c2 = points[i + 1]
        if w1 <= weight <= w2:
            fraction = (weight - w1) / (w2 - w1)
            return c1 + fraction * (c2 - c1)


def cardio_calories_per_min(weight, zone):
    """
    Linear interpolation function to estimate calories burned per minute of cardio based 
    on weights that fall between the sample weights.
    """
    if zone < 1 or zone > 5:
        raise ValueError("Cardio zone must be between 1 and 5.")

    points = sorted(CARDIO_ZONE_CALORIES_PER_MIN_FROM_WEIGHT.items())

    # If the weight is outside the range of the sample weights, return the closest value.
    if weight <= points[0][0]:
        return points[0][1][zone - 1]
    if weight >= points[-1][0]:
        return points[-1][1][zone - 1]

    for i in range(len(points) - 1):
        w1, vals1 = points[i]
        w2, vals2 = points[i + 1]
        if w1 <= weight <= w2:
            fraction = (weight - w1) / (w2 - w1)
            c1 = vals1[zone - 1]
            c2 = vals2[zone - 1]
            return c1 + fraction * (c2 - c1)

def calculate_bmr(age, sex, weight, height):
    """
    Mifflin-St Jeor equation for calculating Basal Metabolic Rate (BMR)
    """
    if sex == "MALE":
        return 10 * weight + 6.25 * height - 5 * age + 5
    else:
        return 10 * weight + 6.25 * height - 5 * age - 161


def generate_phase_schedule(total_days, goal):
    """
    Build a list of calorie offsets (surplus/maintenance/deficit) per day that
    covers the whole simulation. This biases the weight to move in the right direction, 
    but with some day-to-day noise, which simulates real-world variability.

    The stated goal biases which phase shows up more often/longer, reflecting the fact that people trying 
    to gain muscle will spend more time in a surplus, and those trying to lose fat will spend more time 
    in a deficit. The actual calorie offsets are randomly sampled from a range, showing that even within a phase, 
    there is variability in how much surplus or deficit is applied.
    """
    if goal == "MUSCLE_GAIN":
        phase_weights = {"surplus": 0.5, "maintenance": 0.35, "deficit": 0.15}
    else:  # FAT_LOSS
        phase_weights = {"deficit": 0.5, "maintenance": 0.35, "surplus": 0.15}

    phase_types = list(phase_weights.keys())
    weights = list(phase_weights.values())

    per_day_offset = []
    days_used = 0

    while days_used < total_days:
        phase_type = random.choices(phase_types, weights=weights, k=1)[0]
        length = min(random.randint(30, 90), total_days - days_used)

        if phase_type == "surplus":
            offset = random.uniform(150, 400)
        elif phase_type == "deficit":
            offset = random.uniform(-600, -200)
        else:
            offset = random.uniform(-100, 100)

        per_day_offset.extend([offset] * length)
        days_used += length

    return per_day_offset


def generate_daily_record(user_id, profile, day, initial_weight, calorie_offset):
    """
    Generate a daily record for a user based on their profile and simulation parameters.
    """
    date = datetime(2026, 7, 1) + timedelta(days=day)

    bmr = calculate_bmr(
        profile["age"],
        profile["sex"],
        initial_weight,
        profile["height"]
    )

    cardio_min = random.randint(0, 60)
    cardio_zone = random.randint(1, 5)
    steps = random.randint(3000, 18000)

    neat = calories_per_step(initial_weight) * steps
    eat = cardio_calories_per_min(initial_weight, cardio_zone) * cardio_min


    maintenance_calories = (bmr + neat + eat) * TEF

    # Day-to-day noise (appetite, food logging error, water/glycogen, etc.)
    daily_noise = random.gauss(0, 150)

    # This is the number of calories the user ends up actually consuming on this day
    target_calories = maintenance_calories + calorie_offset + daily_noise
    # Minimum of bmr * 0.85 to avoid absurdly low calorie intakes that would be unrealistic for a human being
    target_calories = max(target_calories, bmr * 0.85)

    # Derive macros FROM the target
    protein_g = int(random.uniform(1.6, 2.2) * initial_weight)
    fat_g = int((target_calories * random.uniform(0.20, 0.30)) / CALORIES_PER_GRAM_FAT)
    remaining_calories = (
        target_calories
        - protein_g * CALORIES_PER_GRAM_PROTEIN_OR_FAT
        - fat_g * CALORIES_PER_GRAM_FAT
    )
    carbs_g = max(0, int(remaining_calories / CALORIES_PER_GRAM_PROTEIN_OR_FAT))

    calories = int(
        protein_g * CALORIES_PER_GRAM_PROTEIN_OR_FAT
        + carbs_g * CALORIES_PER_GRAM_PROTEIN_OR_FAT
        + fat_g * CALORIES_PER_GRAM_FAT
    )

    calorie_balance = calories - maintenance_calories
    weight_change = calorie_balance / KG_OF_FAT
    final_weight = initial_weight + weight_change + random.uniform(-0.3, 0.3) # Random noise to account for water/glycogen fluctuations, etc.
    final_weight = max(final_weight, pow(profile["height"] / 100, 2) * 16)  # BMI floor of 16, which is considered the lower limit of healthy weight

    workout = random.choice(workout_types)

    if workout == "Rest":
        total_sets = 0
        rir = None
    else:
        total_sets = random.randint(6, 20)
        rir = random.randint(0, 5)

    sleep = round(random.uniform(5.5, 9), 1)

    return ([
        user_id,
        date.strftime("%Y-%m-%d"),
        round(final_weight, 1),
        int(calories),
        protein_g,
        carbs_g,
        fat_g,
        cardio_min,
        cardio_zone,
        steps,
        workout,
        rir,
        total_sets,
        sleep
    ], final_weight)


def run():
    with open(os.path.join("../data/input", FILE_NAME), "w", newline="") as f:
        writer = csv.writer(f)

        writer.writerow([
            "user_id",
            "date",
            "weight_kg",
            "calories",
            "protein_g",
            "carbs_g",
            "fats_g",
            "cardio_min",
            "cardio_zone",
            "steps",
            "workout_type",
            "workout_rir",
            "total_sets",
            "sleep_h"
        ])

        # Generate a single user profile representing a cohort of users with similar characteristics. 
        profile = COHORT_PROFILE.copy()

        for i in range(NUM_USERS):
            user_id = f"user_{i:06d}"

            curr_weight = profile["weight"]

            calorie_offsets = generate_phase_schedule(DAYS_PER_USER, profile["goal"])

            for day in range(DAYS_PER_USER):
                daily_record, curr_weight = generate_daily_record(
                    user_id,
                    profile,
                    day,
                    curr_weight,
                    calorie_offsets[day]
                )
                writer.writerow(daily_record)

    print(
        f"Successfully generated {DAYS_PER_USER} days of data for {NUM_USERS} users "
        f"(height={profile['height'] / 100:.2f}m, "
        f"weight={profile['weight']:.1f}kg, "
        f"age={profile['age']}, sex={profile['sex']}, "
        f"goal={profile['goal']})."
        )


if __name__ == "__main__":
    run()