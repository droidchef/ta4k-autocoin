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

import eu.verdelhan.ta4j.Order.OrderType
import java.io.Serializable

/**
 * A history/record of a trading session.
 * <p>
 * Holds the full trading record when running a {@link Strategy strategy}.
 * It is used to:
 * <ul>
 * <li>check to satisfaction of some trading rules (when running a strategy)
 * <li>analyze the performance of a trading strategy
 * </ul>
 */
interface TradingRecord : Serializable {

    fun getCurrentTrade(): Trade

    /**
     * @return true if no trade is open, false otherwise
     */
    fun isClosed() = !getCurrentTrade().isOpened()

    /**
     * @return the recorded trades
     */
    fun getTrades(): List<Trade>  // TODO rename to getClosedTrades

    /**
     * @return the number of recorded trades
     */
    fun getTradeCount() = getTrades().size // TODO rename to getClosedTradeCount

    /**
     * @return the last trade recorded
     */
    fun getLastTrade() = getTrades().lastOrNull() // TODO rename to getLastClosedTrade

    /**
     * @return the last order recorded
     */
    fun getLastOrder(): Order?

    /**
     * @return the last entry order recorded
     */
    fun getLastEntry(): Order?

    /**
     * @return the last exit order recorded
     */
    fun getLastExit(): Order?

    /**
     * Creates enter order in the trading record.
     * Closes current trade when entry begins new order type sequence, eg
     * BUY, BUY.., SELL, SELL.. -> BUY
     * SELL, SELL.., BUY, BUY.. -> SELL
     * @param index the index to operate the entry
     */
    fun enter(index: Int) = enter(index, Decimal.NaN, Decimal.NaN)

    /**
     * Creates enter order in the trading record.
     * Closes current trade when entry begins new order type sequence, eg
     * BUY, BUY.., SELL, SELL.. -> BUY
     * SELL, SELL.., BUY, BUY.. -> SELL
     * @param index the index to operate the entry
     * @param price the price of the order
     * @param amount the amount to be ordered
     */
    fun enter(index: Int, price: Decimal, amount: Decimal): Boolean

    /**
     * Creates exit order in the trading record.
     * Closes current trade when exit begins new order type sequence, eg
     * BUY, BUY.., SELL, SELL.. -> BUY
     * SELL, SELL.., BUY, BUY.. -> SELL
     * @param index the index to operate the exit
     */
    fun exit(index: Int) = exit(index, Decimal.NaN, Decimal.NaN)

    /**
     * Creates exit order in the trading record.
     * Closes current trade when exit begins new order type sequence, eg
     * BUY, BUY.., SELL, SELL.. -> BUY
     * SELL, SELL.., BUY, BUY.. -> SELL
     * @param index the index to operate the exit
     * @param price the price of the order
     * @param amount the amount to be ordered
     */
    fun exit(index: Int, price: Decimal, amount: Decimal): Boolean

    /**
     * @param orderType the type of the order to get the last of
     * @return the last order (of the provided type) recorded
     */
    fun getLastOrder(orderType: OrderType): Order?

    /**
     * Mark current trade as closed, store current and create new trade.
     * Closing new trade (without orders) throws exception
     */
    fun closeCurrent(): TradingRecord

}
