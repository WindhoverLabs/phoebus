package com.windhoverlabs.display.representation;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import org.csstudio.javafx.rtplot.internal.PlotPart;
import org.csstudio.javafx.rtplot.internal.PlotPartListener;
import org.csstudio.javafx.rtplot.internal.YAxisImpl;
import org.csstudio.javafx.rtplot.internal.util.GraphicsUtils;
import org.phoebus.ui.javafx.UpdateThrottle;
import org.python.modules.gc;

public class Waypointpath extends Canvas {

  /** Area of this canvas */
  protected volatile Rectangle area = new Rectangle(0, 0, 0, 0);

  /** Background color */
  private volatile Color background = Color.WHITE;

  /** Fill color */
  private volatile Color empty = Color.LIGHT_GRAY.brighter().brighter();

  private volatile Color empty_shadow = Color.LIGHT_GRAY;

  /** Fill color */
  private volatile Color fill = Color.BLUE;

  private volatile Color fill_highlight = new Color(72, 72, 255);

  /** Current value, i.e. fill level */
  private volatile double value = 5.0;

  /** Does layout need to be re-computed? */
  protected final AtomicBoolean need_layout = new AtomicBoolean(true);

  /** Throttle updates, enforcing a 'dormant' period */
  private final UpdateThrottle update_throttle;

  /** Buffer for image of the tank and scale */
  private volatile Image plot_image = null;

  /** Is the scale displayed or not. */
  private volatile boolean scale_visible = true;

  private int testValue = 0;

  private double prevLat = 0;
  private double prevLon = 0;
  private double nextLat = 0;
  private double nextLon = 0;
  private double currentLat = 0;
  private double currentLon = 0;

  private double prevRectX = 0;
  private double prevRectY = 0;

  /** Listener to {@link PlotPart}s, triggering refresh of canvas */
  protected final PlotPartListener plot_part_listener =
      new PlotPartListener() {
        @Override
        public void layoutPlotPart(final PlotPart plotPart) {
          need_layout.set(true);
        }

        @Override
        public void refreshPlotPart(final PlotPart plotPart) {
          requestUpdate();
        }
      };

  /** Redraw the canvas on UI thread by painting the 'plot_image' */
  private final Runnable redraw_runnable =
      () -> {
        updateCanvas();
      };

  private final YAxisImpl<Double> scale = new YAxisImpl<>("", plot_part_listener);

  private final PlotPart plot_area = new PlotPart("main", plot_part_listener);

  /** Constructor */
  public Waypointpath() {
    final ChangeListener<? super Number> resize_listener =
        (prop, old, value) -> {
          area = new Rectangle((int) getWidth(), (int) getHeight());
          need_layout.set(true);
          requestUpdate();
        };
    widthProperty().addListener(resize_listener);
    heightProperty().addListener(resize_listener);

    // 50Hz default throttle
    update_throttle =
        new UpdateThrottle(
            50,
            TimeUnit.MILLISECONDS,
            () -> {
              redrawSafely();
            });
  }

  /**
   * Update the dormant time between updates
   *
   * @param dormant_time How long throttle remains dormant after a trigger
   * @param unit Units for the dormant period
   */
  public void setUpdateThrottle(final long dormant_time, final TimeUnit unit) {
    update_throttle.setDormantTime(dormant_time, unit);
  }

  /** @param font Scale font */
  public void setFont(final Font font) {
    scale.setLabelFont(font);
    scale.setScaleFont(font);
  }

  /** @param color Background color */
  public void setBackground(final javafx.scene.paint.Color color) {
    background = GraphicsUtils.convert(Objects.requireNonNull(color));
  }

  /** @param color Foreground color */
  public void setForeground(final javafx.scene.paint.Color color) {
    scale.setColor(color);
  }

  /** @param color Color for empty region */
  public void setEmptyColor(final javafx.scene.paint.Color color) {
    empty = GraphicsUtils.convert(Objects.requireNonNull(color));
    empty_shadow =
        new Color(
            Math.max(0, empty.getRed() - 32),
            Math.max(0, empty.getGreen() - 32),
            Math.max(0, empty.getBlue() - 32),
            empty.getAlpha());
  }

  /** @param color Color for filled region */
  public void setFillColor(final javafx.scene.paint.Color color) {
    fill = GraphicsUtils.convert(Objects.requireNonNull(color));
    final int saturationContribution =
        (int) (48.f * Color.RGBtoHSB(fill.getRed(), fill.getGreen(), fill.getBlue(), null)[1]);
    fill_highlight =
        new Color(
            Math.min(255, fill.getRed() + 32 + saturationContribution),
            Math.min(255, fill.getGreen() + 32 + saturationContribution),
            Math.min(255, fill.getBlue() + 32 + saturationContribution),
            empty.getAlpha());
  }

  /** @param visible Whether the scale must be displayed or not. */
  public void setScaleVisible(boolean visible) {
    if (visible != scale_visible) {
      scale_visible = visible;
      need_layout.set(true);
    }
  }

  /**
   * Set value range
   *
   * @param low
   * @param high
   */
  public void setRange(final double low, final double high) {
    scale.setValueRange(low, high);
  }

  public void updateWaypoints(
      double currentLon,
      double currentLat,
      double prevLon,
      double prevLat,
      double nextLon,
      double nextLat) {
    this.prevLat = prevLat;
    this.prevLon = prevLon;
    this.nextLat = nextLat;
    this.nextLon = nextLon;
    this.currentLat = currentLat;
    this.currentLon = currentLon;
  }

  /** @param value Set value */
  public void setValue(final double value) {
    requestUpdate();
  }

  public static double normalise(double inValue, double min, double max) {
    if ((max - min) == 0) {
      return 0;
    }
    return (inValue - min) / (max - min);
  }

  /** Draw all components into image buffer */
  public void updateCanvas() {

    final GraphicsContext gc = this.getGraphicsContext2D();
    gc.beginPath();
    gc.setLineWidth(2);
    gc.setStroke(javafx.scene.paint.Color.BLACK);

    gc.setFill(javafx.scene.paint.Color.BLUE);
    gc.clearRect(prevRectX, prevRectY, 10, 10);

    // lineScale should be part of the class. Maybe even make it a property for users to adjust.
    double lineScale = 100;
    gc.lineTo(0, 0);
    gc.lineTo(lineScale, lineScale);
    gc.stroke();

    double currentX = this.currentLon;
    double currentY = this.currentLat;
    double prevX = this.prevLon;
    double prevY = this.prevLat;
    double nextX = this.nextLon;
    double nextY = this.nextLat;

    // Handle case when the values are not "normal" -> prev > next
    double currentXNormalized = normalise(currentX, prevX, nextX);
    double currentYNormalized = normalise(currentY, prevY, nextY);

    prevRectX = lineScale * currentXNormalized;
    prevRectY = lineScale * currentYNormalized;

    gc.fillRect(lineScale * currentXNormalized, lineScale * currentYNormalized, 10, 10);
  }

  /** Request a complete redraw of the plot */
  public final void requestUpdate() {
    update_throttle.trigger();
  }

  /**
   * Redraw the current image and cursors
   *
   * <p>May be called from any thread.
   */
  final void redrawSafely() {
    Platform.runLater(redraw_runnable);
  }

  /** Should be invoked when plot no longer used to release resources */
  public void dispose() { // Stop updates which could otherwise still use
    // what's about to be disposed
    update_throttle.dispose();
  }
}
