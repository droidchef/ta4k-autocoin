/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Marc de Verdelhan & respective authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package eu.verdelhan.ta4j.analysis.criteria;

import eu.verdelhan.ta4j.AnalysisCriterion;
import eu.verdelhan.ta4j.Operation;
import eu.verdelhan.ta4j.OperationType;
import eu.verdelhan.ta4j.TATestsUtils;
import eu.verdelhan.ta4j.Trade;
import eu.verdelhan.ta4j.mocks.MockTimeSeries;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.Test;

public class NumberOfTradesCriterionTest {

    @Test
    public void calculateWithNoTrades() {
        MockTimeSeries series = new MockTimeSeries(100, 105, 110, 100, 95, 105);
        List<Trade> trades = new ArrayList<Trade>();

        AnalysisCriterion buyAndHold = new NumberOfTradesCriterion();
        assertEquals(0d, buyAndHold.calculate(series, trades), TATestsUtils.TA_OFFSET);
    }

    @Test
    public void calculateWithTwoTrades() {
        MockTimeSeries series = new MockTimeSeries(100, 105, 110, 100, 95, 105);
        List<Trade> trades = new ArrayList<Trade>();
        trades.add(new Trade(new Operation(0, OperationType.BUY), new Operation(2, OperationType.SELL)));
        trades.add(new Trade(new Operation(3, OperationType.BUY), new Operation(5, OperationType.SELL)));

        AnalysisCriterion buyAndHold = new NumberOfTradesCriterion();
        assertEquals(2d, buyAndHold.calculate(series, trades), TATestsUtils.TA_OFFSET);
    }

    @Test
    public void calculateWithOneTrade() {
        Trade trade = new Trade();
        NumberOfTradesCriterion tradesCriterion = new NumberOfTradesCriterion();

        assertEquals(1d, tradesCriterion.calculate(null, trade), TATestsUtils.TA_OFFSET);
    }

    @Test
    public void betterThan() {
        AnalysisCriterion criterion = new NumberOfTradesCriterion();
        assertTrue(criterion.betterThan(3, 6));
        assertFalse(criterion.betterThan(7, 4));
    }
}
