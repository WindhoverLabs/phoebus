/*******************************************************************************
 * Copyright (c) 2010-2018 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package com.windhoverlabs.yamcs.applications.parameter;

import com.windhoverlabs.yamcs.core.YamcsObjectManager;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.csstudio.trends.databrowser3.Activator;
import org.phoebus.archive.reader.ArchiveReader;
import org.phoebus.framework.jobs.JobMonitor;
import org.phoebus.framework.jobs.JobRunnable;
import org.yamcs.client.Helpers;
import org.yamcs.protobuf.Pvalue.ParameterValue;

/**
 * Base for Eclipse Job for exporting data from Model to file
 *
 * @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ExportCSVJob implements JobRunnable {

  class CountedParameterValue {
    private ParameterValue pv;
    int count;

    public CountedParameterValue(ParameterValue pv, int count) {
      this.pv = pv;
      this.count = count;
    }
  }

  public static final int PROGRESS_UPDATE_LINES = 1000;
  public Instant start, end;
  public String filename;
  public Consumer<Exception> error_handler;
  public ArrayList<String> parameters = new ArrayList<String>();

  public HashMap<Instant, HashMap<String, CountedParameterValue>> timeStampToParameters =
      new HashMap<Instant, HashMap<String, CountedParameterValue>>();
  /** Active readers, used to cancel and close them */
  private final CopyOnWriteArrayList<ArchiveReader> archive_readers =
      new CopyOnWriteArrayList<ArchiveReader>();

  public final boolean unixTimeStamp;
  private CancellationPoll cancel_poll;

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
      final Instant start,
      final Instant end,
      final String filename,
      final Consumer<Exception> error_handler,
      final boolean unixTimeStamp,
      ArrayList<String> parameters) {
    this.start = start;
    this.end = end;
    this.filename = filename;
    this.error_handler = error_handler;
    this.unixTimeStamp = unixTimeStamp;
    this.parameters = parameters;
  }

  /** Job's main routine {@inheritDoc} */
  @Override
  public final void run(final JobMonitor monitor) {
    monitor.beginTask("Data Export", 100);

    try {
      BufferedWriter writer;
      if (filename != null) {
        writer = Files.newBufferedWriter(Paths.get(filename));
        //                printExportzInfo(out);
      } else writer = null;
      // Start thread that checks monitor to cancels readers when
      // user tries to abort the export job
      cancel_poll = new CancellationPoll(monitor);
      final Future<?> done = Activator.thread_pool.submit(cancel_poll);
      performExport(monitor, writer);
      // ask thread to exit
      //      cancel_poll.exit = true;
      //      if (writer != null) writer.close();
      // Wait for poller to quit
      done.get();
    } catch (final Exception ex) {
      error_handler.accept(ex);
    }
    //    for (ArchiveReader reader : archive_readers) reader.close();
  }

  //    /** Print file header, gets invoked before <code>performExport</code> */
  //    public void printExportInfo(final PrintStream out)
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
  private void performExport(final JobMonitor monitor, BufferedWriter writer) {
    monitor.worked(0);
    YamcsObjectManager.getDefaultInstance()
        .getParameters(
            YamcsObjectManager.getDefaultServer().getYamcsClient(),
            this.parameters,
            start,
            end,
            (pages) -> {
              for (var page : pages) {
                page.iterator()
                    .forEachRemaining(
                        pv -> {
                          constructTimeToParamsMap(pv);
                        });
                while (page.hasNextPage()) {
                  try {
                    try {
                      page = page.getNextPage().get(1, TimeUnit.MINUTES);
                    } catch (TimeoutException e) {
                      // TODO Auto-generated catch block
                      e.printStackTrace();
                    }

                  } catch (InterruptedException | ExecutionException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                  }
                  page.iterator()
                      .forEachRemaining(
                          pv -> {
                            constructTimeToParamsMap(pv);
                          });
                }
              }

              CSVPrinter csvPrinter = null;
              ArrayList<String> columnHeaders = new ArrayList<String>();
              HashMap<Integer, String> columnIndexToPName = new HashMap<Integer, String>();
              try {
                columnHeaders.add("Time");
                columnHeaders.add("RelativeTime_MS");

                for (String p : this.parameters) {
                  var nameParts = p.split("/");
                  var name = nameParts[nameParts.length - 1];
                  columnHeaders.add(name);
                }

                csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT);

              } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
              }
              try {
                System.out.println("performExport#14");
                csvPrinter.printRecord(columnHeaders);

                List<Instant> sortedTimeStamps =
                    new ArrayList<Instant>(timeStampToParameters.keySet());
                Collections.sort(sortedTimeStamps);

                long deltaCount = 0;
                Instant timeZero = sortedTimeStamps.get(0);
                ArrayList<String> recordZero = new ArrayList<String>();
                recordZero.add(timeZero.toString());
                recordZero.add(Long.toString(deltaCount));
                for (var p : this.parameters) {
                  var nameParts = p.split("/");
                  var name = nameParts[nameParts.length - 1];
                  var countedP = timeStampToParameters.get(timeZero).get(name);
                  if (countedP.pv != null) {
                    resolvePV(recordZero, countedP);
                  } else {
                    recordZero.add("N/A");
                  }
                }
                csvPrinter.printRecord(recordZero);

                for (int i = 1; i < sortedTimeStamps.size(); i++) {
                  ArrayList<String> record = new ArrayList<String>();
                  record.add(sortedTimeStamps.get(i).toString());
                  record.add(Long.toString(deltaCount));
                  for (var p : this.parameters) {
                    var nameParts = p.split("/");
                    var name = nameParts[nameParts.length - 1];
                    var countedP = timeStampToParameters.get(sortedTimeStamps.get(i)).get(name);
                    if (countedP.pv != null) {
                      resolvePV(record, countedP);
                    } else {
                      record.add("N/A");
                    }
                  }
                  csvPrinter.printRecord(record);

                  Duration d =
                      Duration
                          .between( // Calculate the span of time between two moments as a number of
                              // hours, minutes, and seconds.
                              timeZero, // Convert legacy class to modern class by calling new
                              // method added to the old class.
                              sortedTimeStamps.get(
                                  i - 1) // Capture the current moment in UTC. About two and a
                              // half hours
                              // later in this example.
                              );

                  deltaCount = d.toMillis();
                }

                System.out.println("performExport#11");

              } catch (IOException e) {
                //              //  				// TODO Auto-generated catch block
                //              //  				e.printStackTrace();
              }
              try {
                csvPrinter.flush();
              } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
              }
              monitor.worked(100);

              cancel_poll.exit = true;
            });
    System.out.println("performExport#2");
    monitor.worked(10);
  }

  private void resolvePV(ArrayList<String> record, CountedParameterValue countedP) {
    switch (countedP.pv.getEngValue().getType()) {
      case AGGREGATE:
        record.add(countedP.pv.getEngValue().getType().toString());
        break;
      case ARRAY:
        record.add(countedP.pv.getEngValue().getType().toString());
        break;
      case BINARY:
        record.add(countedP.pv.getEngValue().getType().toString());
        break;
      case BOOLEAN:
        record.add(Boolean.toString(countedP.pv.getEngValue().getBooleanValue()));
        break;
      case DOUBLE:
        record.add(Double.toString(countedP.pv.getEngValue().getDoubleValue()));
        break;
      case ENUMERATED:
        record.add(countedP.pv.getEngValue().getType().toString());
        break;
      case FLOAT:
        record.add(Float.toString(countedP.pv.getEngValue().getFloatValue()));
        break;
      case NONE:
        record.add(countedP.pv.getEngValue().getType().toString());
        break;
      case SINT32:
        record.add(Integer.toString(countedP.pv.getEngValue().getSint32Value()));
        break;
      case SINT64:
        record.add(Long.toString(countedP.pv.getEngValue().getSint64Value()));
        break;
      case STRING:
        record.add(countedP.pv.getEngValue().getStringValue());
        break;
      case TIMESTAMP:
        record.add(countedP.pv.getEngValue().getType().toString());
        break;
      case UINT32:
        record.add(Integer.toString(countedP.pv.getEngValue().getUint32Value()));
        break;
      case UINT64:
        record.add(Long.toString(countedP.pv.getEngValue().getUint64Value()));
        break;
      default:
        record.add("Something_ELSE");
        break;
    }
  }

  private void constructTimeToParamsMap(ParameterValue pv) {
    Instant pvGenerationTime = Helpers.toInstant(pv.getGenerationTime());

    timeStampToParameters.computeIfAbsent(
        pvGenerationTime,
        p -> {
          return new HashMap<String, CountedParameterValue>();
        });
    System.out.println("constructTimeToParamsMap3");
    String pvNameKey = pv.getId().getName();

    //                       TODO: This has terrible, terrible performance, like O(n * n) bad.
    for (String parameterName : this.parameters) {
      System.out.println("constructTimeToParamsMap4");
      var nameParts = parameterName.split("/");
      var countedParams =
          timeStampToParameters
              .get(pvGenerationTime)
              .computeIfAbsent(
                  nameParts[nameParts.length - 1],
                  p -> {
                    return new CountedParameterValue(null, 0);
                  });
    }
    timeStampToParameters.get(pvGenerationTime).put(pvNameKey, new CountedParameterValue(pv, 0));
  }
}
