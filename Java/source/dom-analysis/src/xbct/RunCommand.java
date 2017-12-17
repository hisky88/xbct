package xbct;

// Created by DevDaily.com

import java.io.*;

public class RunCommand {

	public static String runCmd(String x) {
		// System.out.println(x);
		Runtime run = Runtime.getRuntime();
		try {
			Process pp = run.exec(WDConstants.EXEC_DIR + x);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					pp.getInputStream()));
			String line = in.readLine();
			System.out.println("Line: "+line);
			while ((line) != null) {
				return line;
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		return null;

	}
}
