package org.tamtamcatworks.auction.client.component.admin.analytics;

import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

public class AnalyticsChart extends LineChart<String, Number> {

  public AnalyticsChart() {

    super(new CategoryAxis(), new NumberAxis());

    setAnimated(false);

    setLegendVisible(false);

    setTitle("Analytics");

    getStyleClass().add("analytics-chart");
  }

  public void addSeries(String name, java.util.Map<String, Number> data) {

    XYChart.Series<String, Number> series = new XYChart.Series<>();

    series.setName(name);

    data.forEach((key, value) -> series.getData().add(new XYChart.Data<>(key, value)));

    getData().add(series);
  }
}
