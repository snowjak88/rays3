package org.snowjak.rays3;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.math3.util.FastMath;

public class Global {

	/**
	 * Defines the margin by which two double values may be said to be "near"
	 * one another.
	 */
	public static final double				DOUBLE_TOLERANCE	= 1e-20;

	/**
	 * The central thread-executor. Whenever possible, submit your threads to
	 * this executor.
	 */
	public static final ThreadPoolExecutor	EXECUTOR			= (ThreadPoolExecutor) Executors
			.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	/**
	 * A pre-initialized pseudo-random number generator.
	 */
	public static final Random				RND					= new Random(System.currentTimeMillis());

	/**
	 * Determine if two doubles are "near" one another (using
	 * {@link #DOUBLE_TOLERANCE}).
	 * 
	 * @param d1
	 * @param d2
	 * @return
	 */
	public static final boolean isNear(double d1, double d2) {

		return FastMath.abs(d1 - d2) <= Global.DOUBLE_TOLERANCE;
	}
}
