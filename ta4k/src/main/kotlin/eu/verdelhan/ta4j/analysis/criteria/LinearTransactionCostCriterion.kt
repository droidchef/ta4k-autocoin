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
package eu.verdelhan.ta4j.analysis.criteria

import eu.verdelhan.ta4j.TimeSeries
import eu.verdelhan.ta4j.Trade
import eu.verdelhan.ta4j.TradingRecord

/**
 * A linear transaction cost criterion.
 * <p>
 * That criterion calculate the transaction cost according to an initial traded amount
 * and a linear function defined by a and b (a * x + b).
 */
class LinearTransactionCostCriterion
/**
 * Constructor.
 * (a * x + b)
 * @param initialAmount the initially traded amount
 * @param a the a coefficient (e.g. 0.005 for 0.5% per {@link Order order})
 * @param b the b constant (e.g. 0.2 for $0.2 per {@link Order order})
 */
@JvmOverloads constructor(private val initialAmount: Double, private val a: Double, private val b: Double = 0.0) : AbstractAnalysisCriterion() {

    private val profit = TotalProfitCriterion()

    override fun calculate(series: TimeSeries, trade: Trade) = getTradeCost(series, trade, initialAmount)

    override fun calculate(series: TimeSeries, tradingRecord: TradingRecord): Double {
        var totalCosts = 0.0
        var tradedAmount = initialAmount
        for (trade in tradingRecord.trades) {
            val tradeCost = getTradeCost(series, trade, tradedAmount)
            totalCosts += tradeCost
            // To calculate the new traded amount:
            // - Remove the cost of the first order
            // - Multiply by the profit ratio
            tradedAmount = (tradedAmount - tradeCost) * profit.calculate(series, trade)
        }
        // Special case: if the current trade is open
        val currentTrade = tradingRecord.currentTrade
        if (currentTrade.isOpened()) {
            totalCosts += getOrderCost(tradedAmount)
        }
        return totalCosts
    }

    override fun betterThan(criterionValue1: Double, criterionValue2: Double) = criterionValue1 < criterionValue2

    /**
     * @param tradedAmount the traded amount for the order
     * @return the absolute order cost
     */
    private fun getOrderCost(tradedAmount: Double) = a * tradedAmount + b

    /**
     * TODO adjust to multiple entries/exits model
     * @param series the time series
     * @param trade a trade
     * @param initialAmount the initially traded amount for the trade
     * @return the absolute total cost of all orders in the trade
     */
    private fun getTradeCost(series: TimeSeries, trade: Trade, initialAmount: Double): Double {
        var totalTradeCost = 0.0
        if (trade.getEntries().isNotEmpty()) {
            if (trade.hasAmounts()) {
                trade.getEntries().forEach { totalTradeCost += getOrderCost(it.amount.toDouble()) }
            } else totalTradeCost = getOrderCost(initialAmount)
            if (trade.getExits().isNotEmpty()) {
                // To calculate the new traded amount:
                // - Remove the cost of the first order
                // - Multiply by the profit ratio
                val newTradedAmount = (initialAmount - totalTradeCost) * profit.calculate(series, trade)
                if (trade.hasAmounts()) {
                    trade.getExits().forEach { totalTradeCost += getOrderCost(it.amount.toDouble()) }
                } else totalTradeCost += getOrderCost(newTradedAmount)
            }
        }
        return totalTradeCost
    }
}
