package wikipedia

import org.scalatest.{FunSuite, BeforeAndAfterAll}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._

@RunWith(classOf[JUnitRunner])
class WikipediaSuite extends FunSuite with BeforeAndAfterAll {

  def initializeWikipediaRanking(): Boolean =
    try {
      WikipediaRanking
      true
    } catch {
      case ex: Throwable =>
        println(ex.getMessage)
        ex.printStackTrace()
        false
    }

  override def afterAll(): Unit = {
    assert(initializeWikipediaRanking(), " -- did you fill in all the values in WikipediaRanking (conf, sc, wikiRdd)?")
    import WikipediaRanking._
    sc.stop()
  }

  // Conditions:
  // (1) the language stats contain the same elements
  // (2) they are ordered (and the order doesn't matter if there are several languages with the same count)
  def assertEquivalentAndOrdered(given: List[(String, Int)], expected: List[(String, Int)]): Unit = {
    // (1)
    assert(given.toSet == expected.toSet, "The given elements are not the same as the expected elements")
    // (2)
    assert(
      !(given zip given.tail).exists({ case ((_, occ1), (_, occ2)) => occ1 < occ2 }),
      "The given elements are not in descending order"
    )
  }

  test("'occurrencesOfLang' should work for (specific) RDD with one element") {
    assert(initializeWikipediaRanking(), " -- did you fill in all the values in WikipediaRanking (conf, sc, wikiRdd)?")
    import WikipediaRanking._
    val rdd = sc.parallelize(Seq(WikipediaArticle("title", "Java Jakarta")))
    val res = (occurrencesOfLang("Java", rdd) == 1)
    assert(res, "occurrencesOfLang given (specific) RDD with one element should equal to 1")
  }

  test("'occurrencesOfLang' should not work for (specific) RDD with one element") {
    assert(initializeWikipediaRanking(), " -- did you fill in all the values in WikipediaRanking (conf, sc, wikiRdd)?")
    import WikipediaRanking._
    val rdd = sc.parallelize(Seq(WikipediaArticle("title", "DoJava Jakarta")))
    val res = (occurrencesOfLang("Java", rdd) != 1)
    assert(res, "occurrencesOfLang given (specific) RDD with one element should equal to 1")
  }

  test("'rankLangs' should work for RDD with two elements") {
    assert(initializeWikipediaRanking(), " -- did you fill in all the values in WikipediaRanking (conf, sc, wikiRdd)?")
    import WikipediaRanking._
    val langs = List("Scala", "Java")
    val rdd = sc.parallelize(List(WikipediaArticle("1", "Scala is great"), WikipediaArticle("2", "Java is OK, but Scala is cooler")))
    val ranked = rankLangs(langs, rdd)
    val res = ranked.head._1 == "Scala"
    assert(res)
  }
}
