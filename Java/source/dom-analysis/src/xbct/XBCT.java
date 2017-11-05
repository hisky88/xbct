package xbct;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class XBCT {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("*************************************************");
		System.out.println(" XBCT - Cross-Browser Compability Testing ");
		System.out.println("*************************************************");
		long start = System.currentTimeMillis(); 
		String sessid = "99lh";
		String refBrowserKey = "gc";
		if(args.length < 1){
			System.out.println("USAGE: XBCT Session_Id <Reference_Browser_Key>");
			System.exit(1);
		}else{
			sessid = args[0];
			if(args.length > 1){
				refBrowserKey = args[1];
			}
		}
		int refBrowserId = 0, refBrowserTestId=0;//default
		List<Integer> testBrowserList = new ArrayList<Integer>();
		DbUtils db = new DbUtils();
		ResultSet rs = db.getQuery("SELECT testid, browser from tests where sessid LIKE '"+sessid+"'");
		try {
			while(rs.next()){
				int tid = rs.getInt("testid");
				String browser = rs.getString("browser");
				if("ref".equals(browser)){
					refBrowserId = tid;
				}else if(refBrowserKey.equals(browser)){
					refBrowserTestId = tid;
				}else{
					testBrowserList.add(tid);
				}
			}
			/*System.out.println("Ref Browser ID="+refBrowserId);*/
			if(refBrowserId > 0){
				Integer[] tba = testBrowserList.toArray(new Integer[0]);
				int[] testBrowserArray = new int[tba.length];
				for(int i=0;i<tba.length;i++){ testBrowserArray[i]=tba[i]; }
				int[] refBTid= {refBrowserTestId};
				long initTime = System.currentTimeMillis(); 
				long varTime = System.currentTimeMillis();
				System.out.println(" - Identifying variable elements on page");
				System.out.println(" - Starting traversing the DOM trees: ");
//				new TraverseDOMTrees(refBTid, refBrowserId, true); 
				new TraverseDOMTrees(testBrowserArray,refBrowserId, false); 
				long matchTime = System.currentTimeMillis();
				System.out.println(" - Finished analysis\n\nSTATS:");
				System.out.println("Variable Elements Identification took "+ (varTime - initTime)/1000F +" secs");
				System.out.println("X-Browser Comparison took "+ (matchTime - varTime)/1000F + " secs");
			}else{
				System.out.println("ERROR: Session not found.");
			}
		} catch (SQLException e) {
			System.err.println("ERROR: Database error while running xbct.");
			System.err.println("There might be a problem with the data collected.");
			try {
				FileWriter fw = new FileWriter("sql_err.txt");
				PrintWriter pw = new PrintWriter(fw,true);
				e.printStackTrace(pw);
				pw.flush();
				fw.flush();
			} catch (IOException e1) {
			}
			
		}
	}

}
