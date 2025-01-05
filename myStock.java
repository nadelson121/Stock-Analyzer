import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

// to store the stock name and price
class stockInfo {
	private String name;
	private BigDecimal price;
	public stockInfo(String nameIn, BigDecimal priceIn) {
		name = nameIn; price = priceIn;
	}
	@Override
	public String toString() {return name + " " + price.toString();}
	public BigDecimal getPrice() {
		return price;
	}
	public String getSymbol() {
		return name;
	}
}

public class myStock {
	/* 
	 * Declared the data structures used for the database HERE.
	 * Used two data structures to store two copies of the stock information.
	 * One for O(1) retrieval, the other is used to get top K stocks in O(K) time.
	 * HashMap<String, stockInfo> and TreeSet<Map.Entry<String, stockInfo>> are recommended.
	 * The entries are not sorted in HashMap, but they are sorted in TreeSet.
	 */
	private Map<String, stockInfo> stockMap;
	private TreeSet<Map.Entry<String, stockInfo>> stocksTree;
	
	public myStock() throws IOException{
		/* 
		 * Implemented the constructor to initialize the data structures HERE.
		 * The stocks are sorted by the price in TreeSet data structure 
		 * and the compare method was overridden.
   		*/
		stockMap = new HashMap<>();
		Comparator<Map.Entry<String, stockInfo>> comparator = new Comparator<Map.Entry<String, stockInfo>>() {
		@Override
		public int compare(Map.Entry<String, stockInfo> s1, Map.Entry<String, stockInfo> s2) {
			int comparison = s2.getValue().getPrice().compareTo(s1.getValue().getPrice());
			if (comparison == 0) {
				return s1.getKey().compareTo(s2.getKey());
			}
			return comparison;
		}
		};
		stocksTree = new TreeSet<>(comparator);
	}

	public void insertOrUpdate(String symbol, stockInfo stock) {
		/* 
		 * Implemented this method to insert or update the records
		 * Made sure it could be done within O(log(n)) time.
		 * Made sure multiple copies were inserted or updated.
		 * Made sure there were no records with duplicate symbol
		 */
		//stocksTree.remove(Map.entry(symbol, stockMap.get(symbol)));
		stockMap.put(symbol, stock);
		stocksTree.add(Map.entry(symbol, stock));
	}

	public stockInfo get(String symbol) {
		/* 
		 * Implemented this method to retrive record from database in O(1) time
		 */
		return stockMap.get(symbol);
	}

	public List<Map.Entry<String, stockInfo>> top(int k) {
		/* 
		 * Implemented this method to return the stock records with top k prices in O(k) time
		 * Used iterator to retrive items in the sorted order from a data structure
		 */
		List<Map.Entry<String, stockInfo>> topStocks = new ArrayList<>();
		Iterator<Map.Entry<String, stockInfo>> iterator = stocksTree.iterator();
		for (int i = 0; i < k && iterator.hasNext(); i++) {
			topStocks.add(iterator.next());
		}
		
		return topStocks;
	}

	private stockInfo fetchStockData(String symbol) throws IOException {
		String API_KEY = "NVgtRUs3WnY5TFJZVVgxZGhybDNQd05KNTJ1eEFTTExzVTZRUW90UFM5QT0";
		String urlString = "https://api.marketdata.app/v1/stocks/quotes/" + symbol + "/?token=" + API_KEY;
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

		BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

		String jsonResponse = response.toString();
		//System.out.println(jsonResponse);
        if (jsonResponse.contains("\"s\":\"ok\"")) {
            int priceIndex = jsonResponse.indexOf("\"last\":[")+8;
			//System.out.println(jsonResponse.charAt(priceIndex));
            int priceEndIndex = jsonResponse.indexOf("]", priceIndex);
            String priceString = jsonResponse.substring(priceIndex, priceEndIndex);
			System.out.println(symbol+": "+priceString);
            //BigDecimal price = new BigDecimal(priceString);
			if (priceString != null && priceString.matches("-?\\d+(\\.\\d+)?")) {
    			BigDecimal price = new BigDecimal(priceString);
				return new stockInfo(symbol, price);
    			// Use the price
			} 
			else {
    			// Handle invalid or null price
				throw new IOException("Invalid price data received for " + symbol);
			}
        } else {
            throw new IOException("Failed to fetch stock data for " + symbol);
        }
    }

	public static void main(String[] args) throws IOException {
		// testing code
		myStock techStock = null;
		try {
			techStock = new myStock();
			BufferedReader reader;
			reader = new BufferedReader(new FileReader("./US-Tech-Symbols.txt"));
			//reader = new BufferedReader(new FileReader("./text.txt"));
			String line = reader.readLine();
			while (line != null) {
				String[] var = line.split(":");
				
				// YahooFinance API is used and make sure the lib files are included in the project build path
				stockInfo stock = null;
				try {
					System.out.println(var[0]);
					stock = techStock.fetchStockData(var[0]);
					System.out.println("Successfully retrieved info for "+var[0]);
				} catch (IOException e) {
					System.out.println("do nothing and skip the invalid stock "+var[0]);
				}
				
				// test the insertOrUpdate method when initializing the database
				if (stock != null && stock.getPrice() != null) {
					techStock.insertOrUpdate(var[0], new stockInfo(var[1], stock.getPrice()));
				}
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		int i = 1;
		System.out.println("===========Top 10 stocks===========");

		// test the top method
		for (Map.Entry<String, stockInfo> element : techStock.top(10)) {
			System.out.println("[" + i + "]" + element.getKey() + " " + element.getValue());
			i++;
		}

		// test the get method
		System.out.println("===========Stock info retrieval===========");
		System.out.println("VMW" + " " + techStock.get("VMW"));
		System.out.println("BIDU" + " " + techStock.get("BIDU"));
	}
}
