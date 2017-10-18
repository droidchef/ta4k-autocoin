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
package eu.verdelhan.ta4j

import eu.verdelhan.ta4j.Order.OrderType
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class TradeTest {

    private lateinit var newTrade: Trade
    private lateinit var uncoveredTrade: Trade
    private lateinit var trEquals1: Trade
    private lateinit var trEquals2: Trade
    private lateinit var trNotEquals1: Trade
    private lateinit var trNotEquals2: Trade

    @Before
    fun setUp() {
        newTrade = Trade()
        uncoveredTrade = Trade(OrderType.SELL)

        trEquals1 = Trade()
        trEquals1.enter(1)
        trEquals1.exit(2)

        trEquals2 = Trade()
        trEquals2.enter(1)
        trEquals2.exit(2)

        trNotEquals1 = Trade(OrderType.SELL)
        trNotEquals1.enter(1)
        trNotEquals1.exit(2)

        trNotEquals2 = Trade(OrderType.SELL)
        trNotEquals2.enter(1)
        trNotEquals2.exit(2)
    }

    @Test(expected = IllegalStateException::class)
    fun whenOpenedShouldNotCalculateExitsValue() {
        newTrade.getExitsValue()
    }

    @Test(expected = IllegalStateException::class)
    fun whenNewShouldNotCalculateExitsValue() {
        newTrade.enter(1)
        newTrade.getExitsValue()
    }

    @Test(expected = IllegalStateException::class)
    fun whenEntryWithoutPriceShouldNotCalculateEntriesValue() {
        newTrade.enter(2)
        newTrade.getEntriesValue()
    }

    @Test
    fun whenSingleEntryWithoutAmountShouldCalculateEntriesValue() {
        newTrade.enter(2, 1.5)
        assertThat(newTrade.getEntriesValue()).isEqualTo(Decimal.valueOf(1.5))
    }

    @Test
    fun whenMultipleEntriesWithAmountShouldCalculateEntriesValue() {
        newTrade.enter(2, 1.5, 1.1)
        newTrade.enter(3, 1.4, 0.8)
        newTrade.enter(3, 1.7, 0.9)
        assertThat(newTrade.getEntriesValue().toDouble()).isEqualTo(1.5 * 1.1 + 1.4 * 0.8 + 1.7 * 0.9)
    }

    @Test
    fun whenSingleEntryAndExitWithoutAmountShouldCalculateExitsValue() {
        newTrade.enter(1, 1.0)
        newTrade.exit(2, 1.1)
        assertThat(newTrade.getExitsValue().toDouble()).isEqualTo(1.1)
    }

    @Test
    fun whenMultipleEntriesAndExitsWithoutAmountShouldCalculateExitsValue() {
        newTrade.enter(1, 1.0)
        newTrade.enter(2, 0.9)
        newTrade.enter(3, 1.15)
        newTrade.exit(4, 1.2)
        newTrade.exit(5, 1.25)
        assertThat(newTrade.getExitsValue().toDouble()).isEqualTo(1.2 * 0.5 + 1.25 * 0.5)
    }

    @Test
    fun whenMultipleEntriesWithoutAmountShouldCalculateEntriesValue() {
        val oneThird = 1.0 / 3
        newTrade.enter(1, 1.0)
        newTrade.enter(2, 0.9)
        newTrade.enter(3, 1.15)
        assertThat(newTrade.getEntriesValue().toDouble()).isEqualTo(1.0 * oneThird + 0.9 * oneThird + 1.15 * oneThird)
    }

    @Test
    fun whenNewShouldCreateBuyOrderWhenEntering() {
        newTrade.enter(0)
        assertThat(newTrade.getFirstEntry()).isEqualTo(Order.buyAt(0))
    }

    @Test
    fun whenNewShouldNotBeOpened() {
        assertThat(newTrade.isOpened()).isFalse()
    }

    @Test
    fun whenOpenedShouldCreateSellOrderWhenExiting() {
        newTrade.enter(0)
        newTrade.exit(1)
        assertThat(newTrade.getLastExit()).isEqualTo(Order.sellAt(1))
    }

    @Test(expected = IllegalStateException::class)
    fun whenClosedShouldNotEnter() {
        newTrade.enter(0)
        newTrade.exit(1)
        assertThat(newTrade.isClosed()).isFalse()
        newTrade.close()
        newTrade.enter(2)
    }

    @Test(expected = IllegalStateException::class)
    fun whenExitIndexIsLessThanEntryIndexShouldThrowException() {
        newTrade.enter(3)
        newTrade.exit(1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun shouldThrowExceptionWhenOrdersHaveSameType() {
        Trade(Order.buyAt(0), Order.buyAt(1))
    }

    @Test
    fun whenNewShouldCreateSellOrderWhenEnteringUncovered() {
        uncoveredTrade.enter(0)
        assertThat(uncoveredTrade.getFirstEntry()).isEqualTo(Order.sellAt(0))
    }

    @Test
    fun whenOpenedShouldCreateBuyOrderWhenExitingUncovered() {
        uncoveredTrade.enter(0)
        uncoveredTrade.exit(1)
        assertThat(uncoveredTrade.getLastEntry()).isEqualTo(Order.sellAt(0))
        assertThat(uncoveredTrade.getLastExit()).isEqualTo(Order.buyAt(1))
    }

    @Test
    fun overrideToString() {
        assertThat(trEquals2.toString()).isEqualTo(trEquals1.toString())
        assertThat(trEquals1.toString()).isNotEqualTo(trNotEquals1.toString())
        assertThat(trEquals1.toString()).isNotEqualTo(trNotEquals2.toString())
    }
}
