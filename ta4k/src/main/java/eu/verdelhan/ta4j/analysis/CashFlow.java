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
package eu.verdelhan.ta4j.analysis;

import eu.verdelhan.ta4j.Indicator;
import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.Trade;
import eu.verdelhan.ta4j.TradingRecord;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The cash flow.
 * <p>
 * This class allows to follow the money cash flow involved by a list of trades over a time series.
 */
public class CashFlow implements Indicator<Decimal> {

    /** The time series */
    private final TimeSeries timeSeries;

    /** The cash flow values */
    private List<Decimal> values = new ArrayList<>(Arrays.asList(Decimal.ONE));

    public CashFlow(TimeSeries timeSeries, Trade trade) {
        this.timeSeries = timeSeries;
        calculate(trade);
        fillToTheEnd();
    }

    public CashFlow(TimeSeries timeSeries, TradingRecord tradingRecord) {
        this.timeSeries = timeSeries;
        calculate(tradingRecord);
        fillToTheEnd();
    }

    /**
     * @param index the tick index
     * @return the cash flow value at the index-th position
     */
    @Override
    public Decimal getValue(int index) {
        return values.get(index);
    }

    @Override
    public TimeSeries getTimeSeries() {
        return timeSeries;
    }

    /**
     * @return the size of the time series
     */
    public int getSize() {
        return timeSeries.getTickCount();
    }

    private void calculate(Trade trade) {
        List<Integer> entryIndexes = trade.getEntryIndexes();
        List<Integer> exitIndexes = trade.getExitIndexes();

        int begin = entryIndexes.get(0) + 1;
        if (begin > values.size()) {
            Decimal lastValue = values.get(values.size() - 1);
            values.addAll(Collections.nCopies(begin - values.size(), lastValue));
        }
        int end = exitIndexes.get(exitIndexes.size() - 1);
        for (int i = Math.max(begin, 1); i <= end; i++) {
            Decimal ratio;
            if (trade.entryIsBuy()) {
                if (trade.hasPrices()) {
                    ratio = trade.getExitsValue().dividedBy(trade.getEntriesValue());
                } else {
                    ratio = timeSeries.getTick(i).getClosePrice().dividedBy(timeSeries.getAverageTickClosePrices(entryIndexes));
                }
            } else {
                if (trade.hasPrices()) {
                    ratio = trade.getEntriesValue().dividedBy(trade.getExitsValue());
                } else {
                    ratio = timeSeries.getAverageTickClosePrices(entryIndexes).dividedBy(timeSeries.getTick(i).getClosePrice());
                }
            }
            values.add(values.get(entryIndexes.get(0)).multipliedBy(ratio));
        }
    }

    private void calculate(TradingRecord tradingRecord) {
        for (Trade trade : tradingRecord.getTrades()) {
            calculate(trade);
        }
    }

    /**
     * Fills with last value till the end of the series.
     */
    private void fillToTheEnd() {
        if (timeSeries.getEndIndex() >= values.size()) {
            Decimal lastValue = values.get(values.size() - 1);
            values.addAll(Collections.nCopies(timeSeries.getEndIndex() - values.size() + 1, lastValue));
        }
    }
}
