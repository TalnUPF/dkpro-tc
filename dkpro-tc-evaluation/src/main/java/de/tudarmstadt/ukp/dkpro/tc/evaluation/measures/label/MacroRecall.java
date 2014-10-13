/*******************************************************************************
 * Copyright 2014
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universit�t Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.tc.evaluation.measures.label;

import de.tudarmstadt.ukp.dkpro.tc.evaluation.confusion.matrix.ContingencyTable;


/**
 * @author Andriy Nadolskyy
 * 
 */
public class MacroRecall
{

	public static Double calculate(ContingencyTable cTable, boolean softEvaluation){
		int numberOfMatrices = cTable.getSize();
		double summedRecall = 0.0;
		
		for (int i = 0; i < numberOfMatrices; i++){
			double tp = cTable.getTruePositives(i);
			double fn = cTable.getFalseNegatives(i);
			
			double denominator = tp + fn;
			if (denominator != 0.0) {
				double recall = (double) tp / denominator;
				summedRecall += recall;
			}
			else if (! softEvaluation) {
				return Double.NaN;
			}
		}
		
		return summedRecall / numberOfMatrices;	
	}	
}