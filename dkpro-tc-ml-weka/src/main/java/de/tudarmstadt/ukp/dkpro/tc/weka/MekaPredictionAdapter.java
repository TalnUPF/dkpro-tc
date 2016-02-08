/**
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package de.tudarmstadt.ukp.dkpro.tc.weka;

import java.util.Collection;

import de.tudarmstadt.ukp.dkpro.lab.reporting.ReportBase;
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.DimensionBundle;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.FoldDimensionBundle;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.TaskBase;
import de.tudarmstadt.ukp.dkpro.tc.core.io.DataWriter;
import de.tudarmstadt.ukp.dkpro.tc.core.ml.ModelSerialization_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.core.ml.TCMachineLearningAdapter;
import de.tudarmstadt.ukp.dkpro.tc.core.task.ModelSerializationTask;
import de.tudarmstadt.ukp.dkpro.tc.ml.report.BatchPredictionReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.WekaOutcomeIDReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.WekaExtractFeaturesAndPredictTask;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.serialization.LoadModelConnectorWeka;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.serialization.WekaModelSerializationDescription;
import de.tudarmstadt.ukp.dkpro.tc.weka.writer.MekaDataWriter;

public class MekaPredictionAdapter 
	implements TCMachineLearningAdapter
{

	public static TCMachineLearningAdapter getInstance() {
		return new MekaPredictionAdapter();
	}
	
	@Override
    public TaskBase getTestTask()
    {
        return new WekaExtractFeaturesAndPredictTask();
	}

	@Override
	public Class<? extends ReportBase> getClassificationReportClass() {
        return null;
	}

	@Override
	public Class<? extends ReportBase> getOutcomeIdReportClass() {
		return WekaOutcomeIDReport.class;
	}

	@Override
	public Class<? extends ReportBase> getBatchTrainTestReportClass() {
        return BatchPredictionReport.class;
	}

	@SuppressWarnings("unchecked")
    @Override
	public DimensionBundle<Collection<String>> getFoldDimensionBundle(
			String[] files, int folds) {
		return  new FoldDimensionBundle<String>("files", Dimension.create("", files), folds);
	}

	@Override
	public String getFrameworkFilename(AdapterNameEntries name) {

        switch (name) {
            case featureVectorsFile:  return "training-data.arff.gz";
            case predictionsFile      :  return "predictions.arff";
            case evaluationFile       :  return "evaluation.bin";
            case featureSelectionFile :  return "attributeEvaluationResults.txt";
        }
        
        return null;
	}
	
	@Override
	public Class<? extends DataWriter> getDataWriterClass() {
		return MekaDataWriter.class;
	}

	@Override
	public Class<? extends ModelSerialization_ImplBase> getLoadModelConnectorClass() {
		return LoadModelConnectorWeka.class;
	}
	
	@Override
	public Class<? extends ModelSerializationTask> getSaveModelTask() {
	    return WekaModelSerializationDescription.class;
	}
}
