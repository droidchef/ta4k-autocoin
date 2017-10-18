package eu.verdelhan.ta4j

import eu.verdelhan.ta4j.Order.OrderType.BUY
import java.io.Serializable
import java.util.*

/**
 * Set of BUY (or SELL) {@link Order} orders followed by complementary SELL (or BUY) orders.
 * <p>
 * The exits order has the complement type of the entries order.<br>
 * I.e.:
 *   entries == BUY, BUY... --> exits == SELL, SELL...
 *   entries == SELL, SELL... --> exits == BUY, BUY...
 */
class Trade(val startingType: Order.OrderType = BUY) : Serializable {

    private val entries: MutableList<Order> = ArrayList()
    private val exits: MutableList<Order> = ArrayList()

    private var isClosed = false

    /**
     * Constructor.
     * @param entry the entry {@link Order order}
     * @param exit the exit {@link Order order}
     */
    constructor(entry: Order, exit: Order) : this(listOf(entry), listOf(exit))

    constructor(entries: List<Order>, exits: List<Order>) : this(entries.first().type) {
        if (entries.isEmpty()) throw IllegalArgumentException("Entry orders must not be empty")
        if (exits.isEmpty()) throw IllegalArgumentException("Exit orders must not be empty")
        if (entries.distinctBy { it.type }.count() > 1) throw IllegalArgumentException("Entry orders must have same types")
        if (exits.distinctBy { it.type }.count() > 1) throw IllegalArgumentException("Exit orders must have same types")
        if (entries.first().type === exits.first().type) throw IllegalArgumentException("Entry and exit orders must have different starting types")
        this.entries.addAll(entries)
        this.exits.addAll(exits)
    }

    /**
     * @return entries {@link Order order} of the trade
     */
    fun getEntries(): List<Order> = Collections.unmodifiableList(entries)

    /**
     * @return exits {@link Order order} of the trade
     */
    fun getExits(): List<Order> = Collections.unmodifiableList(exits)

    override fun equals(other: Any?): Boolean {
        return if (other is Trade) entries == other.entries && exits == other.exits
        else false
    }

    override fun hashCode() = Objects.hash(entries, exits)

    fun enter(index: Int): Order = enter(index, Decimal.NaN, Decimal.NaN)

    fun enter(index: Int, price: Double): Order = enter(index, Decimal.valueOf(price), Decimal.NaN)

    fun enter(index: Int, price: Double, amount: Double): Order = enter(index, Decimal.valueOf(price), Decimal.valueOf(amount))

    fun enter(index: Int, price: Decimal): Order = enter(index, price, Decimal.NaN)

    fun enter(index: Int, price: Decimal, amount: Decimal): Order {
        if (index < entries.lastOrNull()?.index ?: 0) throw IllegalStateException("The index i is less than the entryOrder index")
        if (exits.isNotEmpty()) throw IllegalStateException("Cannot enter when there is exit")
        val order = Order(index, startingType, price, amount)
        entries.add(order)
        return order
    }

    fun exit(index: Int): Order = exit(index, Decimal.NaN, Decimal.NaN)

    fun exit(index: Int, price: Double): Order = exit(index, Decimal.valueOf(price), Decimal.NaN)

    fun exit(index: Int, price: Double, amount: Double): Order = exit(index, Decimal.valueOf(price), Decimal.valueOf(amount))

    fun exit(index: Int, price: Decimal, amount: Decimal): Order {
        if (index < exits.lastOrNull()?.index ?: 0) throw IllegalStateException("The index i is less than last exit order index")
        if (entries.isEmpty()) throw IllegalStateException("Cannot exit when there is no enter order")
        if (index < entries.lastOrNull()?.index ?:0) throw IllegalStateException("The index i is less than last  entry order index")
        val order = Order(index, startingType.complementType(), price, amount)
        exits.add(order)
        return order
    }

    fun isOpened() = entries.isNotEmpty() && exits.isEmpty()

    fun canExit() = entries.isNotEmpty()

    fun close(): Trade {
        if (isClosed) throw IllegalStateException("Cannot close already closed trade")
        isClosed = true
        return this
    }

    fun isClosed() = isClosed

    /**
     * @return true if the trade is new, false otherwise
     */
    fun isNew() = entries.isEmpty() && exits.isEmpty()

    fun canBeClosed() = entries.isNotEmpty() && exits.isNotEmpty()

    override fun toString() = "Entries: $entries, exits: $exits"

    fun getLastOrder(): Order? {
        return if (entries.isNotEmpty()) {
            if (exits.isNotEmpty()) exits.last()
            else entries.last()
        } else null
    }

    fun getLastOrder(orderType: Order.OrderType): Order? {
        return if (entries.isNotEmpty()) {
            if (entries.first().isBuy && orderType == BUY) entries.last()
            else exits.lastOrNull()
        } else null
    }

    fun getLastEntry() = entries.lastOrNull()

    fun getLastExit() = exits.lastOrNull()

    fun getEntriesValue(): Decimal {
        if (isNew()) throw IllegalStateException("Cannot calculate entries value when no entries")
        verifyAllEntryOrdersHavePrice()

        return ordersValue(entries)
    }

    fun getEntryIndexes(): List<Int> = entries.map { it.index }

    fun getExitIndexes(): List<Int> = exits.map { it.index }

    private fun ordersValue(orders: List<Order>): Decimal {
        return if (orders.first().amount === Decimal.NaN) {
            val equalAmountPerOrder = Decimal.ONE.dividedBy(Decimal.valueOf(orders.size))
            var value = Decimal.ZERO
            orders.forEach { value += it.price.multipliedBy(equalAmountPerOrder) }
            value
        } else {
            var value = Decimal.ZERO
            orders.forEach { value += it.price.multipliedBy(it.amount) }
            value
        }
    }

    fun getExitsValue(): Decimal {
        if (!canBeClosed()) throw IllegalStateException("Cannot calculate exits value when no entries and exits")
        verifyAllExitOrdersHavePrice()

        return ordersValue(exits)
    }

    private fun verifyAllExitOrdersHavePrice() {
        if (exits.any { it.price === Decimal.NaN }) throw IllegalStateException("Cannot calculate exit orders value when there is no price set")
    }

    private fun verifyAllEntryOrdersHavePrice() {
        if (entries.any { it.price === Decimal.NaN }) throw IllegalStateException("Cannot calculate exit orders value when there is no price set")
    }

    fun hasPrices() = canBeClosed() && entries.first().price !== Decimal.NaN && exits.last().price !== Decimal.NaN

    fun hasAmounts() = entries.none{ it.amount === Decimal.NaN } && exits.none{ it.amount === Decimal.NaN }

    fun entryIsBuy() = startingType === BUY

    fun getFirstEntryIndex() = entries.first().index

    fun getLastExitIndex() = exits.last().index

    fun getFirstEntry() = entries.first()

}
