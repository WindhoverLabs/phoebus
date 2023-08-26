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
import java.util.concurrent.atomic.AtomicInteger;
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

  public AtomicInteger jobBarrier = new AtomicInteger(0);

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
              pages.parallelStream()
                  .forEach(
                      page -> {
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
                      });

              while (jobBarrier.get() < pages.size()) {
                try {
                  Thread.sleep(1000);
                } catch (InterruptedException e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
                }
              }

              CSVPrinter csvPrinter = null;
              ArrayList<String> columnHeaders = new ArrayList<String>();
              try {
                columnHeaders.add("Time");
                columnHeaders.add("RelativeTime_MS");

                for (String p : this.parameters) {
                  var nameParts = p.split("/");
                  var name = nameParts[nameParts.length - 1];
                  columnHeaders.add(name);
                  columnHeaders.add(name + "Count");
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
                HashMap<Instant, HashMap<String, Integer>> zeroParamToCountMap =
                    new HashMap<Instant, HashMap<String, Integer>>();
                for (var p : this.parameters) {
                  var nameParts = p.split("/");
                  var name = nameParts[nameParts.length - 1];
                  zeroParamToCountMap
                      .computeIfAbsent(timeZero, (instant) -> new HashMap<String, Integer>())
                      .put(name, 0);
                }
                //                System.out.println("zeroParamToCountMap:" + zeroParamToCountMap);
                resolvePvsForRecord(timeZero, recordZero, zeroParamToCountMap, null);

                csvPrinter.printRecord(recordZero);

                HashMap<Instant, HashMap<String, Integer>> paramToCountMap =
                    new HashMap<Instant, HashMap<String, Integer>>();

                HashMap<Instant, HashMap<String, ParameterValue>> paramToLatestValMap =
                    new HashMap<Instant, HashMap<String, ParameterValue>>();
                paramToCountMap.put(
                    timeZero, zeroParamToCountMap.entrySet().iterator().next().getValue());
                var latestValueForParam = new HashMap<String, ParameterValue>();
                for (int i = 1; i < sortedTimeStamps.size(); i++) {
                  ArrayList<String> record = new ArrayList<String>();

                  var currentCountMap =
                      paramToCountMap.computeIfAbsent(
                          sortedTimeStamps.get(i), (item) -> new HashMap<String, Integer>());
                  var currentLatestValMap =
                      paramToLatestValMap.computeIfAbsent(
                          sortedTimeStamps.get(i), (item) -> new HashMap<String, ParameterValue>());
                  for (var p : this.parameters) {
                    var nameParts = p.split("/");
                    var name = nameParts[nameParts.length - 1];
                    currentCountMap.put(name, 0);
                    var prevParam =
                        timeStampToParameters.get(sortedTimeStamps.get(i - 1)).get(name);
                    var currentParam =
                        timeStampToParameters.get(sortedTimeStamps.get(i - 1)).get(name);
                    if (currentParam.pv != null) {
                      latestValueForParam.put(name, currentParam.pv);
                    }
                    if (prevParam.pv != null) {
                      int prevCount = paramToCountMap.get(sortedTimeStamps.get(i - 1)).get(name);
                      currentCountMap.put(name, prevCount + 1);
                    } else {
                      int prevCount = paramToCountMap.get(sortedTimeStamps.get(i - 1)).get(name);
                      currentCountMap.put(name, prevCount);
                    }

                    if (latestValueForParam.containsKey(name))
                      ;
                    {
                      currentLatestValMap.put(name, latestValueForParam.get(name));
                    }
                  }
                }

                for (int i = 1; i < sortedTimeStamps.size(); i++) {
                  ArrayList<String> record = new ArrayList<String>();

                  Duration zeroDelta = Duration.between(timeZero, sortedTimeStamps.get(i));
                  deltaCount = zeroDelta.toMillis();
                  record.add(sortedTimeStamps.get(i).toString());
                  record.add(Long.toString(deltaCount));
                  resolvePvsForRecord(
                      sortedTimeStamps.get(i), record, paramToCountMap, paramToLatestValMap);
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
              monitor.worked(100);

              cancel_poll.exit = true;
            });
    System.out.println("performExport#2");
    monitor.worked(10);
  }

  private void resolvePvsForRecord(
      Instant currentTimeStamp,
      ArrayList<String> record,
      HashMap<Instant, HashMap<String, Integer>> currentCountMap,
      HashMap<Instant, HashMap<String, ParameterValue>> latestValueMap) {
    for (var p : this.parameters) {
      var nameParts = p.split("/");
      var name = nameParts[nameParts.length - 1];
      CountedParameterValue currentCountedP = timeStampToParameters.get(currentTimeStamp).get(name);
      if (currentCountedP.pv != null) {
        resolvePV(record, currentCountedP);
        record.add(Integer.toString(currentCountMap.get(currentTimeStamp).get(name)));
      } else {
        //        System.out.println("resolvePvsForRecord1:" + currentCountMap);
        //        System.out.println("resolvePvsForRecord2:" +
        // currentCountMap.get(currentTimeStamp));
        //        System.out.println("resolvePvsForRecord3:" + name);
        //        System.out.println(
        //            "resolvePvsForRecord4:" + currentCountMap.get(currentTimeStamp).get(name));

        if (latestValueMap == null || latestValueMap.get(currentTimeStamp).get(name) == null) {
          record.add("N/A");
        } else {
          CountedParameterValue latestCountedP =
              new CountedParameterValue(latestValueMap.get(currentTimeStamp).get(name), 0);
          resolvePV(record, latestCountedP);
        }
        record.add(Integer.toString(currentCountMap.get(currentTimeStamp).get(name)));
      }
    }
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

  private synchronized void constructTimeToParamsMap(ParameterValue pv) {
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

    jobBarrier.getAndAdd(1);
  }

  private void updateCountForPV() {}
}
