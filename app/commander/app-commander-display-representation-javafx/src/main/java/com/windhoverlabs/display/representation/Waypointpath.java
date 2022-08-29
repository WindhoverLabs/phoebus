package com.windhoverlabs.display.representation;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.text.Font;
import org.csstudio.javafx.rtplot.AxisRange;
import org.csstudio.javafx.rtplot.internal.PlotPart;
import org.csstudio.javafx.rtplot.internal.PlotPartListener;
import org.csstudio.javafx.rtplot.internal.YAxisImpl;
import org.csstudio.javafx.rtplot.internal.util.GraphicsUtils;
import org.phoebus.ui.javafx.BufferUtil;
import org.phoebus.ui.javafx.UpdateThrottle;

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
              plot_image = updateImageBuffer();
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

  /** @param value Set value */
  public void setValue(final double value) {
    if (Double.isFinite(value)) this.value = value;
    else this.value = scale.getValueRange().getLow();
    requestUpdate();
  }

  /** Draw all components into image buffer */
  public void updateCanvas() {

    System.out.println("updateCanvas%%%%%%%%%%%-->" + this.getWidth() + "," + this.getHeight());
    //    final Rectangle area_copy = area;
    //    if (area_copy.width <= 0 || area_copy.height <= 0) return;

    //    final BufferUtil buffer = BufferUtil.getBufferedImage(area_copy.width, area_copy.height);
    //    if (buffer == null) return;
    //    final BufferedImage image = buffer.getImage();

    final GraphicsContext gc = this.getGraphicsContext2D();

    //        gc.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
    // RenderingHints.VALUE_ANTIALIAS_ON);
    //        gc.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
    // RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
    //        gc.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
    // RenderingHints.VALUE_COLOR_RENDER_SPEED);
    //        gc.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);

    //        if (need_layout.getAndSet(false))
    //            computeLayout(gc, area_copy);

    //    final Rectangle plot_bounds = plot_area.getBounds();

    //        gc.setColor(background);
    //    gc.fillRect(0, 0, area_copy.width, area_copy.height);

    //        Image newImage = new Image();

    //        if (scale_visible)
    //            scale.paint(gc, plot_bounds);

    //        plot_area.paint(gc);

    //    final AxisRange<Double> range = scale.getValueRange();
    //    final boolean normal = range.getLow() <= range.getHigh();
    //    final double min = Math.min(range.getLow(), range.getHigh());
    //    final double max = Math.max(range.getLow(), range.getHigh());
    //    final double current = value;
    //    final int level;
    //    if (current <= min) level = 0;
    //    else if (current >= max) level = plot_bounds.height;
    //    else if (max == min) level = 0;
    //    else level = (int) (plot_bounds.height * (current - min) / (max - min) + 0.5);

    //    final int arc = Math.min(plot_bounds.width, plot_bounds.height) / 10;
    //        gc.setPaint(new GradientPaint(plot_bounds.x, 0, empty,
    // plot_bounds.x+plot_bounds.width/2, 0, empty_shadow, true));

    //    gc.fillRoundRect(plot_bounds.x, plot_bounds.y, plot_bounds.width, plot_bounds.height, arc,
    // arc);

    //    gc.setFill(Color.BLUE);

    //    Waypointpath.getCShape(new Line(0, 0, 50, 50));
    gc.fillRect(0, 0, 50, 50);

    //        gc.setPaint(new GradientPaint(plot_bounds.x, 0, fill,
    // plot_bounds.x+plot_bounds.width/2, 0, fill_highlight, true));
    //    if (normal)
    //      gc.fillRoundRect(
    //          plot_bounds.x,
    //          plot_bounds.y + plot_bounds.height - level,
    //          plot_bounds.width,
    //          level,
    //          arc,
    //          arc);
    //    else gc.fillRoundRect(plot_bounds.x, plot_bounds.y, plot_bounds.width, level, arc, arc);

    gc.setFill(javafx.scene.paint.Color.BLUE);

    //        gc.setFill(Color.BLUE);
    //        gc.fillRect(75,75,100,100);
    //        gc.setColor(background);
    //
    //        gc.dispose();

    //    gc.restore();
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

  public static Path getCShape(Line pConnectionPoints) {
    final int ENDSIZE = 0;
    final double x1 =
        Math.max(pConnectionPoints.getStartX(), pConnectionPoints.getEndX()) + ENDSIZE;

    MoveTo moveTo = new MoveTo(pConnectionPoints.getStartX(), pConnectionPoints.getStartY());
    LineTo lineTo1 = new LineTo(pConnectionPoints.getStartX(), pConnectionPoints.getStartY());
    LineTo lineTo2 = new LineTo(pConnectionPoints.getEndX(), pConnectionPoints.getEndY());

    Path path = new Path();
    path.getElements().addAll(moveTo, lineTo1, lineTo2);
    return path;
  }

  public static Canvas getCanvas() {
    Canvas canvas = new Canvas(250, 250);

    GraphicsContext gc = canvas.getGraphicsContext2D();

    gc.setFill(javafx.scene.paint.Color.BLUE);
    gc.fillRect(0, 0, 100, 100);

    return canvas;
  }
}
