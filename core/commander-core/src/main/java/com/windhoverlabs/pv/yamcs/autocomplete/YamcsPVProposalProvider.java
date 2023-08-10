package com.windhoverlabs.pv.yamcs.autocomplete;

import com.windhoverlabs.yamcs.core.YamcsObjectManager;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.phoebus.framework.autocomplete.LocProposal;
import org.phoebus.framework.autocomplete.Proposal;
import org.phoebus.framework.spi.PVProposalProvider;
import org.yamcs.protobuf.Mdb.ParameterTypeInfo;

public class YamcsPVProposalProvider implements PVProposalProvider {
  public static final YamcsPVProposalProvider INSTANCE = new YamcsPVProposalProvider();

  private static final List<Proposal> generic =
      List.of(new LocProposal("loc://name", "VType", "initial value..."));

  public YamcsPVProposalProvider() {
    // Singleton
  }

  @Override
  public String getName() {
    return "YAMCS PV";
  }

  /**
   * Get proposals
   *
   * @param text Text entered by user
   * @return {@link Proposal}s that could be applied to the text
   */
  @Override
  public List<Proposal> lookup(final String text) {

    var content = text;
    //      TODO:Make this an argument
    int limit = 100;
    //      TODO:Don't worry about prefixes like "param", "ops" for now..
    //      if (content.startsWith(getPrefix())) {
    //          content = content.substring(getPrefix().length());
    //      } else if (requirePrefix()) {
    //          return new AutoCompleteResult();
    //      }

    //      content = AutoCompleteHelper.trimWildcards(content);
    //      content = content.replaceAll("\\[[0-9]+\\]", "[]"); // Ignore specific index into array
    //      var namePattern = AutoCompleteHelper.convertToPattern(content);
    //      namePattern = Pattern.compile(namePattern.pattern(), Pattern.CASE_INSENSITIVE);

    var regex = Pattern.quote(content);
    Pattern namePattern = Pattern.compile(regex);
    namePattern = Pattern.compile(namePattern.pattern(), Pattern.CASE_INSENSITIVE);

    var result = new ArrayList<Proposal>();
    var matchCount = 0;
    var mdb = YamcsObjectManager.getDefaultInstance().getMissionDatabase();
    if (mdb != null) {
      for (var para : mdb.getParameters()) {
        var pvCandidates = new ArrayList<String>();
        pvCandidates.add(para.getQualifiedName());
        if (para.hasType()) {
          scanTypeForPvCandidates(para.getQualifiedName(), para.getType(), pvCandidates);
        }
        for (var pvCandidate : pvCandidates) {
          var proposalValue = pvCandidate;
          var m = namePattern.matcher(proposalValue);
          if (m.find()) {
            var p = new Proposal(proposalValue);
            //                      p.addStyle(ProposalStyle.getDefault(m.start(), m.end() - 1));
            result.add(p);
            matchCount++;
            if (matchCount >= limit) {
              break;
            }
          }
        }

        if (matchCount >= limit) {
          break;
        }
      }
    }

    //      result.setCount(matchCount);
    return result;
  }

  private void scanTypeForPvCandidates(
      String basePvName, ParameterTypeInfo type, List<String> pvCandidates) {
    for (var member : type.getMemberList()) {
      var memberPvName = basePvName + "." + member.getName();
      pvCandidates.add(memberPvName);
      if (member.hasType()) {
        scanTypeForPvCandidates(memberPvName, member.getType(), pvCandidates);
      }
    }
    if (type.hasArrayInfo()) {
      var entryPvName = basePvName + "[]";
      var entryType = type.getArrayInfo().getType();
      scanTypeForPvCandidates(entryPvName, entryType, pvCandidates);
    }
  }

  //  {
  //	    if (!text.startsWith("loc://")) return generic;
  //
  //	    final List<Proposal> result = new ArrayList<>();
  //	    //        final List<String> split = LocProposal.splitNameTypeAndInitialValues(text);
  //	    final List<String> split = new ArrayList<String>();
  //
  //	    // Use the entered name, but add "loc://".
  //	    // Default to just "loc://name"
  //	    String name = split.get(0).trim();
  //	    if (name.isEmpty()) name = "loc://name";
  //	    else if (!name.startsWith("loc://")) name = "loc://" + name;
  //
  //	    // Use the entered type, or default to "VType"
  //	    String type = split.get(1);
  //	    if (type != null) {
  //	      result.add(new LocProposal(name, "VDouble", "number"));
  //	      result.add(new LocProposal(name, "VLong", "number"));
  //	      result.add(new LocProposal(name, "VString", "\"string\""));
  //	      result.add(new LocProposal(name, "VEnum", "index", "\"Label 1\"", "\"Label 2\", ..."));
  //	      result.add(new LocProposal(name, "VDoubleArray", "number", "number, ..."));
  //	      result.add(new LocProposal(name, "VStringArray", "\"string\"", "\"string\", ..."));
  //	      result.add(new LocProposal(name, "VTable"));
  //	    } else {
  //	      result.add(new LocProposal(name, "VType", "number"));
  //	      result.add(new LocProposal(name, "VType", "\"string\""));
  //	    }
  //	    return result;
  //  }

  //  @Override
  //  public AutoCompleteResult listResult(ContentDescriptor desc, int limit) {
  //
  //  }

}
