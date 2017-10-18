/**
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2014-2017 Marc de Verdelhan & respective authors (see AUTHORS)
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package eu.verdelhan.ta4j.analysis.criteria;

import eu.verdelhan.ta4j.*;
import eu.verdelhan.ta4j.mocks.MockTimeSeries;
import eu.verdelhan.ta4j.trading.rules.BooleanRule;
import eu.verdelhan.ta4j.trading.rules.FixedRule;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class AbstractAnalysisCriterionTest {

    class AlwaysBuyAfterSellRule implements Rule {
        @Override
        public boolean isSatisfied(int index, TradingRecord tradingRecord) {
            boolean result = true;
            if (tradingRecord.getLastOrder() != null && tradingRecord.getLastOrder().getType() == Order.OrderType.BUY) {
                result = false;
            }
            return result;
        }
    }

    class AlwaysSellAfterBuyRule implements Rule {
        @Override
        public boolean isSatisfied(int index, TradingRecord tradingRecord) {
            boolean result = false;
            if (tradingRecord.getLastOrder() != null && tradingRecord.getLastOrder().getType() == Order.OrderType.BUY) {
                result = true;
            }
            return result;
        }
    }

    private Strategy alwaysBuyThenSellStrategy;

    private Strategy buyAndHoldStrategy;

    private List<Strategy> strategies;

    @Before
    public void setUp() {
        alwaysBuyThenSellStrategy = new BaseStrategy(new AlwaysBuyAfterSellRule(), new AlwaysSellAfterBuyRule());
        buyAndHoldStrategy = new BaseStrategy(new FixedRule(0), new FixedRule(4));
        strategies = Arrays.asList(alwaysBuyThenSellStrategy, buyAndHoldStrategy);
    }

    @Test
    public void bestShouldBeAlwaysOperateOnProfit() {
        MockTimeSeries series = new MockTimeSeries(6.0, 9.0, 6.0, 6.0);
        TimeSeriesManager manager = new TimeSeriesManager(series);
        Strategy bestStrategy = new TotalProfitCriterion().chooseBest(manager, strategies);
        assertEquals(alwaysBuyThenSellStrategy, bestStrategy);
    }

    @Test
    public void bestShouldBeBuyAndHoldOnLoss() {
        MockTimeSeries series = new MockTimeSeries(6.0, 3.0, 6.0, 6.0);
        TimeSeriesManager manager = new TimeSeriesManager(series);
        Strategy bestStrategy = new TotalProfitCriterion().chooseBest(manager, strategies);
        assertEquals(buyAndHoldStrategy, bestStrategy);
    }

    @Test
    public void toStringMethod() {
        AbstractAnalysisCriterion c1 = new AverageProfitCriterion();
        assertEquals("Average Profit", c1.toString());
        AbstractAnalysisCriterion c2 = new BuyAndHoldCriterion();
        assertEquals("Buy And Hold", c2.toString());
        AbstractAnalysisCriterion c3 = new RewardRiskRatioCriterion();
        assertEquals("Reward Risk Ratio", c3.toString());
    }

}
