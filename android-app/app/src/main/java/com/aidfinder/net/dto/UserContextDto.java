package com.aidfinder.net.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Optional, non-clinical profile details sent alongside a search so the AI
 * can judge physical fit (size, weight limits, grip size). Deliberately
 * excludes blood pressure and heart rate - those stay on the device.
 */
public class UserContextDto {
    public Integer age;

    @SerializedName("height_feet")
    public Integer heightFeet;

    @SerializedName("height_inches")
    public Integer heightInches;

    @SerializedName("weight_lbs")
    public Double weightLbs;

    @SerializedName("biological_sex")
    public String biologicalSex;

    @SerializedName("activity_level")
    public String activityLevel;

    public UserContextDto(Integer age, Integer heightFeet, Integer heightInches,
                           Double weightLbs, String biologicalSex, String activityLevel) {
        this.age = age;
        this.heightFeet = heightFeet;
        this.heightInches = heightInches;
        this.weightLbs = weightLbs;
        this.biologicalSex = biologicalSex;
        this.activityLevel = activityLevel;
    }

    /** True if every field is empty/zero - callers use this to send no context at all. */
    public boolean isBlank() {
        return (age == null || age == 0)
                && (heightFeet == null || heightFeet == 0)
                && (heightInches == null || heightInches == 0)
                && (weightLbs == null || weightLbs == 0)
                && (biologicalSex == null || biologicalSex.isEmpty())
                && (activityLevel == null || activityLevel.isEmpty());
    }
}
