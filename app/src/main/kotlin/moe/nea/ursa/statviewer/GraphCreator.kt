package moe.nea.ursa.statviewer

import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartUtils
import org.jfree.chart.JFreeChart
import org.jfree.chart.axis.DateAxis
import org.jfree.data.time.FixedMillisecond
import org.jfree.data.time.TimeSeries
import org.jfree.data.time.TimeSeriesCollection
import org.jfree.data.time.TimeSeriesDataItem
import java.awt.Color
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.util.*
import kotlin.time.DurationUnit
import kotlin.time.toKotlinDuration

object GraphCreator {

    fun createGraphFromPoints(title: String, points: List<Pair<Instant, Double>>, yAxis: String): JFreeChart {
        val chart = ChartFactory.createTimeSeriesChart(
            title,
            "Time",
            yAxis,
            TimeSeriesCollection(TimeSeries(title).also { series ->
                points.forEach { (time, value) ->
                    series.add(TimeSeriesDataItem(FixedMillisecond(time.toEpochMilli()), value))
                }
            }, TimeZone.getTimeZone(ZoneOffset.UTC)),
        )
        val plot = chart.xyPlot
        plot.backgroundPaint = Color.DARK_GRAY
        plot.domainGridlinePaint = Color.white
        plot.rangeGridlinePaint = Color.white
        val dateAxis = plot.domainAxis as DateAxis
        dateAxis.dateFormatOverride = SimpleDateFormat("dd.MM HH:mm")
        return chart
    }

    fun renderGraph(chart: JFreeChart): ByteArray {
        val baos = ByteArrayOutputStream()
        ChartUtils.writeChartAsPNG(baos, chart, 1920, 1080)
        return baos.toByteArray()
    }

    fun calculateGraphDeltas(dataPoints: List<Pair<Instant, Double>>): List<Pair<Instant, Double>> {
        return dataPoints.zipWithNext().map { (last, next) ->
            val δv = next.second - last.second
            val δt = Duration.between(last.first, next.first).toKotlinDuration().toDouble(DurationUnit.MINUTES)
            val perMinute = δv / δt
            next.first to perMinute
        }
    }

    fun queryGraphPoints(key: String, since: Instant): List<Pair<Instant, Double>> {
        val connection = Util.getConnection()
        val preparedStatement =
            connection.prepareStatement("SELECT  value, timestamp from metrics where key = ? and timestamp > ? order by timestamp desc")
        preparedStatement.setString(1, key)
        preparedStatement.setLong(2, since.toEpochMilli())
        preparedStatement.executeQuery().use {
            return buildList {
                while (it.next()) {
                    val value = it.getLong(1)
                    val timestamp = Instant.ofEpochMilli(it.getLong(2))
                    add(timestamp to value.toDouble())
                }
            }
        }
    }
}