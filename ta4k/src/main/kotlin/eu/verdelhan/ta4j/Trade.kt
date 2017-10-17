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
class Trade(private val startingType: Order.OrderType = BUY) : Serializable {

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
        if (entries.first() == exits.first()) throw IllegalArgumentException("Entry and exit orders must have different types")
        this.entries.addAll(entries)
        this.exits.addAll(exits)
    }

    /**
     * @return the first entry {@link Order order} of the trade
     */
    fun getEntry(): Order? = entries.firstOrNull()

    /**
     * @return the first exit {@link Order order} of the trade
     */
    fun getExit(): Order? = exits.firstOrNull()

    override fun equals(other: Any?): Boolean {
        return if (other is Trade) entries == other.entries && exits == other.exits
        else false
    }

    override fun hashCode() = Objects.hash(entries, exits)

    fun enter(index: Int): Order = enter(index, Decimal.NaN, Decimal.NaN)

    fun enter(index: Int, price: Decimal, amount: Decimal): Order {
        if (index < entries.lastOrNull()?.index ?: 0) throw IllegalStateException("The index i is less than the entryOrder index")
        if (exits.isNotEmpty()) throw IllegalStateException("Cannot enter when there is exit")
        val order = Order(index, startingType, price, amount)
        entries.add(order)
        return order
    }

    fun exit(index: Int): Order = exit(index, Decimal.NaN, Decimal.NaN)

    fun exit(index: Int, price: Decimal, amount: Decimal): Order {
        if (index < exits.lastOrNull()?.index ?: 0) throw IllegalStateException("The index i is less than the entryOrder index")
        if (entries.isEmpty()) throw IllegalStateException("Cannot exit when there is no enter order")
        val order = Order(index, startingType.complementType(), price, amount)
        exits.add(order)
        return order
    }

    fun isOpened() = entries.isNotEmpty() && exits.isEmpty()

    fun close() {
        if (isClosed) throw IllegalStateException("Cannot close already closed trade")
        isClosed = true
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

    fun getExitsValue(): Decimal {
        if (!canBeClosed()) throw IllegalStateException("Cannot calculate exits value when no entries and exits")
        return Decimal.NaN
    }

}
