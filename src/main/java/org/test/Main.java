package org.test;

import java.io.IOException;

class Main {
  // Sorry :) didn't find time to check how to configure dependency injection for console apps.
  private static final CountUniqueIpsInFileService countUniqueIpsInFileService = new CountUniqueIpsInFileService();

  public static void main(String[] args) throws IOException, LightspeedTestException {
    int uniqueIpsCount = countUniqueIpsInFileService.countUniqueIpsInFile(args[0]);
    System.out.println("Unique IP addresses count: " + uniqueIpsCount);
  }
}
