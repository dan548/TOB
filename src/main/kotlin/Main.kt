package ge.sibraine

import java.util.*


data class Order(
    val action: Char,
    val instrumentId: Int,
    val side: Char,
    val price: Long,
    val amount: Int
)

data class Offer(
    val instrumentId: Int,
    val side: Char,
    val price: Long,
    var amount: Int
) {
    constructor(offer: Offer) : this(offer.instrumentId, offer.side, offer.price, offer.amount)
    override fun toString(): String = "$instrumentId;$side;$price;$amount"
}

fun main() {
    val storage = mutableMapOf<Int, Pair<SortedMap<Long, Offer>, SortedMap<Long, Offer>>>()

    val addOfferFunc = { oldOffer: Offer, amount: Int -> oldOffer.amount += amount }
    val subtractOfferFunc: (Offer, Int) -> Boolean = { oldOffer, amount ->
        oldOffer.amount -= amount
        oldOffer.amount == 0
    }

    var lastRes: Offer? = null

    while (true) {
        val str = readln()
        if (str == "exit") break
        val inputData = str.split(";")
        val order = Order(
            action = inputData[2].single(),
            instrumentId = inputData[3].toInt(),
            side = inputData[4].single(),
            price = inputData[5].toLong(),
            amount = inputData[6].toInt()
        )

        var res: Offer? = null

        when(order.side) {
            'B' -> {
                val buyOfferMap = storage[order.instrumentId]?.first
                val maxOfferPrice = if (buyOfferMap.isNullOrEmpty()) 0L else buyOfferMap.lastKey()
                val instrumentBuySell = storage.putIfAbsent(order.instrumentId, Pair(sortedMapOf(), sortedMapOf()))

                val currentOffer = instrumentBuySell?.first?.get(order.price)
                if (currentOffer != null) {
                    when (order.action) {
                        '0' -> {
                            addOfferFunc(currentOffer, order.amount)
                            if (maxOfferPrice == currentOffer.price) res = currentOffer
                        }
                        '1', '2' -> if (subtractOfferFunc(currentOffer, order.amount)) {
                            instrumentBuySell.first.remove(order.price)
                            if (maxOfferPrice == currentOffer.price) res =
                                if (instrumentBuySell.first.isNotEmpty()) instrumentBuySell.first.lastEntry().value else Offer(
                                    instrumentId = order.instrumentId,
                                    side = order.side,
                                    price = 0L,
                                    amount = 0
                                )
                        } else {
                            if (maxOfferPrice == currentOffer.price) res = currentOffer
                        }
                    }
                } else {
                    val newOffer = Offer(
                        instrumentId = order.instrumentId,
                        side = order.side,
                        price = order.price,
                        amount = order.amount
                    )
                    storage[order.instrumentId]!!.first[order.price] = newOffer
                    if (maxOfferPrice < newOffer.price) res = newOffer
                }
            }
            'S' -> {
                val sellOfferMap = storage[order.instrumentId]?.second
                val minOfferPrice = if (sellOfferMap.isNullOrEmpty()) 999999999999999999L else sellOfferMap.firstKey()
                val instrumentBuySell = storage.putIfAbsent(order.instrumentId, Pair(sortedMapOf(), sortedMapOf()))

                val currentOffer = instrumentBuySell?.second?.get(order.price)
                if (currentOffer != null) {
                    when (order.action) {
                        '0' -> {
                            addOfferFunc(currentOffer, order.amount)
                            if (minOfferPrice == currentOffer.price) res = currentOffer
                        }
                        '1', '2' -> if (subtractOfferFunc(currentOffer, order.amount)) {
                            instrumentBuySell.second.remove(order.price)
                            if (minOfferPrice != currentOffer.price) res =
                                if (instrumentBuySell.second.isNotEmpty()) instrumentBuySell.second.firstEntry().value else Offer(
                                    instrumentId = order.instrumentId,
                                    side = order.side,
                                    price = 999999999999999999L,
                                    amount = 0
                                )
                        } else {
                            if (minOfferPrice == currentOffer.price) res = currentOffer
                        }
                    }
                } else {
                    val newOffer = Offer(
                        instrumentId = order.instrumentId,
                        side = order.side,
                        price = order.price,
                        amount = order.amount
                    )
                    storage[order.instrumentId]!!.second[order.price] = newOffer
                    if (minOfferPrice > newOffer.price) res = newOffer
                }
            }
        }

        if (res == lastRes) {
            res = null
        } else {
            lastRes = res?.let { Offer(it) }
        }

        println(res ?: "")
    }
}