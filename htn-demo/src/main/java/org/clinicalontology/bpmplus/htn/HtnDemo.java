package org.clinicalontology.bpmplus.htn;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Observation.ObservationComponentComponent;
import org.hl7.fhir.dstu3.model.Patient;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import org.kie.api.runtime.manager.RuntimeEngine;

import org.kie.api.runtime.manager.RuntimeManager;

import org.kie.api.runtime.process.ProcessInstance;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNDecisionResult;
import org.kie.dmn.api.core.DMNModel;
import org.kie.dmn.api.core.DMNResult;
import org.kie.dmn.api.core.DMNRuntime;
import org.kie.dmn.core.api.DMNFactory;
import org.kie.dmn.core.compiler.RuntimeTypeCheckOption;
import org.kie.dmn.core.impl.DMNRuntimeImpl;

import org.kie.dmn.core.util.DMNRuntimeUtil;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class HtnDemo {
  
	protected  EvaluationResult runLogic(Patient patient, List<Condition> problemList, Observation observation, List<MedicationRequest> activePrescriptions)
	{
		
		final Map<String, Object> patient_record = new HashMap<String, Object>(  );
    	List<String> medicationList = new ArrayList<>(  );
    	List<String> conditions = new ArrayList<>(  );
    	
		for (MedicationRequest medication : activePrescriptions) {
			medicationList.add(medication.getMedicationCodeableConcept().getCoding().get(0).getCode());
		}
			
		
		for(Condition condition : problemList) {
			conditions.add(condition.getCode().getCoding().get(0).getCode());
		}
		
		patient_record.put("DOB",patient.getBirthDate());

        for (ObservationComponentComponent component : observation.getComponent()) {
            if (component.getCode().getCoding().get(0).getCode().equals("8462-4")) {
            	patient_record.put("Diastolic", component.getValueQuantity().getValue());
            } else if (component.getCode().getCoding().get(0).getCode().equals("8480-6")) {
            	patient_record.put("Systolic",component.getValueQuantity().getValue());
            }
        }
        
        final DMNRuntime runtime = DMNModelLoader.createRuntime("BP Recommendations.dmn", this.getClass() );
        final DMNModel dmnModel = runtime.getModel("http://www.trisotech.com/definitions/_fcefcca4-3897-4be0-8c8e-4d81bba9cee5", "BP Recommendations" );
        assertThat( dmnModel, notNullValue() );
        assertThat( DMNModelLoader.formatMessages( dmnModel.getMessages() ), dmnModel.hasErrors(), is(false) ); // need proper type support to enable this

        final DMNContext context = DMNFactory.newContext();

        context.set("Patient Record", LoadTest1());
        final DMNResult dmnResult = runtime.evaluateAll(dmnModel, context );

        final DMNContext result = dmnResult.getContext();
        
        Object ob = result.get( "BP Recommendations" );
        System.out.println( result );
        
        HashMap<String, Object> recommendation = (HashMap<String, Object>) result.get( "BP Recommendations" );
        BigDecimal SystolicTarget = (BigDecimal) recommendation.get("Systolic");
        Boolean hasHighBp = (Boolean) recommendation.get("hasHighBp");
        List<String> medications = (List<String>) recommendation.get("Medications");
        
		
		EvaluationResult evaluationResult = new EvaluationResult();
		evaluationResult.setHighBp(hasHighBp);
		evaluationResult.setLowBpGoal(SystolicTarget == new BigDecimal(140));
		evaluationResult.setBpMedications(medications);	
		
		return evaluationResult;
	}

    
    public void demo() {
        final DMNRuntime runtime = DMNRuntimeUtil.createRuntime("BP Recommendations.dmn", this.getClass() );
        final DMNModel dmnModel = runtime.getModel("http://www.trisotech.com/definitions/_fcefcca4-3897-4be0-8c8e-4d81bba9cee5", "BP Recommendations" );
        assertThat( dmnModel, notNullValue() );
        assertThat( DMNRuntimeUtil.formatMessages( dmnModel.getMessages() ), dmnModel.hasErrors(), is(false) ); // need proper type support to enable this

        final DMNContext context = DMNFactory.newContext();

        context.set("Patient Record", LoadTest4());

        final DMNResult dmnResult = runtime.evaluateAll(dmnModel, context );

        final DMNContext result = dmnResult.getContext();
        System.out.println( result );
        Object ob = result.get( "BP Recommendations" );
       
        
        HashMap<String, Object> recommendation = (HashMap<String, Object>) result.get( "BP Recommendations" );
        BigDecimal SystolicTarget = (BigDecimal) recommendation.get("Systolic");
        Boolean hasHighBp = (Boolean) recommendation.get("hasHighBp");
        List<String> medications = (List<String>) recommendation.get("Medications");
        
    }
    

    private Map LoadTest2()  // target Systolic is 150, is in Meds , has Hypertension
    {
    
    	final Map<String, Object> patient_record = new HashMap<String, Object>(  );
    	List<String> medicationList = new ArrayList<>(  );
    	List<String> conditions = new ArrayList<>(  );
    	
    	medicationList.add("1000001");  //Amlodipine 5 MG / Hydrochlorothiazide 25 MG / Olmesartan medoxomil 40 MG Oral Table
    	medicationList.add("100000122"); // junk
    	medicationList.add("1011753"); //aliskiren 150 MG / Hydrochlorothiazide 25 MG Oral Tablet"
    	
    	
    	//conditions.add("109171000119104"); //Retinal edema co-occurrent and due to type 1 diabetes mellitus (disorder)"
    	//conditions.add("585.5"); //Chronic kidney disease, Stage V"
    	conditions.add("585.4"); //Junk
    	
    	patient_record.put("Medication Codes", medicationList);
    	patient_record.put("Active Conditions", conditions);
    	patient_record.put("Systolic", number(151));
    	patient_record.put("Diastolic", number(80));
    	patient_record.put("DOB", LocalDate.parse("1959-01-01"));
    	
    	return patient_record;
    }
    private Map LoadTest4()  // target Systolic is 150, has NO Meds , has Hypertension
    {
    
    	final Map<String, Object> patient_record = new HashMap<String, Object>(  );
    	List<String> medicationList = new ArrayList<>(  );
    	List<String> conditions = new ArrayList<>(  );
    	
    	medicationList.add("10000011232");  //Amlodipine 5 MG / Hydrochlorothiazide 25 MG / Olmesartan medoxomil 40 MG Oral Table
    	medicationList.add("100000122"); // junk
    	medicationList.add("101175312321"); //aliskiren 150 MG / Hydrochlorothiazide 25 MG Oral Tablet"
    	
    	
    	//conditions.add("109171000119104"); //Retinal edema co-occurrent and due to type 1 diabetes mellitus (disorder)"
    	//conditions.add("585.5"); //Chronic kidney disease, Stage V"
    	conditions.add("585.4"); //Junk
    	
    	patient_record.put("Medication Codes", medicationList);
    	patient_record.put("Active Conditions", conditions);
    	patient_record.put("Systolic", number(151));
    	patient_record.put("Diastolic", number(80));
    	patient_record.put("DOB", LocalDate.parse("1959-01-01"));
    	
    	return patient_record;
    }
    private Map LoadTest1()
    {
    
    	final Map<String, Object> patient_record = new HashMap<String, Object>(  );
    	List<String> medicationList = new ArrayList<>(  );
    	List<String> conditions = new ArrayList<>(  );
    	
    	medicationList.add("1000001");  //Amlodipine 5 MG / Hydrochlorothiazide 25 MG / Olmesartan medoxomil 40 MG Oral Table
    	medicationList.add("1000001111"); // junk
    	medicationList.add("1011753"); //aliskiren 150 MG / Hydrochlorothiazide 25 MG Oral Tablet"
    	
    	
    	conditions.add("109171000119104"); //Retinal edema co-occurrent and due to type 1 diabetes mellitus (disorder)"
    	conditions.add("585.5"); //Chronic kidney disease, Stage V"
    	conditions.add("585.4"); //Junk
    	
    	patient_record.put("Medication Codes", medicationList);
    	patient_record.put("Active Conditions", conditions);
    	patient_record.put("Systolic", 120);
    	patient_record.put("Diastolic", 80);
    	patient_record.put("DOB", LocalDate.parse("1959-01-01"));
    	
    	return patient_record;
    }
    
    private Map LoadTest3() // No Hypertension, over 60, on meds
    {
    
    	final Map<String, Object> patient_record = new HashMap<String, Object>(  );
    	List<String> medicationList = new ArrayList<>(  );
    	List<String> conditions = new ArrayList<>(  );
    	
    	medicationList.add("1000001");  //Amlodipine 5 MG / Hydrochlorothiazide 25 MG / Olmesartan medoxomil 40 MG Oral Table
    	medicationList.add("madeup"); // junk
    	medicationList.add("1011753"); //aliskiren 150 MG / Hydrochlorothiazide 25 MG Oral Tablet"
    	
    	
    	//conditions.add("109171000119104"); //Retinal edema co-occurrent and due to type 1 diabetes mellitus (disorder)"
    	//conditions.add("585.5"); //Chronic kidney disease, Stage V"
    	conditions.add("585.42"); //Junk
    	
    	patient_record.put("Medication Codes", medicationList);
    	patient_record.put("Active Conditions", conditions);
    	patient_record.put("Systolic", 120);
    	patient_record.put("Diastolic", 80);
    	patient_record.put("DOB", LocalDate.parse("1959-01-01"));
    	
    	return patient_record;
    }

    private LocalDateTime date(final String date ) {
        return LocalDateTime.parse( date );
    }

    private BigDecimal number(final Number n ) {
        return BigDecimal.valueOf( n.longValue() );
    }
    
    
    public static void main(String [] args) {
    	HtnDemo htnDemo = new HtnDemo() ;
    	htnDemo.demo(); 	
    }

}
