import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.HashMap;

// Implementation of Apriori algorithm for frequent item set mining
public class Apriori {
	public static void main(String[] args) {
		// Accepts two arguments(MinSupport and FilePath) as input from CMD
		Double minSup = 0.0;
		String filePath = "";

		if (args.length < 2) {
			System.out.println("Error: Please enter Min_Support and file's path.");
			System.exit(0);
		} else {
			minSup = Double.parseDouble(args[0]);
			filePath = args[1];
		}

		// reads the file and returns the transactions in ArrayList
		ArrayList<ArrayList<String>> Transactions = new ArrayList<>();
		Transactions = readFile(filePath);

		// runs Apriori Algorithm and returns all frequent item sets
		ArrayList<HashMap<ArrayList<String>, Double>> F;
		F = runApriori(Transactions, minSup);

		// prints all frequent item sets
		for (int i = 0; i < F.size(); i++) {
			System.out.println("f" + (i + 1) + ":  " + F.get(i));
		}
	}

	private static ArrayList<HashMap<ArrayList<String>, Double>> runApriori(ArrayList<ArrayList<String>> Transactions,
			double minSup) {
		ArrayList<HashMap<ArrayList<String>, Double>> F = new ArrayList<HashMap<ArrayList<String>, Double>>();
		HashMap<ArrayList<String>, Double> f1 = new HashMap<ArrayList<String>, Double>();
		// initPass Method returns each unique item with its frequent as HashMap(key, value)
		HashMap<ArrayList<String>, Double> c1 = initPass(Transactions);
		
		// if item support is greater than MinSup, add it to f1, then f1 to F
		HashMap<ArrayList<String>, Double> CK;
		for (Entry<ArrayList<String>, Double> entry : c1.entrySet()) {
			if ((entry.getValue() / Transactions.size()) >= minSup) {
				f1.put(entry.getKey(), entry.getValue());
			}
		}
		F.add(f1);
		// loop until fk-1 is null
		for (; F.get(F.size() - 1).size() != 0;) {
			// generate new candidates
			CK = candidateGen(F.get(F.size() - 1));
			
			// count candidate repetition in transactions
			for (ArrayList<String> transaction : Transactions) {
				for (Entry<ArrayList<String>, Double> candidate : CK.entrySet()) {
					boolean isContain = true;
					for (String c : candidate.getKey()) {
						if (!transaction.contains(c)) {
							isContain = false;
							break;
						}
					}
					if (isContain) {
						CK.put(candidate.getKey(), candidate.getValue() + 1);
					}
				}
			}
			// add candidates to FK if their actual support is greater than MinSupport
			HashMap<ArrayList<String>, Double> FK = new HashMap<ArrayList<String>, Double>();
			for (Entry<ArrayList<String>, Double> c : CK.entrySet()) {
				if ((c.getValue() / Transactions.size()) >= minSup) {
					FK.put(c.getKey(), c.getValue());
				}
			}
			F.add(FK);
		}
		return F;
	}
	
	// return new candidate in HashMap(key, value)
	private static HashMap<ArrayList<String>, Double> candidateGen(HashMap<ArrayList<String>, Double> FK_1) {
		HashMap<ArrayList<String>, Double> CK = new HashMap<ArrayList<String>, Double>();
		// FK_1List is for sorting purpose
		ArrayList<ArrayList<String>> FK_1List = new ArrayList<ArrayList<String>>();
		FK_1List.addAll(FK_1.keySet());
		// sort keys(items) in ascending order
		Collections.sort(FK_1List, new Comparator<ArrayList<String>>() {
			public int compare(ArrayList<String> a1, ArrayList<String> a2) {
				for (int i = 0; i < Math.min(a1.size(), a2.size()); i++) {
					int c = a1.get(i).compareTo(a2.get(i));
					if (c != 0) {
						return c;
					}
				}
				return Integer.compare(a1.size(), a2.size());
			}
		});
		
		ArrayList<String> cArray;
		ArrayList<String> subset;
		int l = 1;
		for (int i = 0; i < FK_1.size() - 1; i++) {
			for (int j = l; j < FK_1.size(); j++) {
				cArray = new ArrayList<String>();
				boolean isEqual = true;
				//check if last element of both list are different
				if (!FK_1List.get(i).get(FK_1List.get(i).size() - 1)
						.equals(FK_1List.get(j).get(FK_1List.get(j).size() - 1))) { 
					// then check if rest of elements in both lists are equal
					for (int w = 0; w < FK_1List.get(i).size() - 1; w++) {
						if (!FK_1List.get(i).get(w).equals(FK_1List.get(j).get(w))) {
							isEqual = false;
							break;
						}
					}
					// if rest of elements in both lists are equal
					if (isEqual) {
						// join f1 and f2
						cArray.addAll(FK_1List.get(i));
						cArray.add(FK_1List.get(j).get(FK_1List.get(j).size() - 1));
						// check the subsets
						for (int q = 0; q < cArray.size(); q++) {
							subset = new ArrayList<String>(cArray);
							subset.remove(q);
							if (!FK_1.containsKey(subset)) {
								break;
							}
						}
						// add new candidate
						CK.put(cArray, new Double(0));
					}
				}
			}
			l++;
		}
		return CK;
	}

	// returns each item uniquely with its frequent as HashMap(key, value)
	private static HashMap<ArrayList<String>, Double> initPass(ArrayList<ArrayList<String>> Transactions) {
		HashMap<ArrayList<String>, Double> c1 = new HashMap<ArrayList<String>, Double>();
		ArrayList<String> itemArr;
		// iterate each item
		for (int i = 0; i < Transactions.size(); i++) {
			for (int j = 0; j < Transactions.get(i).size(); j++) {
				itemArr = new ArrayList<>();
				itemArr.add(Transactions.get(i).get(j));
				// if item does not contain in HashMap add it
				if (!c1.containsKey(itemArr)) {
					c1.put(itemArr, new Double(1));
				} else {// else count it
					c1.put(itemArr, (c1.get(itemArr) + 1));
				}
			}
		}
		return c1;
	}

	public static ArrayList<ArrayList<String>> readFile(String path) {
		ArrayList<ArrayList<String>> Transactions = new ArrayList<ArrayList<String>>();
		ArrayList<String> transaction;

		File file = new File(path);
		FileReader fr = null;
		BufferedReader br = null;
		try {
			fr = new FileReader(file);
			br = new BufferedReader(fr);
			String line;
			String[] transactionArray;

			while ((line = br.readLine()) != null) {
				line = line.replaceAll(" ", ""); // replace spaces
				transactionArray = line.split(","); // split by comma, returns as array
				transaction = new ArrayList<String>(Arrays.asList(transactionArray)); // casts from array to ArrayList
				Transactions.add(transaction);
			}
		} catch (IOException e) {
			System.out.println("Please enter a valid PATH or fileName...");
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return Transactions;

	}
}
