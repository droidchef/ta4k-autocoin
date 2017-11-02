/**
 * The MIT License (MIT)
 *
 *
 * Copyright (c) 2014-2017 Marc de Verdelhan & respective authors (see AUTHORS)
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ta4jexamples.analysis

import eu.verdelhan.ta4j.*
import eu.verdelhan.ta4j.indicators.helpers.ClosePriceIndicator
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartPanel
import org.jfree.chart.ChartUtilities
import org.jfree.chart.JFreeChart
import org.jfree.chart.axis.DateAxis
import org.jfree.chart.plot.Marker
import org.jfree.chart.plot.ValueMarker
import org.jfree.chart.plot.XYPlot
import org.jfree.data.time.Minute
import org.jfree.data.time.TimeSeriesCollection
import org.jfree.ui.ApplicationFrame
import org.jfree.ui.RefineryUtilities
import ta4jexamples.loaders.CsvTradesLoader
import ta4jexamples.strategies.MovingMomentumStrategy

import java.awt.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

/**
 * This class builds a graphical chart showing the buy/sell signals of a strategy.
 */
object BuyAndSellSignalsToChart {

    /**
     * Builds a JFreeChart time series from a Ta4j time series and an indicator.
     *
     * @param tickSeries the ta4j time series
     * @param indicator  the indicator
     * @param name       the name of the chart time series
     * @return the JFreeChart time series
     */
    private fun buildChartTimeSeries(tickSeries: TimeSeries, indicator: Indicator<Decimal>, name: String): org.jfree.data.time.TimeSeries {
        val chartTimeSeries = org.jfree.data.time.TimeSeries(name)
        for (i in 0 until tickSeries.tickCount) {
            val tick = tickSeries.getTick(i)
            chartTimeSeries.add(Minute(Date.from(tick.endTime.toInstant())), indicator.getValue(i).toDouble())
        }
        return chartTimeSeries
    }

    /**
     * Runs a strategy over a time series and adds the value markers
     * corresponding to buy/sell signals to the plot.
     *
     * @param series   a time series
     * @param strategy a trading strategy
     * @param plot     the plot
     */
    private fun addBuySellSignals(series: TimeSeries, strategy: Strategy, plot: XYPlot) {
        // Running the strategy
        val seriesManager = TimeSeriesManager(series)
        val trades = seriesManager.run(strategy).trades
        // Adding markers to plot
        for (trade in trades) {
            addBuySignals(series, plot, trade.getEntries())
            addSellSignals(series, plot, trade.getExits())
        }
    }

    private fun addSellSignals(series: TimeSeries, plot: XYPlot, exits: List<Order>) {
        for (exit in exits) {
            val sellSignalTickTime = Minute(Date.from(series.getTick(exit.index).endTime.toInstant())).firstMillisecond.toDouble()
            val sellMarker = ValueMarker(sellSignalTickTime)
            sellMarker.paint = Color.RED
            sellMarker.label = "S"
            plot.addDomainMarker(sellMarker)
        }
    }

    private fun addBuySignals(series: TimeSeries, plot: XYPlot, entries: List<Order>) {
        for (entry in entries) {
            val buySignalTickTime = Minute(Date.from(series.getTick(entry.index).endTime.toInstant())).firstMillisecond.toDouble()
            val buyMarker = ValueMarker(buySignalTickTime)
            buyMarker.paint = Color.GREEN
            buyMarker.label = "B"
            plot.addDomainMarker(buyMarker)
        }
    }

    /**
     * Displays a chart in a frame.
     *
     * @param chart the chart to be displayed
     */
    private fun displayChart(chart: JFreeChart) {
        // Chart panel
        val panel = ChartPanel(chart)
        panel.fillZoomRectangle = true
        panel.isMouseWheelEnabled = true
        panel.preferredSize = Dimension(1024, 400)
        // Application frame
        val frame = ApplicationFrame("Ta4j example - Buy and sell signals to chart")
        frame.contentPane = panel
        frame.pack()
        RefineryUtilities.centerFrameOnScreen(frame)
        frame.isVisible = true
    }

    @JvmStatic
    fun main(args: Array<String>) {

        // Getting the time series
        val series = CsvTradesLoader.loadBitstampSeries()
        // Building the trading strategy
        val strategy = MovingMomentumStrategy.buildStrategy(series)

        /**
         * Building chart datasets
         */
        val dataset = TimeSeriesCollection()
        dataset.addSeries(buildChartTimeSeries(series, ClosePriceIndicator(series), "Bitstamp Bitcoin (BTC)"))

        /**
         * Creating the chart
         */
        val chart = ChartFactory.createTimeSeriesChart(
                "Bitstamp BTC", // title
                "Date", // x-axis label
                "Price", // y-axis label
                dataset, // data
                true, // create legend?
                true, // generate tooltips?
                false // generate URLs?
        )
        val plot = chart.plot as XYPlot
        val axis = plot.domainAxis as DateAxis
        axis.dateFormatOverride = SimpleDateFormat("MM-dd HH:mm")

        /**
         * Running the strategy and adding the buy and sell signals to plot
         */
        addBuySellSignals(series, strategy, plot)
        try {
            ChartUtilities.saveChartAsPNG(File("test.png"), chart, 1920, 1024)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        /**
         * Displaying the chart
         */
        //        displayChart(chart);
    }
}
