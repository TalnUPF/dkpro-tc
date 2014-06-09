package de.tudarmstadt.ukp.dkpro.tc.groovyexamples.regression.pair

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription

import org.apache.uima.analysis_engine.AnalysisEngineDescription
import org.apache.uima.resource.ResourceInitializationException

import weka.classifiers.functions.SMOreg
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter
import de.tudarmstadt.ukp.dkpro.lab.Lab
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask.ExecutionPolicy
import de.tudarmstadt.ukp.dkpro.tc.core.Constants
import de.tudarmstadt.ukp.dkpro.tc.examples.io.STSReader
import de.tudarmstadt.ukp.dkpro.tc.features.pair.core.length.DiffNrOfTokensPairFeatureExtractor
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchCrossValidationReport
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchOutcomeIDReport
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchTrainTestReport
import de.tudarmstadt.ukp.dkpro.tc.weka.report.RegressionReport
import de.tudarmstadt.ukp.dkpro.tc.weka.task.BatchTaskCrossValidation
import de.tudarmstadt.ukp.dkpro.tc.weka.task.BatchTaskTrainTest
import de.tudarmstadt.ukp.dkpro.tc.weka.writer.WekaDataWriter

/**
 * A demo for pair classification with a regression outcome.
 *
 * This uses the Semantic Text Similarity (STS) from the SemEval 2012 task. It computes text
 * similarity features between document pairs and then learns a regression model that predicts
 * similarity of unseen document pairs.
 */
public class RegressionDemo implements Constants {

    // === PARAMETERS===========================================================

    def experimentName = "RegressionExample"
    def NUM_FOLDS = 2
    def String inputFileTrain = "src/main/resources/data/sts2012/STS.input.MSRpar.txt"
    def String goldFileTrain = "src/main/resources/data/sts2012/STS.gs.MSRpar.txt"
    def String inputFileTest = "src/main/resources/data/sts2012/STS.input.MSRvid.txt"
    def String goldFileTest = "src/main/resources/data/sts2012/STS.gs.MSRvid.txt"

    // === DIMENSIONS===========================================================

    def dimReaders = Dimension.createBundle("readers", [
        readerTrain: STSReader,
        readerTrainParams: [
            STSReader.PARAM_INPUT_FILE,
            inputFileTrain,
            STSReader.PARAM_GOLD_FILE,
            goldFileTrain
        ],
        readerTest: STSReader,
        readerTestParams: [
            STSReader.PARAM_INPUT_FILE,
            inputFileTest,
            STSReader.PARAM_GOLD_FILE,
            goldFileTest
        ]
    ])

    def dimLearningMode = Dimension.create(DIM_LEARNING_MODE, LM_REGRESSION)
    def dimFeatureMode = Dimension.create(DIM_FEATURE_MODE, FM_PAIR)
    def dimDataWriter = Dimension.create(DIM_DATA_WRITER, WekaDataWriter.name)

    def dimClassificationArgs =
    Dimension.create(DIM_CLASSIFICATION_ARGS, [SMOreg.name])

    // yields really bad results. To improve the performance, use a string similarity
    // based feature extractor
    def dimFeatureSets = Dimension.create(
    DIM_FEATURE_SET, [
        DiffNrOfTokensPairFeatureExtractor.name
    ])

    // === Experiments =========================================================

    /**
     * Crossvalidation setting
     *
     * @throws Exception
     */
    protected void runCrossValidation() throws Exception
    {

        BatchTaskCrossValidation batchTask = [
            experimentName: experimentName + "-CV-Groovy",
            // we need to explicitly set the name of the batch task, as the constructor of the groovy setup must be zero-arg
            type: "Evaluation-"+ experimentName +"-CV-Groovy",
            preprocessingPipeline:  getPreprocessing(),
            innerReports: [RegressionReport],
            parameterSpace : [
                dimReaders,
                dimFeatureMode,
                dimLearningMode,
                dimDataWriter,
                dimClassificationArgs,
                dimFeatureSets
            ],
            executionPolicy: ExecutionPolicy.RUN_AGAIN,
            reports:         [BatchCrossValidationReport],
            numFolds: NUM_FOLDS]

        Lab.getInstance().run(batchTask)
    }

    /**
     * TrainTest Setting
     *
     * @throws Exception
     */
    protected void runTrainTest() throws Exception
    {

        BatchTaskTrainTest batchTask = [
            experimentName: experimentName + "-TrainTest-Groovy",
            // we need to explicitly set the name of the batch task, as the constructor of the groovy setup must be zero-arg
            type: "Evaluation-"+ experimentName +"-TrainTest-Groovy",
            preprocessingPipeline:  getPreprocessing(),
            innerReports: [RegressionReport],
            parameterSpace : [
                dimReaders,
                dimLearningMode,
                dimFeatureMode,
                dimDataWriter,
                dimClassificationArgs,
                dimFeatureSets
            ],
            executionPolicy: ExecutionPolicy.RUN_AGAIN,
            reports:         [
                BatchTrainTestReport,
                BatchOutcomeIDReport]
        ]

        // Run
        Lab.getInstance().run(batchTask)
    }

    private AnalysisEngineDescription getPreprocessing()
    throws ResourceInitializationException
    {
        return createEngineDescription(createEngineDescription(BreakIteratorSegmenter.class),
        createEngineDescription(OpenNlpPosTagger.class))
    }

    public static void main(String[] args)
    {
        new RegressionDemo().runTrainTest()
        new RegressionDemo().runCrossValidation()
    }
}