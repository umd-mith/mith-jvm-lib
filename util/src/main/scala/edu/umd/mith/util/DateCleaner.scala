package edu.umd.mith.util

class DateCleaner {
  def parseYearField(value: String): Option[(Int, Option[Int])] = {
    val AbbrevYearRange = """.*(\d{4})\-(\d{2}).*""".r
    val YearRange = """.*(\d{4}).*(\d{4}).*""".r
    val Year = """.*(\d{4}).*""".r
    val Decade = """.*(\d{3})\-.*""".r
    val Century = """.*(\d{2})\-.*""".r
    value.replaceAll("""[\.\[\], ]""", "") match {
      case AbbrevYearRange(start, end) =>
        Some((start.toInt, Some((start.substring(0, 2) + end).toInt)))
      case YearRange(start, end) => Some((start.toInt, Some(end.toInt)))
      case Year(start) => Some((start.toInt, None))
      case Decade(known) => {
        val decade = known.toInt
        Some((decade * 10, Some(decade * 10 + 9)))
      }
      case Century(known) => {
        val century = known.toInt
        Some((century * 100, Some(century * 100 + 99)))
      }
      case RomanNumeral(start) => Some((start, None))
      case _ => None 
    }
  }
}

