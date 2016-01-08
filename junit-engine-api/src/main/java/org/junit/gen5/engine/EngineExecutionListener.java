/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine;

import java.util.Arrays;

import org.junit.gen5.engine.TestExecutionResult.Status;

/**
 * Listener to be notified of test execution events by
 * {@linkplain TestEngine test engines}.
 *
 * <p>Contrary to JUnit 4, {@linkplain TestEngine test engines}
 * must report events not only for {@linkplain TestDescriptor test descriptors}
 * that represent executable leaves but also for all intermediate containers.
 *
 * @since 5.0
 * @see TestEngine
 * @see ExecutionRequest
 */
public interface EngineExecutionListener {

	/**
	 * Must be called when a new, dynamic {@link TestDescriptor} has been
	 * registered.
	 *
	 * <p>A <em>dynamic test</em> is a test that is not known a-priori and
	 * therefore was not present in the test tree when discovering tests.
	 *
	 * @param testDescriptor the descriptor of the newly registered test
	 * or container
	 */
	void dynamicTestRegistered(TestDescriptor testDescriptor);

	/**
	 * Must be called when the execution of a leaf or subtree of the test tree
	 * has been skipped.
	 *
	 * <p>The {@link TestDescriptor} may represent a test or a container. In the
	 * case of a container, engines must not fire any additional events for its
	 * descendants.
	 *
	 * <p>A skipped test or subtree of tests must not be reported as
	 * {@linkplain #executionStarted started} or
	 * {@linkplain #executionFinished finished}.
	 *
	 * @param testDescriptor the descriptor of the skipped test or container
	 * @param reason a human-readable message describing why the execution
	 * has been skipped
	 */
	void executionSkipped(TestDescriptor testDescriptor, String reason);

	/**
	 * Must be called when the execution of a leaf or subtree of the test tree
	 * is about to be started.
	 *
	 * <p>The {@link TestDescriptor} may represent a test or a container. In the
	 * case of a container, engines have to fire additional events for its
	 * children.
	 *
	 * <p>This method may only be called if the test or container has not
	 * been {@linkplain #executionSkipped skipped}.
	 *
	 * <p>This method must be called for a container {@code TestDescriptor}
	 * <em>before</em> {@linkplain #executionStarted starting} or
	 * {@linkplain #executionSkipped skipping} any of its children.
	 *
	 * @param testDescriptor the descriptor of the started test or container
	 */
	void executionStarted(TestDescriptor testDescriptor);

	/**
	 * Must be called when the execution of a leaf or subtree of the test tree
	 * has finished, regardless of the outcome.
	 *
	 * <p>The {@link TestDescriptor} may represent a test or a container.
	 *
	 * <p>This method may only be called if the test or container has not
	 * been {@linkplain #executionSkipped skipped}.
	 *
	 * <p>This method must be called for a container {@code TestIdentifier}
	 * <em>after</em> all of its children have been
	 * {@linkplain #executionSkipped skipped} or have
	 * {@linkplain #executionFinished finished}.
	 *
	 * <p>The {@link TestExecutionResult} describes the result of the execution
	 * for the supplied {@code testDescriptor}. The result does not include or
	 * aggregate the results of its children. For example, a container with a
	 * failing test must be reported as {@link Status#SUCCESSFUL SUCCESSFUL} even
	 * if one or more of its children are reported as {@link Status#FAILED FAILED}.
	 *
	 * @param testDescriptor the descriptor of the finished test or container
	 * @param testExecutionResult the (unaggregated) result of the execution for
	 * the supplied {@code TestDescriptor}
	 *
	 * @see TestExecutionResult
	 */
	void executionFinished(TestDescriptor testDescriptor, TestExecutionResult testExecutionResult);

	/**
	 * Combine several {@code listeners} into a single one so that each one is receiving all events.
	 *
	 * @param listeners the individual listeners to combine
	 * @return the combined listener
	 */
	static EngineExecutionListener combine(EngineExecutionListener... listeners) {
		return new EngineExecutionListener() {

			@Override
			public void dynamicTestRegistered(TestDescriptor testDescriptor) {
				Arrays.stream(listeners).forEach(listener -> listener.dynamicTestRegistered(testDescriptor));
			}

			@Override
			public void executionSkipped(TestDescriptor testDescriptor, String reason) {
				Arrays.stream(listeners).forEach(listener -> listener.executionSkipped(testDescriptor, reason));
			}

			@Override
			public void executionStarted(TestDescriptor testDescriptor) {
				Arrays.stream(listeners).forEach(listener -> listener.executionStarted(testDescriptor));
			}

			@Override
			public void executionFinished(TestDescriptor testDescriptor, TestExecutionResult testExecutionResult) {
				Arrays.stream(listeners).forEach(
					listener -> listener.executionFinished(testDescriptor, testExecutionResult));

			}
		};
	}
}
