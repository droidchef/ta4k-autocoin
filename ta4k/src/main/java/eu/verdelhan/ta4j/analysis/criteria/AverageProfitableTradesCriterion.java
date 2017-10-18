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

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.Trade;
import eu.verdelhan.ta4j.TradingRecord;

import java.util.List;

/**
 * Average profitable trades criterion.
 * <p>
 * The number of profitable trades.
 */
public class AverageProfitableTradesCriterion extends AbstractAnalysisCriterion {

    @Override
    public double calculate(TimeSeries series, Trade trade) {
        return (tradeProfit(series, trade).isGreaterThan(Decimal.ONE)) ? 1d : 0d;
    }

    private Decimal tradeProfit(TimeSeries series, Trade trade) {
        List<Integer> entryIndexes = trade.getEntryIndexes();
        List<Integer> exitIndexes = trade.getExitIndexes();

        Decimal result;
        if (trade.hasPrices()) {
            if (trade.entryIsBuy()) {
                result = trade.getExitsValue().dividedBy(trade.getEntriesValue());
            } else {
                result = trade.getEntriesValue().dividedBy(trade.getExitsValue());
            }
        } else {
            if (trade.entryIsBuy()) {
                // buy-then-sell trade
                result = series.getAverageTickClosePrices(exitIndexes).dividedBy(series.getAverageTickClosePrices(entryIndexes));
            } else {
                // sell-then-buy trade
                result = series.getAverageTickClosePrices(entryIndexes).dividedBy(series.getAverageTickClosePrices(exitIndexes));
            }
        }
        return result;
    }

    @Override
    public double calculate(TimeSeries series, TradingRecord tradingRecord) {
        int numberOfProfitable = 0;
        for (Trade trade : tradingRecord.getTrades()) {
            if (tradeProfit(series, trade).isGreaterThan(Decimal.ONE)) {
                numberOfProfitable++;
            }
        }
        return ((double) numberOfProfitable) / tradingRecord.getTradeCount();
    }

    @Override
    public boolean betterThan(double criterionValue1, double criterionValue2) {
        return criterionValue1 > criterionValue2;
    }
}
