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
    //          content = content.replaceAll("\\[[0-9]+\\]", "[]"); // Ignore specific index into
    // array
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
      var entryPvName = new StringBuilder(basePvName + "[]");
      var entryType = type.getArrayInfo().getType();
      for (int i = 0; i < type.getArrayInfo().getDimensionsList().get(0).getFixedValue(); i++) {
        //        System.out.println("arrayItem-->" + entryPvName);
        entryPvName = new StringBuilder(basePvName + "[]");
        entryPvName.insert(basePvName.length() + 1, i);
        //        System.out.println("array info for " + entryPvName);
        pvCandidates.add(entryPvName.toString());
      }
    }
  }
}
