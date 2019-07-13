package org.clinicalontology.bpmplus.htn;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.hl7.fhir.dstu3.formats.IParser;
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

import ca.uhn.fhir.context.FhirContext;



class Tester {

	public void processFiles(File folder, IParser parser) {
		for (File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				// Do nothing
			} else {
				FileReader reader = new FileReader(fileEntry);
				BufferedReader bufferReader = new BufferedReader(reader);

				while ((line = bufferReader.readLine()) != null) {
					Resource resource = parser.parseResource(line);
					if (resource instanceof Observation) {
						// do xyz
					}
					line = bufferReader.readLine();
				}
			}
		}
	}

	public static void main(String[] args) {
		HtnDemo htnDemo = new HtnDemo();
		htnDemo.demo();

		File folder = new File("/path/to/test/folder");
		FhirContext context = FhirContext.forDstu3();
		IParser parser = context.newJsonParser();

	}

}