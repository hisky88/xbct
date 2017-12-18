package xbct;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class TraverseDOMTrees{
	private HashMap[] map;
	private DbUtils db;
	private Node[][] set;
	private boolean variable_check;
	public TraverseDOMTrees(int[] browserTestId,int refBrowserTestId, boolean variable_check) throws SQLException{
		db=new DbUtils();
		this.variable_check = variable_check;
		initializeMap(browserTestId);
		createAllNodeObjects(refBrowserTestId,browserTestId); 
	}
	
	@SuppressWarnings("null")
	private Node[] createListToPutInArray(int testid) throws SQLException{
		 String filter = null;
		 ResultSet currentId=this.db.getQuery("SELECT * FROM tests where testid="+testid);
		 currentId.first();
		 String currentBrowser = currentId.getString(3);
		 if(currentBrowser.equals("gc")||currentBrowser.equals("ref")){
			 filter = "Chrome";
		 }else if(currentBrowser.equals("ff")){
			 filter = "Firefox";
		 }else{
			 filter = "IE";
		 }	
		 ResultSet rs1=this.db.getQuery("SELECT MAX(id) FROM domdata where testid="+testid);
		 rs1.first();
		 int row=rs1.getInt(1);//rs.getRow();
		 ResultSet rs=this.db.getQuery("SELECT * FROM domdata where testid="+testid);
		 rs.first();
		 int rowCount = 0;
	     try {
	        rs.last();
	           rowCount = rs.getRow();
	      } catch (Exception e) {
	        e.printStackTrace();
         }
	     rs.first();
		 Node[] nodelist=new Node[row+1];
		 String[] childlist=new String[row+1];
		 int idcount = 1;
		 do{
				 boolean clickable=false;
				 boolean visible=false;
				 if(rs.getInt(11)==1)
					 clickable=true;
				 if(rs.getInt(12)==1)
					 visible=true;
				 nodelist[rs.getInt(4)]=new Node(rs.getInt(2),rs.getString(1), rs.getString(15) , rs.getString(17), rs.getString(5), Integer.parseInt(rs.getString(4)), Integer.parseInt(rs.getString(6)), Integer.parseInt(rs.getString(7)), Integer.parseInt(rs.getString(8)), Integer.parseInt(rs.getString(9)), Integer.parseInt(rs.getString(10)), Integer.parseInt(rs.getString(16)), clickable, visible, nodelist[rs.getInt(13)]);
 				 if((nodelist[rs.getInt(4)].getX0()==nodelist[rs.getInt(4)].getX1())||(nodelist[rs.getInt(4)].getY0()==nodelist[rs.getInt(4)].getY1())){
					 nodelist[rs.getInt(4)].setVisible(false);
				 }
				if(rs.getInt(18)==1){
					nodelist[rs.getInt(4)].setNonDet();
				}
				 childlist[rs.getInt(4)]=rs.getString(14);
				 System.out.println("Dom"+"("+idcount+")"+": " + childlist[rs.getInt(4)]);

				 idcount = idcount + 1;
				}while(rs.next());
			 for(int i=1;i<childlist.length;i++){
				 if(childlist[i] != null){
					String temp[]=childlist[i].split(" ");
					for(String t:temp){
						String tempNos[]=t.split(",");
						for(String tNo:tempNos){
							
							try{
								nodelist[i].addChild(nodelist[Integer.parseInt(tNo)]);
//								System.out.println("Integer.parseInt(tNo): " + Integer.parseInt(tNo));
//								System.out.println("nodelist[Integer.parseInt(tNo)]: " + nodelist[Integer.parseInt(tNo)]);
//								System.out.println("nodelist[i]: " + nodelist[i]);
							}
							catch (Exception e) {
							}
						}
					}
				 }
			 }	
			 return nodelist;
	}
	private void createAllNodeObjects(int refBrowserTestId,int[] browserTestId) throws SQLException {
		//First we will have refbrowser and then all browsertests
		//Populate the tree
		set[0]=createListToPutInArray(refBrowserTestId);
		//Adding all browsers to our set
		 for(int i=0; i<browserTestId.length;i++){
			set[i+1]=createListToPutInArray(browserTestId[i]);
		}
		 traverseTree(browserTestId,refBrowserTestId);
	}
	
	public void traverseTree(int[] browserTestId, int refBrowserTestId) throws SQLException{
		for(int i=1;i<=browserTestId.length;i++){ // Ln3
			ArrayList<Node> worklist = new ArrayList<Node>(); 
			Node rootDOM0=set[0][1];
			worklist.addAll(rootDOM0.getChildren()); // Ln4
			while(worklist.size()>0){	// Ln5
				Node node=worklist.remove(0); //Ln6
				//Ignore script tags because they do not manifest itself as an element that is visibile
				if(node.getName().equals("SCRIPT")){
					continue;
				}
				Node bestMatch=null; //Ln7
				double bestMI=0; //Ln8
				for(int j=1;j<set[i].length;j++){
					Node nodej=set[i][j];
					
					if(nodej == null || nodej.isMapped()){
						continue;
					}else if((node.getX0()==node.getX1())||(node.getY0()==node.getY1()) || (nodej.getX0()==nodej.getX1()) || (nodej.getY0()==nodej.getY1())){
						continue;
					}else{
						double mI=computeMatchIndex(node,nodej);
						
						if(mI==1){
							bestMI=mI;
							bestMatch=nodej;
							//remove node here
							nodej.setMapped();
							break;
						}
						else if(bestMI<mI){
							bestMatch=nodej;
							bestMI=mI;
						}
					}
				}
				if(bestMI>0.5){
					map[i-1].put(node, bestMatch);
				}
				worklist.addAll(node.getChildren());					
			}
			removeExtraNodes(i);
		}
		
//		System.out.println(map[0]);
		//Insert the map to database
		insertMapToDB(map,browserTestId,refBrowserTestId);
	}
	private void removeExtraNodes(int i){
		ArrayList<Node> toBeDel=new ArrayList<Node>();
		Collection<Node> valueList = map[i-1].values();
		Set checkDup=new HashSet<Node>();
		Iterator<Node> itr = valueList.iterator();
		while(itr.hasNext()){
			Node curr=itr.next();
			boolean doesntHave=checkDup.add(curr);
			if(!doesntHave){
				Iterator it = map[i-1].entrySet().iterator();
				ArrayList<Node> keysForVal=new ArrayList<Node>();
				while (it.hasNext()) {
			        Map.Entry pairs = (Map.Entry)it.next();
			       
			        if(pairs.getValue()==curr){
			        	keysForVal.add((Node) pairs.getKey());
			        }       
			    }
			    double bestMI=0;
		        int bestMIlocation=-1;
		        int y=0;
		        for(Node x:keysForVal){
		        	double currMI=computeMatchIndex(x, keysForVal.get(y));
		        	if(currMI>bestMI){
		        		bestMI=currMI;
		        		bestMIlocation=y;
		        	}
		        	y++;
		        }
		        for(int k=0;k<keysForVal.size();k++){
		        	if(k!=bestMIlocation){
		        		toBeDel.add(keysForVal.get(k));
		        	}
		        }

			}
		}
		for(int l=0;l<toBeDel.size();l++)
			map[i-1].remove(toBeDel.get(l));
	}
	private void insertMapToDB(HashMap[] map,int[]browserTestId, int refBrowserTestId) throws SQLException{
		for(int i=0;i<map.length;i++){
			String mapping=map[i].toString();
			int testId1=refBrowserTestId;
			int testId2=browserTestId[i];
			this.db.insQuery("INSERT INTO map (testid1,testid2,mapping) VALUES("+testId1+","+testId2+",'"+mapping+"')");
		}
		if(this.variable_check){
			new VariableElementIdentifier(browserTestId, refBrowserTestId, map);
		}else{
			new CrossBrowserTest(browserTestId, refBrowserTestId,map);
		}
	}

	
	private double computeMatchIndex(Node a, Node b) {
		double rho=0;
		double alpha=0.9;
		if((!a.getDomid().equals("") && !b.getDomid().equals("")) && (a.getDomid().equals(b.getDomid()))){
				rho=1;
		}else if(!a.getDomid().equals(b.getDomid())){
			//pass through
		}
		else if(a.getXpath().equals(b.getXpath())){
			rho=1;
		}
		else if(a.getName().equals(b.getName())){
			LevenshteinDistance l=new LevenshteinDistance();
			double rho1=1-l.computeLevenshteinDistance(a.getXpath(), b.getXpath())/Math.max(a.getXpath().length(), b.getXpath().length());
			double rho2=0;
			if((Math.abs(a.getX0()-b.getX0())<=5) && (Math.abs(a.getX1()-b.getX1())<=5) && (Math.abs(a.getY0()-b.getY0())<=5) && (Math.abs(a.getY1()-b.getY1())<=5)){
				rho2+=1;
			}
			if(a.isClickable()==b.isClickable()){
				rho2+=1;
			}
			if(a.isVisible()==b.isVisible()){
				rho2+=1;
			}
			if(a.getZindex()==b.getZindex()){
				rho2+=1;
			}
			if(a.getContenthash().equals(b.getContenthash())){
				rho2+=1;
			}
			rho2=rho2/5;
			rho=rho1*alpha+rho2*(1-alpha);
		}
		return rho;
	}

	public void initializeMap(int[] browserTestId){
		map=new HashMap[browserTestId.length];
		//Also initialize the hash set. Initialize it with a spot for refbrowser therefore all browserTestId+1
		set=new Node[browserTestId.length+1][];
		for(int i=0;i<browserTestId.length;i++)
		{
			map[i]=new HashMap<Node,Node>();
		}
	}
	
}