package com.kenkeremath.vatroutbuddy.data.html

import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.Month

class StockingHtmlParserTest {
    @Test
    fun `parse correctly handles typical stocking entry`() {
        val html = """
            <table>
                <tr>
                    <th>Date</th>
                    <th>County</th>
                    <th>Waterbody</th>
                    <th>Category</th>
                    <th>Species Stocked</th>
                </tr>
                <tr>
                    <td class="date_stocked">February 21, 2025</td>
                    <td class="locality_name">Warren County</td>
                    <td class="waterbody_details">Happy Creek</td>
                    <td class="optional"><a href="#">B</a></td>
                    <td>
                        <ul class="species-stocked">
                            <li>Rainbow Trout</li>
                        </ul>
                    </td>
                </tr>
            </table>
        """.trimIndent()

        val doc = Jsoup.parse(html)
        val result = StockingHtmlParser.parse(doc)

        assertEquals(1, result.size)
        with(result[0]) {
            assertEquals(LocalDate.of(2025, Month.FEBRUARY, 21), date)
            assertEquals("Warren County", county)
            assertEquals("Happy Creek", waterbody)
            assertEquals("B", category)
            assertEquals(listOf("Rainbow Trout"), species)
            assertEquals(false, isNationalForest)
        }
    }

    @Test
    fun `parse correctly handles national forest water`() {
        val html = """
            <table>
                <tr><th>Date</th><th>County</th><th>Waterbody</th><th>Category</th><th>Species Stocked</th></tr>
                <tr>
                    <td>February 21, 2025</td>
                    <td>Bath County</td>
                    <td>Cowpasture River[National Forest Water]</td>
                    <td><a href="#">A</a></td>
                    <td><ul><li>Rainbow Trout</li></ul></td>
                </tr>
            </table>
        """.trimIndent()

        val doc = Jsoup.parse(html)
        val result = StockingHtmlParser.parse(doc)

        assertEquals(1, result.size)
        with(result[0]) {
            assertEquals("Cowpasture River", waterbody)
            assertEquals(true, isNationalForest)
        }
    }

    @Test
    fun `parse correctly handles multiple species`() {
        val html = """
            <table>
                <tr><th>Date</th><th>County</th><th>Waterbody</th><th>Category</th><th>Species Stocked</th></tr>
                <tr>
                    <td>February 20, 2025</td>
                    <td>City of Alexandria</td>
                    <td>Cook Lake</td>
                    <td><a href="#">U</a></td>
                    <td>
                        <ul>
                            <li>Rainbow Trout</li>
                            <li>Brown Trout</li>
                            <li>Tiger Trout</li>
                        </ul>
                    </td>
                </tr>
            </table>
        """.trimIndent()

        val doc = Jsoup.parse(html)
        val result = StockingHtmlParser.parse(doc)

        assertEquals(1, result.size)
        assertEquals(
            listOf("Rainbow Trout", "Brown Trout", "Tiger Trout"),
            result[0].species
        )
    }

    @Test
    fun `parse skips no stockings today entries`() {
        val html = """
            <table>
                <tr><th>Date</th><th>County</th><th>Waterbody</th><th>Category</th><th>Species Stocked</th></tr>
                <tr>
                    <td>February 19, 2025</td>
                    <td>Statewide</td>
                    <td>No Stockings Today</td>
                    <td><a href="#"></a></td>
                    <td></td>
                </tr>
            </table>
        """.trimIndent()

        val doc = Jsoup.parse(html)
        val result = StockingHtmlParser.parse(doc)

        assertEquals(0, result.size)
    }

    @Test
    fun `parse correctly handles multiple tags in waterbody name`() {
        val html = """
            <table>
                <tr><th>Date</th><th>County</th><th>Waterbody</th><th>Category</th><th>Species Stocked</th></tr>
                <tr>
                    <td>February 21, 2025</td>
                    <td>Bath County</td>
                    <td>South River [Delayed Harvest Water] [NSF] [Heritage Day Water]</td>
                    <td><a href="#">A</a></td>
                    <td><ul><li>Rainbow Trout</li></ul></td>
                </tr>
            </table>
        """.trimIndent()

        val doc = Jsoup.parse(html)
        val result = StockingHtmlParser.parse(doc)

        assertEquals(1, result.size)
        with(result[0]) {
            assertEquals("South River", waterbody)
            assertEquals(true, isDelayedHarvest)
            assertEquals(true, isNsf)
            assertEquals(true, isHeritageDayWater)
            assertEquals(false, isNationalForest)
        }
    }

    @Test
    fun `parse correctly handles whitespace in tags`() {
        val html = """
            <table>
                <tr><th>Date</th><th>County</th><th>Waterbody</th><th>Category</th><th>Species Stocked</th></tr>
                <tr>
                    <td>February 21, 2025</td>
                    <td>Bath County</td>
                    <td>Lake Test  [NSF]  [Heritage Day Water]   </td>
                    <td><a href="#">A</a></td>
                    <td><ul><li>Rainbow Trout</li></ul></td>
                </tr>
            </table>
        """.trimIndent()

        val doc = Jsoup.parse(html)
        val result = StockingHtmlParser.parse(doc)

        assertEquals(1, result.size)
        with(result[0]) {
            assertEquals("Lake Test", waterbody)
            assertEquals(true, isNsf)
            assertEquals(true, isHeritageDayWater)
            assertEquals(false, isDelayedHarvest)
            assertEquals(false, isNationalForest)
        }
    }

    @Test
    fun `parse correctly handles case insensitive tags`() {
        val html = """
            <table>
                <tr><th>Date</th><th>County</th><th>Waterbody</th><th>Category</th><th>Species Stocked</th></tr>
                <tr>
                    <td>February 21, 2025</td>
                    <td>Bath County</td>
                    <td>Lake Test [nsf] [HERITAGE DAY WATER]</td>
                    <td><a href="#">A</a></td>
                    <td><ul><li>Rainbow Trout</li></ul></td>
                </tr>
            </table>
        """.trimIndent()

        val doc = Jsoup.parse(html)
        val result = StockingHtmlParser.parse(doc)

        assertEquals(1, result.size)
        with(result[0]) {
            assertEquals("Lake Test", waterbody)
            assertEquals(true, isNsf)
            assertEquals(true, isHeritageDayWater)
        }
    }
} 