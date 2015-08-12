---
layout: page-fullwidth
title: "Getting Started with DKPro TC 0.5.0"
permalink: "/DemoExperiments_0_5_0/"
---

In this introduction, we assume a certain familiarity with machine learning, natural language processing and the respective terminology. This document is not intended to be an introduction into these topics in general.


Please make sure that you have set up an environment variable **DKPRO\_HOME**. The variable should point to a (possibly yet empty) directory which is intended to store any sort of resources which are to be used by any DKPro component. One way to set the variable **DKPRO\_HOME** (there are several other ways) is to include the following line of code at the beginning of the main method of your experiment class:

```
System.setProperty("DKPRO_HOME", "pathToYourDKproDirectory");
// example of pathToYourDKproDirectory: /home/user/workspace/DKPRO_HOME 
```



DKPro TC comes with a collection of demo experiments which show various ways to define your experiment setups.

Currently, there are four basic example experiments:

```
de.tudarmstadt.ukp.dkpro.tc.demo.twentynewsgroups-gpl
de.tudarmstadt.ukp.dkpro.tc.demo.reuters-gpl
de.tudarmstadt.ukp.dkpro.tc.demo.regression-gpl
de.tudarmstadt.ukp.dkpro.tc.demo.pairtwentynewsgroups-gpl
```

All come with a set of data and can be run right away. The _twentynewsgroups_ experiment is a binary, single-label classification task. The _reuters_ example is a multi-classification task.  The _regression_ demo shows how to use DKPro-TC for regression experiments. The _pairtwentynewsgroups_ demo is a text-pair classification task.
If you don't know where to start, go with the twentynewsgroups demo first, as it has the most extensive documentation and configuration classes.

## Binary Classification with DKPro-TC: twentynewsgroups demo

It might be advisable to open the code in parallel to reading this tutorial, as we will not copy the whole code in here.

These are some of the files you should find in this project:

```
├── pom.xml
└── src
   ├── main.groovy...dkpro
   │                   └── twentynewsgroups
   │                          ├── io
   │                          │   └── TwentyNewsgroupsCorpusReader.java
   │                          ├── TwentyNewsgroupsJavaExperiment.java
   │                          └── TwentyNewsgroupsGroovyExperiment.groovy
   │                          
   └─ main.resources
          └── data
              ├── bydate-test
              │   ├── alt.atheism
              │   ├── comp.graphics
              │   ├── comp.os.ms-windows.misc
              │   └── comp.sys.ibm.pc.hardware
              └── bydate-train
                  ├── alt.atheism
                  ├── comp.graphics
                  ├── comp.os.ms-windows.misc
                  └── comp.sys.ibm.pc.hardware

```

There are two ways to run the experiment:
  * `TwentyNewsgroupsGroovyExperiment` (Groovy configuration)
  * `TwentyNewsgroupsJavaExperiment` (Java configuration)

### TwentyNewsgroupsGroovyExperiment

It takes care of

  * loading the data
  * extracting features (which feature extractors are used and how to configure them)
  * training classifiers (which classifiers to use and how to configure them)
  * evaluating classifiers (either with designated train/test sets or using cross-validation)
  * writing results (which reports to use)

Parameters which should be tested for different values and combinations will be defined as [discriminators](http://code.google.com/p/dkpro-lab/). These parameters are usually prefixed with _dim_ and will be injected into the `ParameterSpace`.
A full list of all discriminators in DKPro-TC can be found [here](Discriminators.md).

The input data and reader(s) are defined as `DimensionBundle`.

```
def dimReaderTrain = Dimension.createBundle("readerTrain", [
   readerTrain: TwentyNewsgroupsCorpusReader.class,
   readerTrainParams: [
      "sourceLocation", corpusFilePathTrain,
      "language", languageCode,
      "patterns", TwentyNewsgroupsCorpusReader.INCLUDE_PREFIX + "*/*.txt"
   ]
]);
```

In this case, the `TwentyNewsgroupsCorpusReader` will read all the .txt-files that can be found in (sub-)directories of `corpusFilePathTrain`.

The collected data is then processed via

```
private AnalysisEngineDescription getPreprocessing()
```

In the given examples two preprocessing methods are used:

```
BreakIteratorSegmenter
OpenNlpPosTagger
```

A segmenter and a Part-of-Speech Tagger.

The feature extractors which will be used are defined as

```
def dimFeatureSets = Dimension.create("featureSet"...
```

Any configuration parameters which can be set for the feature extractors (e.g. uni-, bi- and trigrams for n-gram features) are defined as follows:

```
def dimPipelineParameters = Dimension.create("pipelineParameters" ...
```

The classifiers as defined here:

```
def dimClassificationArgs = Dimension.create("classificationArguments"...
```

### runCrossValidation()

In the presented example, two folds are used:

```
def numFolds = 2;
```

Gluing all of the various parameters and processing tasks together to actually perform all the tasks is governed by `runCrossValidation()`

### runTrainTest()

Similarly, for the designated test and train sets.

### main()

This calls both the cross-validation and the train/test runs.

In your `DKPRO_HOME` folder, you will find a set of directories storing intermediate and final evaluation results of your experiments:
The `Evaluation...` folders (usually one for the TrainTest setup and one for Crossvalidation, named according to the experiment name setup on the overall BatchTask) contain the final results for all runs of the pipeline.
E.g., the `eval.xls` file contains information of the performance of the individual classifiers (especially useful if you want to compare several classifiers on the same data set).

Once you got this example running as it is, you can start adapting various parameters:

  * using different data sets - which are completely up to you
  * using different features - any that you can think of. Please have a look at the respective classes to get an idea about the parameters you might have to configure for each of the feature extractors.
  * using different classifiers - please refer to the WEKA-JavaDoc for further information on that. Currently, we only support WEKA for classification.