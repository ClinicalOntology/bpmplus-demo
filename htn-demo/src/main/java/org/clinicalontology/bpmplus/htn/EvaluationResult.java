package org.clinicalontology.bpmplus.htn;

import org.hl7.fhir.dstu3.model.MedicationRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EvaluationResult {

    private Map<String,Boolean> predicateEvaluation = new HashMap<>();
    private List<String> bpMedications = new ArrayList<>();

    public Boolean hasHighBp() {
        return predicateEvaluation.get(Constants.PREDICATE_HAS_HIGH_BP);
    }

    public void setHighBp(Boolean hasHighBp) {
        predicateEvaluation.put(Constants.PREDICATE_HAS_HIGH_BP, hasHighBp);
    }

    public Boolean isOnBpMeds() {
        return predicateEvaluation.get(Constants.PREDICATE_IS_ON_BP_MED);
    }

    public void setOnBpMeds(Boolean isOnBpMeds) {
        predicateEvaluation.put(Constants.PREDICATE_IS_ON_BP_MED, isOnBpMeds);
    }

    public Boolean isLowBpGoal() {
        return predicateEvaluation.get(Constants.PREDICATE_IS_LOW_BP_GOAL);
    }

    public void setLowBpGoal(Boolean isLowBpGoal) {
        predicateEvaluation.put(Constants.PREDICATE_IS_LOW_BP_GOAL, isLowBpGoal);
    }

    public Map<String, Boolean> getPredicateEvaluation() {
        return predicateEvaluation;
    }

    public void setPredicateEvaluation(Map<String, Boolean> predicateEvaluation) {
        this.predicateEvaluation = predicateEvaluation;
    }

    public List<String> getBpMedications() {
        return bpMedications;
    }

    public void setBpMedications(List<String> bpMedications) {
        this.bpMedications = bpMedications;
    }
}
