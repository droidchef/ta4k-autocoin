package eu.verdelhan.ta4j.analysis.criteria

import eu.verdelhan.ta4j.*
import eu.verdelhan.ta4j.mocks.MockTimeSeries
import org.junit.Assert.*
import org.junit.Test

/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2017 Marc de Verdelhan & respective authors (see AUTHORS)
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
class TotalProfitCriterionTest {

    private val profit: AnalysisCriterion = TotalProfitCriterion()

//    private fun buyAtWithAmount(series: TimeSeries, index: Int, amount: Int) = Order.buyAt(index, series.getTick(index).closePrice, Decimal.valueOf(amount))
//
//    private fun sellAtWithAmount(series: TimeSeries, index: Int, amount: Int) = Order.sellAt(index, series.getTick(index).closePrice, Decimal.valueOf(amount))
//
//    @Test
//    fun calculateWithMultipleBuyOrdersWhenSellingAll() {
//        val series = MockTimeSeries(100.0, 105.0, 100.0, 110.0, 95.0, 105.0)
//        val tradingRecord = BaseTradingRecord(
//                buyAtWithAmount(series, 0, 2),
//                buyAtWithAmount(series, 2, 1),
//                sellAtWithAmount(series, 3, 3))
//
//        assertEquals(1.10, profit.calculate(series, tradingRecord), TATestsUtils.TA_OFFSET)
//    }

    @Test
    fun calculateOnlyWithGainTrades() {
        val series = MockTimeSeries(100.0, 105.0, 110.0, 100.0, 95.0, 105.0)
        val tradingRecord = BaseTradingRecord(
                Order.buyAt(0), Order.sellAt(2),
                Order.buyAt(3), Order.sellAt(5)).closeCurrent()

        assertEquals(1.10 * 1.05, profit.calculate(series, tradingRecord), TATestsUtils.TA_OFFSET)
    }

    @Test
    fun calculateOnlyWithLossTrades() {
        val series = MockTimeSeries(100.0, 95.0, 100.0, 80.0, 85.0, 70.0)
        val tradingRecord = BaseTradingRecord(
                Order.buyAt(0), Order.sellAt(1),
                Order.buyAt(2), Order.sellAt(5)).closeCurrent()

        assertEquals(0.95 * 0.7, profit.calculate(series, tradingRecord), TATestsUtils.TA_OFFSET)
    }

    @Test
    fun calculateProfitWithTradesThatStartSelling() {
        val series = MockTimeSeries(100.0, 95.0, 100.0, 80.0, 85.0, 70.0)
        val tradingRecord = BaseTradingRecord(
                Order.sellAt(0), Order.buyAt(1),
                Order.sellAt(2), Order.buyAt(5)).closeCurrent()

        assertEquals((1 / 0.95) * (1 / 0.7), profit.calculate(series, tradingRecord), TATestsUtils.TA_OFFSET)
    }

    @Test
    fun calculateWithNoTradesShouldReturn1() {
        val series = MockTimeSeries(100.0, 95.0, 100.0, 80.0, 85.0, 70.0)

        assertEquals(1.0, profit.calculate(series, BaseTradingRecord()), TATestsUtils.TA_OFFSET)
    }

    @Test
    fun calculateWithOpenedTradeShouldReturn1() {
        val series = MockTimeSeries(100.0, 95.0, 100.0, 80.0, 85.0, 70.0)
        val trade = Trade()
        assertEquals(1.0, profit.calculate(series, trade), TATestsUtils.TA_OFFSET)
        trade.enter(0)
        assertEquals(1.0, profit.calculate(series, trade), TATestsUtils.TA_OFFSET)
    }

    @Test
    fun betterThan() {
        assertTrue(profit.betterThan(2.0, 1.5))
        assertFalse(profit.betterThan(1.5, 2.0))
    }
}
