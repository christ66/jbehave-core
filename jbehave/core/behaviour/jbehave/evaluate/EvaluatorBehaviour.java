/*
 * Created on 25-Dec-2003
 * 
 * (c) 2003-2004 ThoughtWorks
 * 
 * See license.txt for license details
 */
package jbehave.evaluate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jbehave.evaluate.listener.ListenerSupport;
import jbehave.framework.CriteriaVerifier;
import jbehave.framework.Result;
import jbehave.framework.Verify;

/**
 * Test the {@link Evaluator} class
 * 
 * @author <a href="mailto:dan@jbehave.org">Dan North</a>
 */
public class EvaluatorBehaviour {
    private final static List resultList = new ArrayList(); // handy place to store results
    private Evaluator runner;
    
    public void setUp() {
        runner = new Evaluator();
        resultList.clear();
    }

    public static class BehaviourClassWithOneBehaviour {
        public void shouldDoSomething() {
        }
    }
    
    public static class BehaviourClassWithTwoBehaviours {
        public void shouldDoOneThing() {
        }
        public void shouldDoAnotherThing() {
        }
    }

    public void shouldAddBehaviourClass() throws Exception {
        Verify.equal(0, runner.countBehaviourClasses());
        Verify.equal(0, runner.countBehaviours());
        
        runner.addBehaviourClass(BehaviourClassWithOneBehaviour.class);
        Verify.equal(1, runner.countBehaviourClasses());
        Verify.equal(1, runner.countBehaviours());
        Verify.equal(BehaviourClassWithOneBehaviour.class, runner.getBehaviourClass(0));
        
        runner.addBehaviourClass(BehaviourClassWithTwoBehaviours.class);
        Verify.equal(2, runner.countBehaviourClasses());
        Verify.equal(3, runner.countBehaviours());
        Verify.equal(BehaviourClassWithTwoBehaviours.class, runner.getBehaviourClass(1));
    }
    
    public void shouldCountBehaviours() throws Exception {
        runner.addBehaviourClass(BehaviourClassWithOneBehaviour.class);
    }

    private static class RunStartedListener extends ListenerSupport {
        private final Evaluator expectedRunner;
        private final String message;
        
        public RunStartedListener(Evaluator expectedRunner, String message) {
            this.expectedRunner = expectedRunner;
            this.message = message;
        }

        public void runStarted(Evaluator runner) {
            Verify.sameInstance(expectedRunner, runner);
            resultList.add(message);
        }
    }
    
    public static class BehaviourClassThatSaysHello {
        public void shouldSayHello() {
            resultList.add("hello");
        }
    }
    
    public void shouldNotifyListenersInOrderWhenRunStarts() throws Exception {
        runner.registerListener(new RunStartedListener(runner, "one"));
        runner.registerListener(new RunStartedListener(runner, "two"));
        runner.addBehaviourClass(BehaviourClassThatSaysHello.class);
        runner.evaluateCriteria();
        Verify.equal(Arrays.asList(
		new String[]{"one", "two", "hello"}), resultList);
    }

    private static class RunEndedListener extends ListenerSupport {
        private final Evaluator expectedRunner;
        private final String message;
        
        public RunEndedListener(Evaluator expectedRunner, String message) {
            this.expectedRunner = expectedRunner;
            this.message = message;
        }
        
        public void runEnded(Evaluator runner) {
            Verify.sameInstance(expectedRunner, runner);
            resultList.add(message);
        }
    }
    
    public void shouldNotifyListenersInOrderWhenRunEnds() throws Exception {
        runner.registerListener(new RunEndedListener(runner, "one"));
        runner.registerListener(new RunEndedListener(runner, "two"));
        runner.addBehaviourClass(BehaviourClassThatSaysHello.class);
        runner.evaluateCriteria();
        Verify.equal(Arrays.asList(
		new String[]{"hello", "one", "two"}), resultList);
    }

    private static class BehaviourClassStartedListener extends ListenerSupport {
        private final String message;
        
        public BehaviourClassStartedListener(String message) {
            this.message = message;
        }
        
        public void behaviourEvaluationStarted(Class behaviourClass) {
            resultList.add(message + ":" + behaviourClass.getName());
        }
    }
    
    public void shouldNotifyListenersInOrderWhenBehaviourClassStarts() throws Exception {
        runner.registerListener(new BehaviourClassStartedListener("one"));
        runner.registerListener(new BehaviourClassStartedListener("two"));
        runner.addBehaviourClass(BehaviourClassThatSaysHello.class);
        runner.evaluateCriteria();
        
        String expectedName = BehaviourClassThatSaysHello.class.getName();
        String[] expected = {"one:" + expectedName, "two:" + expectedName, "hello"};
        Verify.equal(Arrays.asList(expected), resultList);
    }

    private static class BehaviourClassEndedListener extends ListenerSupport {
        private final String message;
        
        public BehaviourClassEndedListener(String message) {
            this.message = message;
        }
        
        public void behaviourEvaluationEnded(Class behaviourClass) {
            resultList.add(message + ":" + behaviourClass.getName());
        }
    }
    
    public void shouldNotifyListenersInOrderWhenBehaviourClassEnds() throws Exception {
        runner.registerListener(new BehaviourClassEndedListener("one"));
        runner.registerListener(new BehaviourClassEndedListener("two"));
        runner.addBehaviourClass(BehaviourClassThatSaysHello.class);
        runner.evaluateCriteria();
        
        String expectedName = BehaviourClassThatSaysHello.class.getName();
        Verify.equal(Arrays.asList(
		new String[]{
		    "hello",
		    "one:" + expectedName,
		    "two:" + expectedName
		    }), resultList);
    }
    
    private static class BehaviourStartedListener extends ListenerSupport {
        private final String message;
        
        public BehaviourStartedListener(String message) {
            this.message = message;
        }
        
        public void beforeCriterionEvaluationStarts(CriteriaVerifier behaviour) {
            resultList.add(message + ":" + behaviour.getName());
        }
    }
    
    public void shouldNotifyListenersInOrderWhenBehaviourStarts() throws Exception {
        runner.registerListener(new BehaviourStartedListener("one"));
        runner.registerListener(new BehaviourStartedListener("two"));
        runner.addBehaviourClass(BehaviourClassThatSaysHello.class);
        runner.evaluateCriteria();
        Verify.equal(Arrays.asList(
		new String[]{"one:shouldSayHello", "two:shouldSayHello", "hello"}), resultList);
    }
    
    private static class BehaviourEndedListener extends ListenerSupport {
        private final String message;
        
        public BehaviourEndedListener(String message) {
            this.message = message;
        }
        
        public void afterCriterionEvaluationEnds(Result behaviourResult) {
            resultList.add(message + ":" + behaviourResult.getName());
        }
    }
    
    public void shouldNotifyListenersInOrderWhenBehaviourEnds() throws Exception {
        runner.registerListener(new BehaviourEndedListener("one"));
        runner.registerListener(new BehaviourEndedListener("two"));
        runner.addBehaviourClass(BehaviourClassThatSaysHello.class);
        runner.evaluateCriteria();
        Verify.equal(Arrays.asList(
		new String[]{"hello", "one:shouldSayHello", "two:shouldSayHello"}), resultList);
    }
    
    public void shouldNotifyBehaviourListenersForEveryBehaviour() throws Exception {
        runner.registerListener(new BehaviourStartedListener("started"));
        runner.registerListener(new BehaviourEndedListener("ended"));
        runner.addBehaviourClass(BehaviourClassWithTwoBehaviours.class);
        runner.evaluateCriteria();
        
        Verify.equal(4, resultList.size());
        
        Verify.that(resultList.contains("started:shouldDoOneThing"));
        Verify.that(resultList.contains("ended:shouldDoOneThing"));
        
        Verify.that(resultList.contains("started:shouldDoAnotherThing"));
        Verify.that(resultList.contains("ended:shouldDoAnotherThing"));
    }
}
