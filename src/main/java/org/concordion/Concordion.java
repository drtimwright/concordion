package org.concordion;

import java.io.IOException;
import java.util.List;

import org.concordion.api.*;
import org.concordion.internal.SummarizingResultRecorder;

public class Concordion {

    private final SpecificationLocator specificationLocator;
    private final EvaluatorFactory evaluatorFactory;
    private final SpecificationReader specificationReader;
    private Object fixture;
    private final Resource resource;
    private final SpecificationByExample specification;

    public Concordion(SpecificationLocator specificationLocator, SpecificationReader specificationReader, EvaluatorFactory evaluatorFactory, Object fixture) throws IOException {
        this.specificationLocator = specificationLocator;
        this.specificationReader = specificationReader;
        this.evaluatorFactory = evaluatorFactory;
        this.fixture = fixture;

        if (fixture != null) {
            resource = specificationLocator.locateSpecification(fixture);
            specification = loadSpecificationFromResource(resource);
        } else {
            resource = null;
            specification = null;
        }
    }

    public ResultSummary process() throws IOException {
        return process(specification, fixture);
    }

    public ResultSummary process(Resource resource) throws IOException {
        return process(loadSpecificationFromResource(resource), fixture);
    }

    public ResultSummary process(Resource resource, Object fixture) throws IOException {
        return process(loadSpecificationFromResource(resource), fixture);
    }

    private ResultSummary process(SpecificationByExample specification, Object fixture) {
//        try {
            SummarizingResultRecorder resultRecorder = new SummarizingResultRecorder();
            resultRecorder.setSpecificationDescription(getDefaultFixtureName(fixture));
            specification.process(evaluatorFactory.createEvaluator(fixture), resultRecorder);
            return resultRecorder;
//        } catch (ParsingException e) {
//        	e.printStackTrace();
//        	throw e;
////        	throw new ConcordionAssertionError("Could not parse resource " + resource.getPath(), new SingleResultSummary(Result.EXCEPTION));
//       }
    }

    public List<String> getExampleNames() throws IOException {
        return specification.getExampleNames();
    }

    public ResultSummary processExample(String example) throws IOException {

//        try {
        SummarizingResultRecorder resultRecorder = new SummarizingResultRecorder();
        resultRecorder.setSpecificationDescription(example);
        specification.processExample(evaluatorFactory.createEvaluator(fixture), example, resultRecorder);
        return resultRecorder;
//        } catch (ParsingException e) {
//        	e.printStackTrace();
//        	throw e;
////        	throw new ConcordionAssertionError("Could not parse resource " + resource.getPath(), new SingleResultSummary(Result.EXCEPTION));
//       }
    }


    /**
     * Loads the specification for the specified fixture.
     *
     * @return
     * @throws IOException if the fixture's specification cannot be loaded
     */
    private SpecificationByExample loadSpecificationFromFixture() throws IOException {
        return loadSpecificationFromResource(resource);
    }

    /**
     * Loads the specification for the specified fixture.
     *
     * @param resource the resource to load
     * @return a SpecificationByExample object to use
     * @throws IOException if the resource cannot be loaded
     */
    private SpecificationByExample loadSpecificationFromResource(Resource resource) throws IOException {
        Specification specification= specificationReader.readSpecification(resource);

        SpecificationByExample specificationByExample;
        if (specification instanceof SpecificationByExample) {
            specificationByExample = (SpecificationByExample) specification;
        } else {
            specificationByExample = new SpecificationToSpecificationByExampleAdaptor(specification);
        }
        if (fixture != null) {
            specificationByExample.setFixtureClass(fixture.getClass());
        }
        return specificationByExample;
    }

    public static String getDefaultFixtureName(Object fixture) {
        Class<?> fixtureClass = fixture==null?Class.class:fixture.getClass();
        return getDefaultFixtureClassName(fixtureClass);
    }

    public static String getDefaultFixtureClassName(Class<?> fixtureClass) {
        return ("[Concordion Specification for '" + fixtureClass.getSimpleName()).replaceAll("Test$", "']"); // Based on suggestion by Danny Guerrier
    }
}
