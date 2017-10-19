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
import java.util.*

/**
 * Base implementation of a {@link TradingRecord}.
 * <p>
 */
class BaseTradingRecord : TradingRecord {

    /** The recorded trades */
    override val trades = ArrayList<Trade>()

    /** The entry type (BUY or SELL) in the trading session */
    private val startingType: OrderType

    /** The current non-closed trade (there's always one) */
    override var currentTrade: Trade
    private set

    constructor() : this(OrderType.BUY)

    /**
     * @param entryOrderType the {@link OrderType order type} of entries in the trading session
     */
    constructor(entryOrderType: OrderType) {
        this.startingType = entryOrderType
        currentTrade = Trade(entryOrderType)
    }

    /**
     * @param orders the orders to be recorded (cannot be empty)
     */
    constructor(vararg orders: Order) : this(orders[0].type) {
        createTradesFrom(orders.asList())
    }

    constructor(vararg trades: Trade) : this(trades[0].startingType) {
        this.trades.addAll(trades.asList())
    }

    private fun createTradesFrom(orders: List<Order>) = orders.forEach { addOrder(it) }

    private fun addOrder(order: Order) {
        if (isEntry(order)) enter(order.index, order.price, order.amount)
        else exit(order.index, order.price, order.amount)
    }

    private fun isEntry(order: Order) = order.type == startingType

    override fun enter(index: Int, price: Decimal, amount: Decimal): Boolean {
        if (currentTrade.canBeClosed()) closeCurrent()
        currentTrade.enter(index, price, amount)
        return true
    }

    override fun exit(index: Int, price: Decimal, amount: Decimal): Boolean {
        if (currentTrade.isNew()) throw IllegalStateException("Exit can be performed only when there is at least one entry")
        currentTrade.exit(index, price, amount)
        return true
    }

    override fun getLastOrder() = currentTrade.getLastOrder() ?: trades.lastOrNull()?.getLastOrder()

    override fun getLastOrder(orderType: OrderType) = currentTrade.getLastOrder(orderType) ?: trades.lastOrNull()?.getLastOrder(orderType)

    override fun getLastEntry() = currentTrade.getLastEntry() ?: trades.lastOrNull()?.getLastEntry()

    override fun getLastExit() = currentTrade.getLastExit() ?: trades.lastOrNull()?.getLastExit()

    override fun closeCurrent(): BaseTradingRecord {
        if (currentTrade.canBeClosed()) {
            currentTrade.close()
            trades.add(currentTrade)
            currentTrade = Trade(startingType)
        } else throw IllegalStateException("Current trade cannot be closed")
        return this
    }

    companion object {
        private val serialVersionUID = -4436851731855891220L
    }
}
