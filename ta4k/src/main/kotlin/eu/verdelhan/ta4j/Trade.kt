package eu.verdelhan.ta4j

import eu.verdelhan.ta4j.Order.OrderType
import java.io.Serializable
import java.util.*

/**
 * Set of BUY (or SELL) {@link Order orders} followed by complementary SELL (or BUY) orders.
 * <p>
 * The exits order has the complement type of the entries order.<br>
 * I.e.:
 *   entries == BUY, BUY... --> exits == SELL, SELL...
 *   entries == SELL, SELL... --> exits == BUY, BUY...
 */
class Trade(private val startingType: Order.OrderType = Order.OrderType.BUY): Serializable {

    private val entries: MutableList<Order> = ArrayList()
    private val exits: MutableList<Order> = ArrayList()

    /**
     * Constructor.
     * @param entry the entry {@link Order order}
     * @param exit the exit {@link Order order}
     */
    constructor(entry: Order, exit: Order): this(entry.type) {
        if (entry.type == exit.type) throw IllegalArgumentException("Both orders must have different types")
        entries.add(entry)
        exits.add(exit)
    }

    /**
     * @return the first entry {@link Order order} of the trade
     */
    fun getEntry(): Order? = entries.firstOrNull()

    /**
     * @return the first exit {@link Order order} of the trade
     */
    fun getExit(): Order? = exits.firstOrNull()

    override fun equals(t: Any?): Boolean {
        return if (t is Trade) entries == t.entries && exits == t.exits
        else false
    }

    override fun hashCode() = Objects.hash(entries, exits)

    fun operate(index: Int): Order = operate(index, Decimal.NaN, Decimal.NaN)

    /**
     * Operates the trade at the index-th position
     * @param index the tick index
     * @param price the price
     * @param amount the amount
     * @return the order
     */
    fun operate(index: Int, price: Decimal, amount: Decimal): Order {
        var order = Order(index, startingType, price, amount)
        if (isNew()) {
            entries.add(order)
        } else if (isOpened()) {
            if (index < entries.first().index) throw IllegalStateException("The index i is less than the entryOrder index")
            order = Order(index, startingType.complementType(), price, amount)
            exits.add(order)
        }
        return order
    }

    fun isClosed() = entries.isNotEmpty() && exits.isNotEmpty()

    fun isOpened() = entries.isNotEmpty() && exits.isEmpty()

    /**
     * @return true if the trade is new, false otherwise
     */
    fun isNew() = entries.isEmpty() && exits.isEmpty()

    override fun toString() = "Entries: $entries, exits: $exits"

}
