package com.aleksa.domain

import kotlin.math.roundToLong

@JvmInline
value class Money private constructor(
    /**
     * minor currency unit. E.g. cents
     */
    val minor: Long
) : Comparable<Money> {

    operator fun plus(other: Money): Money = Money(minor + other.minor)
    operator fun minus(other: Money): Money = Money(minor - other.minor)
    operator fun unaryMinus(): Money = Money(-minor)

    operator fun times(multiplier: Int): Money = Money(minor * multiplier.toLong())
    operator fun times(multiplier: Long): Money = Money(minor * multiplier)

    operator fun div(divisor: Int): Money = div(divisor.toLong())
    operator fun div(divisor: Long): Money {
        require(divisor != 0L) { "Divisor cannot be zero" }
        return Money(minor / divisor)
    }

    fun isZero(): Boolean = minor == 0L
    fun isPositive(): Boolean = minor > 0L
    fun isNegative(): Boolean = minor < 0L
    fun abs(): Money = Money(kotlin.math.abs(minor))

    fun toDecimalString(decimals: Int = 2): String {
        require(decimals in 0..9) { "decimals must be 0..9" }
        if (decimals == 0) return minor.toString()

        val sign = if (minor < 0) "-" else ""
        val v = kotlin.math.abs(minor)
        val factor = pow10(decimals)

        val whole = v / factor
        val frac = v % factor

        return buildString {
            append(sign)
            append(whole)
            append('.')
            append(frac.toString().padStart(decimals, '0'))
        }
    }

    fun format(
        symbol: String = "EUR",
        decimals: Int = 2,
        symbolFirst: Boolean = false,
        groupThousands: Boolean = true
    ): String {
        val sign = if (minor < 0) "-" else ""
        val v = kotlin.math.abs(minor)
        val factor = pow10(decimals)

        val whole = v / factor
        val frac = v % factor

        val wholeStr = if (groupThousands) groupThousands(whole) else whole.toString()
        val amountStr = if (decimals == 0) {
            wholeStr
        } else {
            wholeStr + "." + frac.toString().padStart(decimals, '0')
        }

        val base = if (symbolFirst) "$symbol $amountStr" else "$amountStr $symbol"
        return sign + base
    }

    override fun compareTo(other: Money): Int = minor.compareTo(other.minor)

    companion object {
        fun ofMinor(minorUnits: Long): Money = Money(minorUnits)
        fun ofMajor(major: Long, decimals: Int = 2): Money =
            Money(major * pow10(decimals))

        fun ofDouble(amount: Double, decimals: Int = 2): Money {
            require(decimals in 0..9) { "decimals must be 0..9" }
            val factor = pow10(decimals).toDouble()
            return Money((amount * factor).roundToLong())
        }

        fun zero(): Money = Money(0L)

        private fun pow10(decimals: Int): Long {
            var r = 1L
            repeat(decimals) { r *= 10L }
            return r
        }

        private fun groupThousands(value: Long): String {
            val s = value.toString()
            if (s.length <= 3) return s
            val sb = StringBuilder(s.length + s.length / 3)
            var i = 0
            val firstGroup = s.length % 3
            val start = if (firstGroup == 0) 3 else firstGroup
            sb.append(s.substring(0, start))
            i = start
            while (i < s.length) {
                sb.append(',')
                sb.append(s.substring(i, i + 3))
                i += 3
            }
            return sb.toString()
        }
    }
}