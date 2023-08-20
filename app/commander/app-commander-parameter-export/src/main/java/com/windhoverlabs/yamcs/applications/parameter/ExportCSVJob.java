/*******************************************************************************
 * Copyright (c) 2010-2018 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package com.windhoverlabs.yamcs.applications.parameter;

import java.io.PrintStream;
import java.time.Instant;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import org.csstudio.trends.databrowser3.Activator;
import org.csstudio.trends.databrowser3.model.ArchiveDataSource;
import org.csstudio.trends.databrowser3.model.Model;
import org.csstudio.trends.databrowser3.model.ModelItem;
import org.csstudio.trends.databrowser3.model.PVItem;
import org.phoebus.archive.reader.ArchiveReader;
import org.phoebus.framework.jobs.JobMonitor;
import org.phoebus.framework.jobs.JobRunnable;

/**
 * Base for Eclipse Job for exporting data from Model to file
 *
 * @author Kay Kasemir
 */
@SuppressWarnings("nls")
public abstract class ExportCSVJob implements JobRunnable {
  protected static final int PROGRESS_UPDATE_LINES = 1000;
  protected final String comment;
  protected final Model model;
  protected final Instant start, end;
  protected final double optimize_parameter;
  protected final String filename;
  protected final Consumer<Exception> error_handler;
  /** Active readers, used to cancel and close them */
  private final CopyOnWriteArrayList<ArchiveReader> archive_readers =
      new CopyOnWriteArrayList<ArchiveReader>();

  protected final boolean unixTimeStamp;

  /**
   * Thread that polls a progress monitor and cancels active archive readers if the user requests
   * the export job to end via the progress monitor
   */
  class CancellationPoll implements Runnable {
    private final JobMonitor monitor;
    volatile boolean exit = false;

    public CancellationPoll(final JobMonitor monitor) {
      this.monitor = monitor;
    }

    @Override
    public void run() {
      while (!exit) {
        if (monitor.isCanceled()) {
          for (ArchiveReader reader : archive_readers) reader.cancel();
        }
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          // Ignore
        }
      }
    }
  }

  /**
   * @param comment Comment prefix ('#' for most ASCII, '%' for Matlab, ...)
   * @param model Model with data
   * @param start Start time
   * @param end End time
   * @param source Where to get samples
   * @param optimize_parameter Used by optimized source
   * @param filename Name of file to create or <code>null</code> if <code>performExport</code>
   *     handles the file
   * @param error_handler Callback for errors
   * @param unixTimeStamp If <code>true</code>, time stamps are UNIX style, i.e. ms since EPOCH.
   *     Defaults to false.
   */
  public ExportCSVJob(
      final String comment,
      final Model model,
      final Instant start,
      final Instant end,
      final double optimize_parameter,
      final String filename,
      final Consumer<Exception> error_handler,
      final boolean unixTimeStamp) {
    this.comment = comment;
    this.model = model;
    this.start = start;
    this.end = end;
    this.optimize_parameter = optimize_parameter;
    this.filename = filename;
    this.error_handler = error_handler;
    this.unixTimeStamp = unixTimeStamp;
  }

  /** Job's main routine {@inheritDoc} */
  @Override
  public final void run(final JobMonitor monitor) {
    monitor.beginTask("Data Export");
    try {
      final PrintStream out;
      if (filename != null) {
        out = new PrintStream(filename);
        //                printExportInfo(out);
      } else out = null;
      // Start thread that checks monitor to cancels readers when
      // user tries to abort the export job
      final CancellationPoll cancel_poll = new CancellationPoll(monitor);
      final Future<?> done = Activator.thread_pool.submit(cancel_poll);
      performExport(monitor, out);
      // ask thread to exit
      cancel_poll.exit = true;
      if (out != null) out.close();
      // Wait for poller to quit
      done.get();
    } catch (final Exception ex) {
      error_handler.accept(ex);
    }
    for (ArchiveReader reader : archive_readers) reader.close();
    monitor.done();
  }

  //    /** Print file header, gets invoked before <code>performExport</code> */
  //    protected void printExportInfo(final PrintStream out)
  //    {
  //        out.println(comment + "Created by CS-Studio Data Browser");
  //        out.println(comment);
  //        out.println(comment + "Start Time : " + TimestampFormats.MILLI_FORMAT.format(start));
  //        out.println(comment + "End Time   : " + TimestampFormats.MILLI_FORMAT.format(end));
  //        out.println(comment + "Source     : " + source.toString());
  //        if (source == Source.OPTIMIZED_ARCHIVE)
  //            out.println(comment + "Desired Value Count: " + optimize_parameter);
  //        else if (source == Source.LINEAR_INTERPOLATION)
  //            out.println(comment + "Interpolation Interval: " +
  // SecondsParser.formatSeconds(optimize_parameter));
  //    }

  /**
   * Perform the data export
   *
   * @param out PrintStream for output
   * @throws Exception on error
   */
  protected abstract void performExport(final JobMonitor monitor, final PrintStream out)
      throws Exception;

  /**
   * Print info about item
   *
   * @param out PrintStream for output
   * @param item ModelItem
   */
  protected void printItemInfo(final PrintStream out, final ModelItem item) {
    out.println(comment + "Channel: " + item.getResolvedName());
    // If display name differs from PV, show the _resolved_ version
    if (!item.getName().equals(item.getDisplayName()))
      out.println(comment + "Name   : " + item.getResolvedDisplayName());
    if (item instanceof PVItem) {
      final PVItem pv = (PVItem) item;
      out.println(comment + "Archives:");
      int i = 1;
      for (ArchiveDataSource archive : pv.getArchiveDataSources()) {
        out.println(comment + i + ") " + archive.getName());
        out.println(comment + "   URL: " + archive.getUrl());
        ++i;
      }
    }
    out.println(comment);
  }
}
