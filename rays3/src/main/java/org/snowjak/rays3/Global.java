package org.snowjak.rays3;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.math3.util.FastMath;

public class Global {

	/**
	 * Defines the margin by which two double values may be said to be "near"
	 * one another.
	 */
	public static final double						DOUBLE_TOLERANCE	= 1e-12;

	/**
	 * The main rendering thread executor.
	 */
	public static final ThreadPoolExecutor			RENDER_EXECUTOR		= (ThreadPoolExecutor) Executors
			.newCachedThreadPool();

	/**
	 * A single thread allocated to execute periodic tasks. Note that there is
	 * only <em>one</em> thread so allocated, so keep your scheduled tasks
	 * light!
	 */
	public static final ScheduledExecutorService	SCHEDULED_EXECUTOR	= Executors.newSingleThreadScheduledExecutor();

	/**
	 * A pre-initialized pseudo-random number generator.
	 */
	public static final Random						RND					= new Random(System.currentTimeMillis());

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
