package edu.umd.mith.util

object RomanNumeral {
  private val numerals = List(
    ("M", 1000), ("CM", 900), ("D",  500), ("CD", 400), ("C",  100),
    ("XC",  90), ("L",   50), ("XL",  40), ("X",   10), ("IX",   9),
    ("V",    5), ("IV",   4), ("I",    1)
  )

  def unapply(s: String): Option[Int] = s.toUpperCase match {
    case "" => Some(0)
    case s: String => {
      numerals.filter { case (n, _) => s.startsWith(n) } match {
        case (n: String, i: Int) :: _ =>
          RomanNumeral.unapply(s.substring(n.length)).map(_ + i)
        case Nil => None
      }
    }
  }
}

