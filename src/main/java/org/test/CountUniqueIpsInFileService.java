package org.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class CountUniqueIpsInFileService {
  private static final int FILE_READ_BUFFER_SIZE = 1000;
  private static final char DOT = '.';
  private static final char LINE_DELIMITER = '\n';

  private int currentIpPartIndex = 0;
  private int uniqueIpsCount = 0;

  // Sorry :) didn't find time to check how to configure dependency injection for console apps.
  private final ProcessedIpsStorage processedIpsStorage = new ProcessedIpsStorage();

  // This value declared here in order not to create new objects for every IP
  // which causes frequent garbage collection.
  private final int[] currentIpParts = new int[4];

  public int countUniqueIpsInFile(String fileDir) throws IOException, LightspeedTestException {
    try (BufferedReader reader = new BufferedReader(new FileReader(fileDir))) {
      char[] buffer = new char[FILE_READ_BUFFER_SIZE];
      StringBuilder ipPartStringBuilder = new StringBuilder();
      for (int charsReadCount = reader.read(buffer);
           charsReadCount != -1;
           charsReadCount = reader.read(buffer)) {
        processBuffer(ipPartStringBuilder, buffer, charsReadCount);
      }
      return uniqueIpsCount;
    }
  }

  private void processBuffer(StringBuilder ipPartStringBuilder, char[] buffer, int charsReadCount) throws LightspeedTestException {
    for (int charIndex = 0; charIndex < charsReadCount; charIndex++) {
      char character = buffer[charIndex];
      if (Character.isDigit(character)) {
        ipPartStringBuilder.append(character);
      } else {
        int ipPart = Integer.parseInt(ipPartStringBuilder, 0, ipPartStringBuilder.length(), 10);
        currentIpParts[currentIpPartIndex] = ipPart;
        ipPartStringBuilder.setLength(0);
        switch (character) {
          case DOT -> currentIpPartIndex++;
          case LINE_DELIMITER -> {
            if (!processedIpsStorage.checkIpWasAlreadyMet(currentIpParts)) {
              processedIpsStorage.saveIp(currentIpParts);
              uniqueIpsCount++;
            }
            currentIpPartIndex = 0;
          }
          default -> throw new LightspeedTestException("Unknown character: " + character);
        }
      }
    }
  }
}
