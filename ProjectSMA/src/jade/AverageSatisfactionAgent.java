package jade;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
* This classis is made to calculate the average of the <b>Satisfaction Indicator</b> for every agent in our system
*/

public class AverageSatisfactionAgent {

	public static void main(String[] args) {

		/**
		* The file where to store the <b>Satisfaction values</b> for every agent for each time this value has been changed 
		*/
		File folder = new File("logSatisfaction");
		File[] listOfFiles = folder.listFiles();

		if (listOfFiles != null) {

			for (File file : listOfFiles) {
				if (file.isFile()) {
					double averageAgent = 0;
					int total = 0;
					try {
						
						BufferedReader br = new BufferedReader(new FileReader(file));
						String readLine = "";
						while ((readLine = br.readLine()) != null) {
							averageAgent = averageAgent + Double.parseDouble(readLine);
							total++;
						}
						br.close();
						
						System.out.println("Average of satisfation for agent "+ file.getName()+" = "+ averageAgent/total);

					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}

				}
				else {
					System.out.println("No logs agents detected...");
				}
			}
		} 
	}

}
