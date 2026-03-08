package functions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class FileOperations {
	String filePath;
	
	public FileOperations(String filePath) {
		this.filePath = filePath;
	} 
	
	public String getData() {
		File file = new File(filePath);
		
		StringBuilder res = new StringBuilder();
		
		try (BufferedReader reader = new BufferedReader(new FileReader(file))){
			String line;
			while((line = reader.readLine()) != null) {
				res.append(line + "\n");
			}
		}catch (IOException e){
			System.out.println(e);
		}
		
		String result = res.toString();
		
		return result;
	}
}
