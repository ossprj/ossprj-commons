package com.ossprj.commons.web.facade;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

/**
 * Thread.sleep()s for the specified number of seconds with a random percentage of plus/minus variance (fuzziness)
 */
public class FuzzyWait {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public void perform(final Integer numberOfSecondsInMillis, final Integer percentFuzziness) {

        try {
            // Should we add or subtract ?
            final Integer signedMultiplier = Math.random() >= 0.5 ? 1 : -1;
            final Double fuzzyMultiplier = percentFuzziness.doubleValue() / 100.0;
            final Double fuzzinessValue = numberOfSecondsInMillis.doubleValue() * fuzzyMultiplier;
            final Double fuzzinessFactor = Math.random() * fuzzinessValue * signedMultiplier;
            final Integer totalTimeToWait = (int) (numberOfSecondsInMillis.doubleValue() + fuzzinessFactor);

            logger.debug("Waiting for " + totalTimeToWait);

            Thread.sleep(totalTimeToWait);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        final FuzzyWait fuzzyWait = new FuzzyWait();
        fuzzyWait.perform(10000, 10);
        fuzzyWait.perform(10000, 10);
        fuzzyWait.perform(10000, 10);
        fuzzyWait.perform(10000, 10);
        fuzzyWait.perform(10000, 10);
        fuzzyWait.perform(10000, 10);
        fuzzyWait.perform(10000, 10);
        fuzzyWait.perform(10000, 10);
        fuzzyWait.perform(10000, 10);
        fuzzyWait.perform(10000, 10);
        fuzzyWait.perform(10000, 10);

    }
}
