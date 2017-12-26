/*******************************************************************************
 * Copyright 2017
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
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
package org.dkpro.tc.core.ml;

import java.util.Collection;

import org.dkpro.lab.reporting.ReportBase;
import org.dkpro.lab.task.impl.DimensionBundle;
import org.dkpro.lab.task.impl.TaskBase;
import org.dkpro.tc.core.io.DataWriter;
import org.dkpro.tc.core.task.ModelSerializationTask;

/**
 * Interface for machine learning frameworks in TC
 */
public interface TcShallowLearningAdapter
{

    public enum AdapterNameEntries
    {
        /**
         * Intended for storing feature vectors (both in training and testing phases)
         */
        featureVectorsFile,

        /**
         * Intended for actual predictions on test data, i.e. label sequence
         */
        predictionsFile,

        featureSelectionFile
    }

    /**
     * @return The task that reads the ML feature store format, trains the classifier and stores the
     *         test results.
     */
    public TaskBase getTestTask();

    /**
     * @return The data writer class that needs to be used with the corresponding ML framework
     */
    public Class<? extends DataWriter> getDataWriterClass();

    /**
     * @return The class of the load model connector to be used with this ML framework
     */
    public Class<? extends ModelSerialization_ImplBase> getLoadModelConnectorClass();

    /**
     * This report is always added to {@code testTask} reports by default in
     * ExperimentCrossValidation and ExperimentTrainTest.
     *
     * @return The report that collects the outcomeId to prediction values.
     */
    public Class<? extends ReportBase> getOutcomeIdReportClass();

    /**
     * This report is always added to {@code crossValidationTask} reports by default in
     * ExperimentCrossValidation.
     *
     * @return The report that collects the results from the different folds.
     */
    public Class<? extends ReportBase> getBatchTrainTestReportClass();

    /**
     * @param  collection of cas
     * @return The fold dimension bundle for CV
     */
    public <T extends DimensionBundle<Collection<String>>> T getFoldDimensionBundle(String[] files,
            int folds);

    /**
     * @param name
     *            Which name should be returned
     * @return Returns the filename that is used for specific files by the framework
     */
    public String getFrameworkFilename(AdapterNameEntries name);

    /**
     * @return
     * 		Returns a task that deals with serializing a model
     */
	public Class<? extends ModelSerializationTask> getSaveModelTask();

	/**
	 * 
	 * @return
	 * 		boolean value wheter sparse features shall be used or not
	 */
	public boolean useSparseFeatures();
}