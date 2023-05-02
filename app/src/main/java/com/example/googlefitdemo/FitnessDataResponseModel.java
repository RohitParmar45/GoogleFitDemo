package com.example.googlefitdemo;

public class FitnessDataResponseModel {
    public  float steps ;
    public  String stepsForUi ;

    public String getStepsForUi() {
        return stepsForUi;
    }

    public void setStepsForUi(String stepsForUi) {
        this.stepsForUi = stepsForUi;
    }

    public FitnessDataResponseModel() {
    }

    public float getSteps() {
        return steps;
    }

    public void setSteps(float steps) {
        this.steps = steps;
        this.stepsForUi = Float.toString(steps);
    }
}
