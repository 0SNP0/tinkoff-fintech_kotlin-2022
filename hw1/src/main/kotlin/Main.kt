import java.time.LocalDate
import java.time.temporal.ChronoUnit.DAYS
import java.util.*

abstract class Security(val isin: String) {
    var value: Double = 0.0
        private set
    open val price: Double get() = updatePrice()


    fun updatePrice(): Double {
        // get actual price
        value = setPrice() ?: value
        return value
    }

    private fun setPrice(): Double? {
        print("\tset actual price of $isin: ")
        return readln().toDoubleOrNull()
    }

    override fun toString() = "$isin : $price"
}

class Stock(
    val ticker: String, isin: String, private val dividends: SortedMap<LocalDate, Double> = sortedMapOf()
) : Security(isin) {

    fun addDividends(date: LocalDate, value: Double) {
        dividends[date] = value
    }

    fun nextDividents(): Map<LocalDate, Double> {
        val currentDate = LocalDate.now()
        return dividends.filter { (date, _) -> date.isAfter(currentDate) }
    }

    override fun toString() = "$ticker : $price"
}

class Bond(
    isin: String,
    val faceValue: Double,
    val term: Pair<LocalDate, LocalDate>,
    val coupons: SortedMap<LocalDate, Double> = sortedMapOf()
) : Security(isin) {

    override val price: Double get() = updatePrice() + accumulatedCouponIncome()

    private fun accumulatedCouponIncome(): Double {
        val currentDate = LocalDate.now()
        var prevDate = term.first
        var nextDate = term.second
        var value = 0.0

        for ((date, coupon) in coupons) {
            if (date.isAfter(currentDate)) {
                nextDate = date
                value = coupon
                break;
            }
            prevDate = date
        }

        return value * DAYS.between(prevDate, currentDate) / DAYS.between(prevDate, nextDate)
    }
}

class Position(val security: Security) {
    private var count: Int = 0
    private var openingPrice: Double = 0.0

    val income get() = security.value * count - openingPrice
    val percentResult get() = income / openingPrice * 100

    fun buy(number: Int) {
        count += number
        openingPrice += security.price * number
    }

    fun sell(number: Int) {
        count -= number
        openingPrice -= security.price * number
    }

    override fun toString() = "$security * $count <+> $income | $percentResult%"
}

fun main() {
    val stock = Stock(
        "CHMF", "RU0009046510", sortedMapOf(
            // ...
            LocalDate.of(2021, 12, 28) to 85.93
        )
    )
    val bond = Bond(
        "RU000A0JSMA2", faceValue = 1000.0,
        LocalDate.of(2012, 8, 1) to LocalDate.of(2022, 7, 20), sortedMapOf(
            // ...
            LocalDate.of(2022, 1, 19) to 37.90,
            LocalDate.of(2022, 7, 20) to 37.90
        )
    )
    listOf(stock, bond).forEach { println(it) }

    stock.addDividends(LocalDate.of(2022, 6, 15), 109.81)
    println("Next ${stock.ticker} dividends: ${stock.nextDividents()}")
    println("Face value of ${bond.isin} is ${bond.faceValue}")
    println("Bond maturity date is ${bond.term.second}")

    val pos = Position(Stock("GMKN", "RU0007288411"))
    pos.buy(1)
    pos.sell(3)
    pos.buy(5)
    println(pos)
}