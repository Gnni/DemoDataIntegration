package model.datagen.util

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import scala.util.Random

object Helper {
	def getTSInHanaSeconddateFormat: String = {
		val df: DateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S")
		val today: Date = Calendar.getInstance.getTime
		return df.format(today)
	}

	def getTSInHanaSeconddateFormat(date: Date): String = {
		val df: DateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S")
		return df.format(date)
	}

	def getRandomDateInHanaSeconddateFormat: String = {
		val gc: Calendar = Calendar.getInstance()
		val year: Int = randBetween(1900, 2016)
		gc.set(Calendar.YEAR, year)
		val dayOfYear: Int = randBetween(1, gc.getActualMaximum(Calendar.DAY_OF_YEAR))
		gc.set(Calendar.DAY_OF_YEAR, dayOfYear)
		return gc.get(Calendar.YEAR) + "-" + (gc.get(Calendar.MONTH) + 1) + "-" + gc.get(Calendar.DAY_OF_MONTH) + " 01:02:03"
	}

	def getRandomDateInFutureInHanaSeconddateFormat: String = {
		val gc: Calendar = Calendar.getInstance()
		val year: Int = randBetween(2016, 2017)
		gc.set(Calendar.YEAR, year)
		var dayOfYear: Int = 0
		if (year == 2016) {
			dayOfYear = randBetween(Calendar.getInstance.get(Calendar.DAY_OF_YEAR) + 1, gc.getActualMaximum(Calendar.DAY_OF_YEAR))
		}
		else {
			dayOfYear = randBetween(1, gc.getActualMaximum(Calendar.DAY_OF_YEAR))
		}
		gc.set(Calendar.DAY_OF_YEAR, dayOfYear)
		return gc.get(Calendar.YEAR) + "-" + (gc.get(Calendar.MONTH) + 1) + "-" + gc.get(Calendar.DAY_OF_MONTH) + " 11:22:33"
	}

	def randBetween(min: Int, max: Int): Int = {
		val r: Random = Random
		r.nextInt(max-min) + min
	}

	def randBetween(start: Long, end: Long): Int = {
		randBetween(start.toInt, end.toInt)
	}

	def doubleRandBetween(min: Int, max: Int): Double = {
		val r: Random = Random
		return (min + (max - min) * r.nextDouble)
	}

	def doubleRandBetween(min: Double, max: Double): Double = {
		val r: Random = Random
		return (min + (max - min) * r.nextDouble)
	}
}