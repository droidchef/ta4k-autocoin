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
package eu.verdelhan.ta4j

import eu.verdelhan.ta4j.Order.OrderType.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class TradingRecordTest {

    private lateinit var emptyRecord: TradingRecord
    private lateinit var openedRecord: TradingRecord
    private lateinit var closedRecord: TradingRecord

    @Before
    fun setUp() {
        emptyRecord = BaseTradingRecord()
        openedRecord = BaseTradingRecord(Order.buyAt(0), Order.sellAt(3),
                Order.buyAt(7))
        closedRecord = BaseTradingRecord(Order.buyAt(0), Order.sellAt(3),
                Order.buyAt(7), Order.sellAt(8)).closeCurrent()
    }

    @Test
    fun shouldEmptyBeNew() {
        assertThat(emptyRecord.currentTrade.isNew()).isTrue()
    }

    @Test
    fun shouldOpenedBeOpened() {
        assertThat(openedRecord.currentTrade.isOpened()).isTrue()
    }

    @Test
    fun shouldBeNewAfterClosing() {
        assertThat(closedRecord.currentTrade.isNew()).isTrue()
    }

    @Test
    fun shouldEmptyNotBeClosed() {
        assertThat(emptyRecord.isClosed()).isFalse()
    }

    @Test
    fun shouldOpenedNotBeClosed() {
        assertThat(openedRecord.isClosed()).isFalse()
    }

    @Test
    fun shouldClosedHaveTradeRecorded() {
        assertThat(closedRecord.getTradeCount()).isEqualTo(2)
    }

    @Test
    fun shouldBeOpenedAfterSingleBuyEntry() {
        // given
        val record = BaseTradingRecord()
        // when
        record.enter(1)
        // then
        assertThat(record.currentTrade.isOpened()).isTrue()
    }

    @Test
    fun shouldBeOpenedAfterMultipleBuyEntries() {
        // given
        val record = BaseTradingRecord()
        // when
        record.enter(1)
        record.enter(2)
        // then
        assertThat(record.currentTrade.isOpened()).isTrue()
    }

    @Test
    fun shouldCreateNewTradeAfterClosingCurrent() {
        // given
        val record = BaseTradingRecord()
        // when
        record.enter(1)
        record.exit(2)
        record.closeCurrent()
        // then
        assertThat(record.currentTrade.isNew()).isTrue()
    }

    @Test(expected = IllegalStateException::class)
    fun shouldThrowExceptionWhenClosingOpenWithoutAnyExitOrder() {
        openedRecord.closeCurrent()
    }

    @Test(expected = IllegalStateException::class)
    fun shouldThrowExceptionWhenClosingEmpty() {
        emptyRecord.closeCurrent()
    }

    @Test(expected = IllegalStateException::class)
    fun shouldThrowExceptionWhenClosingClosed() {
        closedRecord.closeCurrent()
    }

    @Test
    fun shouldCloseTradeWhenBuySellBuy() {
        // given
        val record = BaseTradingRecord(Order.buyAt(0), Order.sellAt(3), Order.sellAt(4))
        // when
        record.enter(5)
        // then
        assertThat(record.currentTrade.isNew()).isFalse()
        assertThat(record.getTradeCount()).isEqualTo(1)
        assertThat(record.trades[0].isClosed()).isTrue()
    }

    @Test
    fun shouldCloseCurrentTradeWhenSellBuySell() {
        // given
        val record = BaseTradingRecord(Order.sellAt(0), Order.buyAt(3), Order.buyAt(4))
        // when
        record.enter(5)
        // then
        assertThat(record.currentTrade.isNew()).isFalse()
        assertThat(record.getTradeCount()).isEqualTo(1)
        assertThat(record.trades[0].isClosed()).isTrue()
    }

    @Test
    fun operate() {
        val record = BaseTradingRecord()

        record.enter(1)
        assertThat(record.currentTrade.isOpened()).isTrue()
        assertThat(record.getTradeCount()).isZero()
        assertThat(record.getLastTrade()).isNull()
        assertThat(record.getLastOrder()).isEqualTo(Order.buyAt(1))
        assertThat(record.getLastOrder(BUY)).isEqualTo(Order.buyAt(1))
        assertThat(record.getLastOrder(SELL)).isNull()
        assertThat(record.getLastEntry()).isEqualTo(Order.buyAt(1))
        assertThat(record.getLastExit()).isNull()

        record.exit(3)
        record.enter(4)
        assertThat(record.currentTrade.isNew()).isFalse()
        assertThat(record.getTradeCount()).isOne()
        assertThat(record.getLastTrade()).isEqualTo(Trade(Order.buyAt(1), Order.sellAt(3)))
        assertThat(record.getLastOrder(SELL)).isEqualTo(Order.sellAt(3))
        assertThat(record.getLastOrder(BUY)).isEqualTo(Order.buyAt(4))
        assertThat(record.getLastEntry()).isEqualTo(Order.buyAt(4))
        assertThat(record.getLastExit()).isEqualTo(Order.sellAt(3))

        record.enter(5)
        assertThat(record.currentTrade.isOpened()).isTrue()
        assertThat(record.getTradeCount()).isOne()
        assertThat(record.getLastTrade()).isEqualTo(Trade(Order.buyAt(1), Order.sellAt(3)))
        assertThat(record.getLastOrder()).isEqualTo(Order.buyAt(5))
        assertThat(record.getLastOrder(BUY)).isEqualTo(Order.buyAt(5))
        assertThat(record.getLastOrder(SELL)).isEqualTo(Order.sellAt(3))
        assertThat(record.getLastEntry()).isEqualTo(Order.buyAt(5))
        assertThat(record.getLastExit()).isEqualTo(Order.sellAt(3))
    }

    @Test
    fun shouldHaveProperTradeCount() {
        assertThat(emptyRecord.getTradeCount()).isZero()
        assertThat(openedRecord.getTradeCount()).isOne()
        assertThat(closedRecord.getTradeCount()).isEqualTo(2)
    }

    @Test
    fun shouldHaveProperLastTrade() {
        assertThat(emptyRecord.getLastTrade()).isNull()
        assertThat(openedRecord.getLastTrade()).isEqualTo(Trade(Order.buyAt(0), Order.sellAt(3)))
        assertThat(closedRecord.getLastTrade()).isEqualTo(Trade(Order.buyAt(7), Order.sellAt(8)))
    }

    @Test
    fun shouldHaveProperLastOrder() {
        // Last order
        assertThat(emptyRecord.getLastOrder()).isNull()
        assertThat(openedRecord.getLastOrder()).isEqualTo(Order.buyAt(7))
        assertThat(closedRecord.getLastOrder()).isEqualTo(Order.sellAt(8))
        // Last BUY order
        assertThat(emptyRecord.getLastOrder(BUY)).isNull()
        assertThat(openedRecord.getLastOrder(BUY)).isEqualTo(Order.buyAt(7))
        assertThat(closedRecord.getLastOrder(BUY)).isEqualTo(Order.buyAt(7))
        // Last SELL order
        assertThat(emptyRecord.getLastOrder(SELL)).isNull()
        assertThat(openedRecord.getLastOrder(SELL)).isEqualTo(Order.sellAt(3))
        assertThat(closedRecord.getLastOrder(SELL)).isEqualTo(Order.sellAt(8))
    }

    @Test
    fun shouldHaveProperLastEntry() {
        assertThat(emptyRecord.getLastEntry()).isNull()
        assertThat(openedRecord.getLastEntry()).isEqualTo(Order.buyAt(7))
        assertThat(closedRecord.getLastEntry()).isEqualTo(Order.buyAt(7))
    }

    @Test
    fun shouldHaveProperLastEntryExit() {
        assertThat(emptyRecord.getLastExit()).isNull()
        assertThat(openedRecord.getLastExit()).isEqualTo(Order.sellAt(3))
        assertThat(closedRecord.getLastExit()).isEqualTo(Order.sellAt(8))
    }
}
