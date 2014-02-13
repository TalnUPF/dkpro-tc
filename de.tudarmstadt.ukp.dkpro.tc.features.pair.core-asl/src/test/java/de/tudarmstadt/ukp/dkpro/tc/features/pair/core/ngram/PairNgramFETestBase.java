package de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.gson.Gson;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureStore;
import de.tudarmstadt.ukp.dkpro.tc.core.io.AbstractPairReader;
import de.tudarmstadt.ukp.dkpro.tc.core.io.JsonDataWriter;
import de.tudarmstadt.ukp.dkpro.tc.core.util.TaskUtils;
import de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram.meta.LuceneNGramPairMetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.features.pair.core.ngram.meta.TestPairReader;
import de.tudarmstadt.ukp.dkpro.tc.fstore.simple.SimpleFeatureStore;


public abstract class PairNgramFETestBase
{
	protected List<List<String>> instanceList;
	protected List<List<String>> outcomeList;
	protected List<String> featureNames;
	
    protected TemporaryFolder folder;
    protected File lucenePath;
    protected File outputPath;
    protected Object[] parameters;
    
    protected void initialize() throws Exception{
    	folder = new TemporaryFolder();
        lucenePath = folder.newFolder();
        outputPath = folder.newFolder();
    	
    }

    protected void runPipeline()
            throws Exception
    {
        List<Object> parameterList = new ArrayList<Object>(Arrays.asList(parameters));
               
        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
                TestPairReader.class, 
                TestPairReader.PARAM_INPUT_FILE, "src/test/resources/data/textpairs.txt"
        );
        
        AnalysisEngineDescription segmenter = AnalysisEngineFactory.createEngineDescription(BreakIteratorSegmenter.class);
        
        AggregateBuilder builder = new AggregateBuilder();
        builder.add(segmenter, AbstractPairReader.INITIAL_VIEW, AbstractPairReader.PART_ONE);
        builder.add(segmenter, AbstractPairReader.INITIAL_VIEW, AbstractPairReader.PART_TWO);
        
        AnalysisEngineDescription metaCollector = AnalysisEngineFactory.createEngineDescription(
                LuceneNGramPairMetaCollector.class,
                LuceneNGramPairFeatureExtractor.PARAM_LUCENE_DIR, lucenePath
        );

        AnalysisEngineDescription featExtractorConnector = TaskUtils.getFeatureExtractorConnector(
                parameterList,
                outputPath.getAbsolutePath(),
                JsonDataWriter.class.getName(),
                false,
                false,
                LuceneNGramPairFeatureExtractor.class.getName()
        );

        // run meta collector
        SimplePipeline.runPipeline(reader, builder.createAggregateDescription(), metaCollector);

        // run FE(s)
        SimplePipeline.runPipeline(reader, builder.createAggregateDescription(), featExtractorConnector);

        Gson gson = new Gson();
        FeatureStore fs = gson.fromJson(FileUtils.readFileToString(new File(outputPath, JsonDataWriter.JSON_FILE_NAME)), SimpleFeatureStore.class);
        assertEquals(1, fs.getNumberOfInstances());
        assertEquals(1, fs.getUniqueOutcomes().size());

        String arff = FileUtils.readFileToString(new File(outputPath, JsonDataWriter.JSON_FILE_NAME)).replace("{", "").replace("}", "");

        System.out.println(arff);
        
        makeInstanceList(arff);
        makeOutcomesList(arff);
        makeFeatureNamesList(arff);
        
    }
	private void makeInstanceList(String arff)
	{
		instanceList = new ArrayList<List<String>>();
        String instancesString = arff.split("],")[0].replace("\"instanceList\":[", "");
        for(String instanceString: instancesString.split("]")){
        	if(instanceString.length() > 0){
        		List<String> newInstance = new ArrayList<String>();
        		for(String value: instanceString.replace("[", "").split(",")){
        			newInstance.add(value.replace("\"", ""));
        		}
        		instanceList.add(newInstance);
        	}
        }
	}
	private void makeOutcomesList(String arff)
	{
		outcomeList = new ArrayList<List<String>>();
        String outcomesString = arff.split("],")[1].replace("\"outcomeList\":[", "");
        for(String outcomeString: outcomesString.split("]")){
        	if(outcomeString.length() > 0){
        		List<String> newOutcome = new ArrayList<String>();
        		for(String value: outcomeString.replace("[", "").split(",")){
        			newOutcome.add(value.replace("\"", ""));
        		}
        		outcomeList.add(newOutcome);
        	}
        }
	}
	private void makeFeatureNamesList(String arff)
	{
		featureNames = new ArrayList<String>();
        String featuresString = arff.split("],")[2].replace("\"featureNames\":[", "").replace("]", "");
        for(String featureName: featuresString.split(",")){
        	featureNames.add(featureName.replace("\"", ""));
        }
	}
	
}