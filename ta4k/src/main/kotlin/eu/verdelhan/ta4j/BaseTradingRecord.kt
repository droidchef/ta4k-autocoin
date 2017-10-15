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

    /** The recorded orders */
    private val orders = ArrayList<Order>()

    /** The recorded BUY orders */
    private val buyOrders = ArrayList<Order>()

    /** The recorded SELL orders */
    private val sellOrders = ArrayList<Order>()

    /** The recorded entry orders */
    private val entryOrders = ArrayList<Order>()

    /** The recorded exit orders */
    private val exitOrders = ArrayList<Order>()

    /** The recorded trades */
    private val trades = ArrayList<Trade>()

    /** The entry type (BUY or SELL) in the trading session */
    private val startingType: OrderType

    /** The current non-closed trade (there's always one) */
    @get:JvmName("getCurrentTrade_")
    var currentTrade: Trade

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
        orders.forEach {
            val newOrderWillBeAnEntry = currentTrade.isNew()
            if (newOrderWillBeAnEntry && it.type != startingType) {
                // Special case for entry/exit types reversal
                // E.g.: BUY, SELL,
                // BUY, SELL,
                // SELL, BUY,
                // BUY, SELL
                currentTrade = Trade(it.type)
            }
            val newOrder = currentTrade.operate(it.index, it.price, it.amount)
            recordOrder(newOrder, newOrderWillBeAnEntry)
        }
    }

    override fun getCurrentTrade() = currentTrade

    override fun operate(index: Int, price: Decimal, amount: Decimal) {
        if (currentTrade.isClosed()) {
            // Current trade closed, should not occur
            throw IllegalStateException("Current trade should not be closed")
        }
        val newOrderWillBeAnEntry = currentTrade.isNew()
        val newOrder = currentTrade.operate(index, price, amount)
        recordOrder(newOrder, newOrderWillBeAnEntry)
    }

    override fun enter(index: Int, price: Decimal, amount: Decimal): Boolean {
        return if (currentTrade.isNew()) {
            operate(index, price, amount)
            true
        } else false
    }

    override fun exit(index: Int, price: Decimal, amount: Decimal): Boolean {
        return if (currentTrade.isOpened()) {
            operate(index, price, amount)
            true
        } else false
    }

    override fun getTrades() = trades

    override fun getLastOrder() = orders.lastOrNull()

    override fun getLastOrder(orderType: OrderType): Order? = when {
        orderType == OrderType.BUY && buyOrders.isNotEmpty() ->  buyOrders[buyOrders.size - 1]
        orderType == OrderType.SELL && sellOrders.isNotEmpty() -> sellOrders[sellOrders.size - 1]
        else -> null
    }

    override fun getLastEntry() = entryOrders.lastOrNull()

    override fun getLastExit() = exitOrders.lastOrNull()

    /**
     * Records an order and the corresponding trade (if closed).
     * @param order the order to be recorded
     * @param isEntry true if the order is an entry, false otherwise (exit)
     */
    private fun recordOrder(order: Order, isEntry: Boolean) {
        // Storing the new order in entries/exits lists
        if (isEntry) entryOrders.add(order)
        else exitOrders.add(order)

        // Storing the new order in orders list
        orders.add(order)
        if (order.type == OrderType.BUY) {
            // Storing the new order in buy orders list
            buyOrders.add(order)
        } else if (order.type == OrderType.SELL) {
            // Storing the new order in sell orders list
            sellOrders.add(order)
        }
        // Storing the trade if closed
        if (currentTrade.isClosed()) {
            trades.add(currentTrade)
            currentTrade = Trade(startingType)
        }
    }

    companion object {
        private val serialVersionUID = -4436851731855891220L
    }
}
