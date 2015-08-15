---
layout: page-fullwidth
title: "Discriminators (0.5.0 Release)"
permalink: "/Discriminators_0_5_0/"
---

### Mandatory Parameters

  * `readers` (DimensionBundle)
    * `readerTrain` (name of the reader for the training data)
    * `readerTrainParams` (further parameters for the training data reader)
    * `readerTest` (name of the reader for the test data) _not necessary for Crossvalidation_
    * `readerTestParams` (further parameters for the test data reader) _not necessary for Crossvalidation_
  * `dataWriter` (a writer to produce the input for the classification framework, e.g. Weka)
  * `classificationArguments` (the classification algorithm and a list of arguments to parametrize it)
  * `featureSet` (the names of the feature extractors to use)
  * `pipelineParameters` (further parameters necessary to configure the feature extractors)

### Optional Parameters with default values

  * `multiLabel`: false (if true, you need to set threshold)
  * `threshold`: 0 _needs to be set when multiLabel is true_
  * `featureSelection` (DimensionBundle)
    * `attributeEvaluator`
    * `featureSearcher` (only for single-label)
    * `labelTransformationMethod` (only for multi-label)
    * `numLabelsToKeep` (only for multi-label)
    * `applySelection`
  * `isRegressionExperiment`: false
  * `isPairClassification`: false
  * `isUnitClassification`: false