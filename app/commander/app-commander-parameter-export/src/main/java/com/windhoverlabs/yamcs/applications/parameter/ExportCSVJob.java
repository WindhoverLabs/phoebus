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

  public HashMap<Instant, HashMap<String, ArrayList<CountedParameterValue>>> timeStampToParameters =
      new HashMap<Instant, HashMap<String, ArrayList<CountedParameterValue>>>();
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
    System.out.println("performExport#1");
    monitor.worked(0);
    YamcsObjectManager.getDefaultInstance()
        .getParameters(
            YamcsObjectManager.getDefaultServer().getYamcsClient(),
            this.parameters,
            start,
            end,
            (page, e1) -> {
              page.iterator()
                  .forEachRemaining(
                      pv -> {
                        constructTimeToParamsMap(pv);
                      });
              System.out.println("performExport#3");
              while (page.hasNextPage()) {
                try {
                  System.out.println("performExport#4");
                  try {
                    page = page.getNextPage().get(1, TimeUnit.MINUTES);
                  } catch (TimeoutException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                  }
                  System.out.println("performExport#5");

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

              System.out.println("performExport#8");

              CSVPrinter csvPrinter = null;
              ArrayList<String> columnHeaders = new ArrayList<String>();
              HashMap<Integer, String> columnIndexToPName = new HashMap<Integer, String>();
              try {
                System.out.println("performExport#9:" + this.parameters);

                columnHeaders.add("Time");
                columnHeaders.add("RelativeTime_MS");

                for (String p : this.parameters) {
                  System.out.println("performExport#10:" + this.parameters);
                  var nameParts = p.split("/");
                  System.out.println("performExport#11:" + this.parameters);
                  var name = nameParts[nameParts.length - 1];
                  columnHeaders.add(name);
                  //                  columnHeaders.add(name + "_Count");
                }
                System.out.println("performExport#12:" + columnHeaders);

                csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT);

                System.out.println("performExport#13");
              } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
              }
              try {
                System.out.println("performExport#14");
                csvPrinter.printRecord(columnHeaders);

                List sortedTimeStamps = new ArrayList(timeStampToParameters.keySet());
                Collections.sort(sortedTimeStamps);

                for (var entry : sortedTimeStamps) {
                  ArrayList<String> record = new ArrayList<String>();
                  record.add(entry.toString());
                  record.add("delta");
                  for (var p : this.parameters) {
                    var nameParts = p.split("/");
                    System.out.println("performExport#11:" + this.parameters);
                    var name = nameParts[nameParts.length - 1];
                    for (var countedP : timeStampToParameters.get(entry).get(name)) {
                      switch (countedP.pv.getEngValue().getType()) {
                        case AGGREGATE:
                          break;
                        case ARRAY:
                          break;
                        case BINARY:
                          break;
                        case BOOLEAN:
                          record.add(Boolean.toString(countedP.pv.getEngValue().getBooleanValue()));
                          break;
                        case DOUBLE:
                          record.add(Double.toString(countedP.pv.getEngValue().getDoubleValue()));
                          break;
                        case ENUMERATED:
                          break;
                        case FLOAT:
                          record.add(Float.toString(countedP.pv.getEngValue().getFloatValue()));
                          break;
                        case NONE:
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
                          break;
                        case UINT32:
                          record.add(Integer.toString(countedP.pv.getEngValue().getUint32Value()));
                          break;
                        case UINT64:
                          record.add(Long.toString(countedP.pv.getEngValue().getUint64Value()));
                          break;
                        default:
                          break;
                      }
                    }
                  }
                  csvPrinter.printRecord(record);
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
              System.out.println("performExport#6");
              monitor.worked(100);

              //              for(var e: )
              //              {
              //
              //              }
              System.out.println("size -->" + timeStampToParameters.keySet().size());
              //              timeStampToParameters
              //              .get(pvGenerationTime)
              //              .get(pvNameKey)
              //              .add(new CountedParameterValue(pv, 0));

              cancel_poll.exit = true;
            });
    System.out.println("performExport#2");
    monitor.worked(10);
  }

  private void constructTimeToParamsMap(ParameterValue pv) {
    System.out.println("constructTimeToParamsMap1");
    Instant pvGenerationTime = Helpers.toInstant(pv.getGenerationTime());
    System.out.println("constructTimeToParamsMap2");

    timeStampToParameters.computeIfAbsent(
        pvGenerationTime,
        p -> {
          return new HashMap<String, ArrayList<CountedParameterValue>>();
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
                    return new ArrayList<CountedParameterValue>();
                  });
    }

    //    System.out.println(
    //        "constructTimeToParamsMap7:"
    //            + timeStampToParameters
    //                .get(pvGenerationTime)
    //                .get(pvNameKey)
    //                .add(new CountedParameterValue(pv, 0)));
    timeStampToParameters
        .get(pvGenerationTime)
        .get(pvNameKey)
        .add(new CountedParameterValue(pv, 0));

    System.out.println("constructTimeToParamsMap5:" + timeStampToParameters.get(pvGenerationTime));

    System.out.println("constructTimeToParamsMap7");
  }
}
