package org.test;

public class ProcessedIpsStorage {
  private static final int NUMBER_OF_VALUES_OF_IP_PART = 256;
  private static final int NUMBER_OF_LONGS_TO_REPRESENT_256_VALUES = 4;

  // ipPartsPresences[0] represents FIRST two values of IP, ipPartsPresences[1] represents SECOND two values.
  // For example, we have IP 145.67.23.5.
  // - 145 is used for index shifting for a bitmap where 67 is stored.
  // This bitmap is stored in four long values in a range
  // from ipPartsPresences[0][145 * 4] to ipPartsPresences[0][145 * 4 + 3]
  // - 23 is used for index shifting for a bitmap where 5 is stored.
  // This bitmap is stored in four long values in a range
  // from ipPartsPresences[1][23 * 4] to ipPartsPresences[1][23 * 4 + 3]
  private final Long[][] ipPartsPresences = new Long[2][NUMBER_OF_VALUES_OF_IP_PART * NUMBER_OF_LONGS_TO_REPRESENT_256_VALUES];

  // This value declared here in order not to create new objects for every IP
  // which causes frequent garbage collection.
  private static final long[] currentOddPartLongs = new long[NUMBER_OF_LONGS_TO_REPRESENT_256_VALUES];

  public void saveIp(int[] ipParts) {
    for (int evenPartIndex = 0; evenPartIndex < ipParts.length; evenPartIndex += 2) {
      int evenPart = ipParts[evenPartIndex];
      int evenPartLongsShift = evenPart * NUMBER_OF_LONGS_TO_REPRESENT_256_VALUES;

      int oddPart = ipParts[evenPartIndex + 1];
      int oddPartLongsShift = evenPartLongsShift + (oddPart >>> 6);

      int twoIpPartsPresencesIndex = evenPartIndex == 0 ? 0 : 1;
      if (ipPartsPresences[twoIpPartsPresencesIndex][evenPartLongsShift] == null) {
        for (int oddPartLongIndex = 0; oddPartLongIndex < NUMBER_OF_LONGS_TO_REPRESENT_256_VALUES; oddPartLongIndex++) {
          ipPartsPresences[twoIpPartsPresencesIndex][evenPartLongsShift + oddPartLongIndex] = 0L;
        }
      }
      ipPartsPresences[twoIpPartsPresencesIndex][oddPartLongsShift] |= (1L << oddPart);
    }
  }

  public boolean checkIpWasAlreadyMet(int[] ipParts) {
    boolean ipWasAlreadyMet = true;
    int oddPartPresencesStartIndex = 0;
    int twoIpPartsPresencesIndex = 0;
    for (int partIndex = 0; partIndex < ipParts.length; partIndex++) {
      int part = ipParts[partIndex];
      if (partIndex % 2 == 0) {
        twoIpPartsPresencesIndex = partIndex == 0 ? 0 : 1;
        oddPartPresencesStartIndex = part * NUMBER_OF_LONGS_TO_REPRESENT_256_VALUES;
        ipWasAlreadyMet = ipPartsPresences[twoIpPartsPresencesIndex][oddPartPresencesStartIndex] != null;
      } else {
        Long[] twoIpPartsPresences = ipPartsPresences[twoIpPartsPresencesIndex];
        ipWasAlreadyMet = checkOddPartIsSaved(part, oddPartPresencesStartIndex, twoIpPartsPresences);
      }
      if (!ipWasAlreadyMet) {
        break;
      }
    }

    return ipWasAlreadyMet;
  }

  private static boolean checkOddPartIsSaved(int oddPart, int startIndex, Long[] twoIpPartsPresences) {
    for (int srcIndex = startIndex, dstIndex = 0;
         srcIndex < startIndex + NUMBER_OF_LONGS_TO_REPRESENT_256_VALUES;
         srcIndex++, dstIndex++) {
      currentOddPartLongs[dstIndex] = twoIpPartsPresences[srcIndex];
    }
    return (currentOddPartLongs[oddPart >>> 6] & (1L << oddPart)) != 0;
  }
}
