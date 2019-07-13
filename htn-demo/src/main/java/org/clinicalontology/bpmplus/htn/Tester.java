package org.clinicalontology.bpmplus.htn;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.instance.model.api.IBaseResource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;



class Tester {

	public void processFiles(File folder, IParser parser) throws Exception {
		for (File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				// Do nothing
			} else {
				FileReader reader = new FileReader(fileEntry);
				BufferedReader bufferReader = new BufferedReader(reader);

				String line = null;
				while ((String line = bufferReader.readLine()) != null) {
					IBaseResource resource = parser.parseResource(line);
					if (resource instanceof Observation) {
						// do xyz
					}
					line = bufferReader.readLine();
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {
		HtnDemo htnDemo = new HtnDemo();
		htnDemo.demo();

		File folder = new File("/path/to/test/folder");
		FhirContext context = FhirContext.forDstu3();
		IParser parser = context.newJsonParser();

	}

}