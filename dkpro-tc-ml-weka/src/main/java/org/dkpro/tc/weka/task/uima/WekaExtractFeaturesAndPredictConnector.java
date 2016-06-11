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
package org.dkpro.tc.weka.task.uima;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.Instance;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.task.uima.ConnectorBase;
import org.dkpro.tc.weka.task.WekaExtractFeaturesAndPredictTask;
import org.dkpro.tc.weka.util.WekaUtils;
import org.dkpro.tc.weka.writer.WekaDataWriter;
import meka.classifiers.multilabel.MultilabelClassifier;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.Instances;

/**
 * 
 * UIMA analysis engine that is used in the {@link WekaExtractFeaturesAndPredictTask} to apply the
 * feature extractors on each CAS, and classify them using a previously trained model.
 */
public class WekaExtractFeaturesAndPredictConnector
    extends ConnectorBase
{

    public static final String PARAM_OUTPUT_DIRECTORY = "outputDirectory";
    @ConfigurationParameter(name = PARAM_OUTPUT_DIRECTORY, mandatory = true)
    private File outputDirectory;

    public static final String PARAM_ARFF_FILE_TRAINING = "arffFileTrainingData";
    @ConfigurationParameter(name = PARAM_ARFF_FILE_TRAINING, mandatory = true)
    private File arffFileTrainingData;

    public static final String PARAM_BIPARTITION_THRESHOLD = "bipartitionThreshold";
    @ConfigurationParameter(name = PARAM_BIPARTITION_THRESHOLD, mandatory = true, defaultValue = "0.5")
    private String bipartitionThreshold;

    @ExternalResource(key = PARAM_FEATURE_EXTRACTORS, mandatory = true)
    protected FeatureExtractorResource_ImplBase[] featureExtractors;

    @ConfigurationParameter(name = PARAM_LEARNING_MODE, mandatory = true)
    private String learningMode;

    @ConfigurationParameter(name = PARAM_FEATURE_MODE, mandatory = true)
    private String featureMode;

    @ConfigurationParameter(name = PARAM_DEVELOPER_MODE, mandatory = true, defaultValue = "false")
    private boolean developerMode;

    public static final String PARAM_CLASSIFICATION_ARGUMENTS = "classificationArguments";
    @ConfigurationParameter(name = PARAM_CLASSIFICATION_ARGUMENTS, mandatory = true)
    private List<String> classificationArguments;

    private Map<String, List<String>> predictionMap;

    private Classifier wekaClassifier;
//    List<Attribute> attributes;
    List<String> allClassLabels;
    Instances trainingData;
    

    boolean isRegression;
    boolean isMultiLabel;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        isRegression = learningMode.equals(Constants.LM_REGRESSION);
        isMultiLabel = learningMode.equals(Constants.LM_MULTI_LABEL);

        predictionMap = new HashMap<String, List<String>>();

        if (featureExtractors.length == 0) {
            context.getLogger().log(Level.SEVERE, "No feature extractors have been defined.");
            throw new ResourceInitializationException();
        }

        try {
            if (isMultiLabel) {
                List<String> mlArgs = classificationArguments
                        .subList(1, classificationArguments.size());
                wekaClassifier = AbstractClassifier.forName(classificationArguments.get(0),
                        new String[] {});
                ((MultilabelClassifier) wekaClassifier).setOptions(mlArgs.toArray(new String[0]));
            }
            else {
                wekaClassifier = AbstractClassifier.forName(classificationArguments.get(0),
                        classificationArguments
                                .subList(1, classificationArguments.size()).toArray(new String[0]));
            }

            trainingData = WekaUtils.getInstances(arffFileTrainingData,
                    isMultiLabel);
            trainingData = WekaUtils.removeInstanceId(trainingData, isMultiLabel);
            wekaClassifier.buildClassifier(trainingData);

//            attributes = new ArrayList<Attribute>();
//            Enumeration<Attribute> atts = trainData.enumerateAttributes();
//            while (atts.hasMoreElements()) {
//                attributes.add(atts.nextElement());
//            }
//            attributes.add(trainData.classAttribute());
            if (!isRegression) {
                allClassLabels = WekaUtils.getClassLabels(trainingData, isMultiLabel);
            }
        }
        catch (Exception e) {
            throw new ResourceInitializationException(e);
        }
    }

    @Override
    public void process(JCas jcas)
        throws AnalysisEngineProcessException
    {
        weka.core.Instance wekaInstance;

        try {
            Instance instance = org.dkpro.tc.core.util.TaskUtils.getSingleInstance(
                featureMode, featureExtractors, jcas,
                developerMode, false, false);
  
            if (!isMultiLabel) {
                wekaInstance = WekaUtils.tcInstanceToWekaInstance(instance, trainingData,
                        allClassLabels, isRegression);
            }
            else {
                wekaInstance = WekaUtils.tcInstanceToMekaInstance(instance, trainingData,
                        allClassLabels);
            }
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(new IllegalStateException(e.getMessage()));
        }
        List<String> predicted = new ArrayList<String>();

        try {
            // singlelabel
            if (!isMultiLabel) {
                double val = wekaClassifier.classifyInstance(wekaInstance);
                if (!isRegression) {
                    predicted.add(wekaInstance.classAttribute().value((int) val));
                }
                else {
                    // regression
                    predicted.add(String.valueOf(val));
                }
            }
            // multilabel
            else {
                double[] prediction =
                        wekaClassifier.distributionForInstance(wekaInstance);
                for (int i = 0; i < prediction.length; i++) {
                    if (prediction[i] >= Double.valueOf(bipartitionThreshold)) {
                        String label = wekaInstance.attribute(i).name()
                                .split(WekaDataWriter.CLASS_ATTRIBUTE_PREFIX)[1];
                        predicted.add(label);
                    }
                }
            }
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }

        String docId = DocumentMetaData.get(jcas).getDocumentId();
        if (docId == null) {
            throw new AnalysisEngineProcessException("DocumentId cannot be empty", null);
        }
        predictionMap.put(docId, predicted);
    }

    @Override
    public void collectionProcessComplete()
        throws AnalysisEngineProcessException
    {
        super.collectionProcessComplete();

        File file = new File(outputDirectory, Constants.PREDICTION_MAP_FILE_NAME);
        try {
            FileOutputStream f = new FileOutputStream(file);
            ObjectOutputStream s = new ObjectOutputStream(f);
            s.writeObject(predictionMap);
            s.close();
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
}