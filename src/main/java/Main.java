import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;

/*
*@author Dhruv Patel TopBloc Coding challenge 11/22
*/
public class Main {
	private static List<String> excelFiles = new ArrayList<String>() {
		{
		add("Student Info.xlsx");
		add("Test Scores.xlsx");
		add("Test Retake Scores.xlsx");
		}
	};
	private static final String SERVER_PATH = "http://3.236.235.205:5000/challenge";

	public static void main(String[] args) throws IOException {
		// Set with all students in file
		Set<Student> studentSet = scanStudents(excelFiles.get(0));
		
		// Map for mapping student id to test scores
		HashMap<String, Integer> studentToScore = new HashMap<String, Integer>();
		
		// Scan test score and fill map
		scanTestScores(excelFiles.get(1), studentToScore);
		scanTestScores(excelFiles.get(2), studentToScore);
		
		// Class average of test
		int classAverage = calcClassAverage(studentToScore);
		
		
		// List with all female CS students
		ArrayList<String> femaleCSList = getFemaleCSStudents(studentSet);
		
		// POST request to path
		postRequest(SERVER_PATH, classAverage, femaleCSList);


		// TODO Auto-generated method stub

	}

	public static HashSet<Student> scanStudents(String fileName) throws IOException {
		HashSet<Student> students = new HashSet<Student>();
		File excelFile = new File(fileName);
		FileInputStream inputStream = new FileInputStream(excelFile);
		XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
		Iterator<Sheet> sheetIterator = workbook.sheetIterator();
		List<String> headers = new ArrayList();

		boolean firstRow = true;
		
		// Scan the excel file and add Student objects to set
		System.out.println("Scanning excel file for student info");
		while (sheetIterator.hasNext()) {
			Sheet currSheet = sheetIterator.next();
			Iterator<Row> rowIterator = currSheet.rowIterator();
			while (rowIterator.hasNext()) {
				Row currRow = rowIterator.next();
				Iterator<Cell> cellIterator = currRow.cellIterator();
				int index = 0;
				Student student = new Student();

				while (cellIterator.hasNext()) {

					if (firstRow) {
						Cell currCell = cellIterator.next();
						headers.add(currCell.toString()); // Add all headers to list
					} else {
						Cell currCell = cellIterator.next();
						switch (headers.get(index)) {
						case "studentId":
							student.setId(String.valueOf((int) currCell.getNumericCellValue())); // Set students ID
							index++;
							break;
						case "major":
							student.setMajor(currCell.toString()); // Set students major
							index++;
							break;
						case "gender":
							student.setGender(currCell.toString().charAt(0)); // Set students gender
							index++;
							break;
						}

					}
					if (!firstRow) { // Don't want to execute on header row
						students.add(student); // Add student to set 
					}

				}

				firstRow = false;

			}

		}
		System.out.println("Complete");

		return students; // Set with all students 

	}

	public static void scanTestScores(String fileName, HashMap<String, Integer> studentToScore) throws IOException {
		HashMap<String, Integer> scoreMap = studentToScore;
		File excelFile = new File(fileName);
		FileInputStream inputStream = new FileInputStream(excelFile);
		XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
		Iterator<Sheet> sheetIterator = workbook.sheetIterator();
		List<String> infoList = new ArrayList();

		boolean firstRow = true;
		
		// Scan the excel file and fill map (id => score)
		System.out.println("Scanning excel for for student test scores");
		while (sheetIterator.hasNext()) {
			Sheet currSheet = sheetIterator.next();
			Iterator<Row> rowIterator = currSheet.rowIterator();
			List<String> headers = new ArrayList();
			
			while (rowIterator.hasNext()) {
				Row currRow = rowIterator.next();
				Iterator<Cell> cellIterator = currRow.cellIterator();
				int index = 0;
				String id = null;
				int score = 0;
				
				while (cellIterator.hasNext()) {
					if (firstRow) {
						Cell currCell = cellIterator.next();
						headers.add(currCell.toString()); // Add all headers to list
					} else {
						Cell currCell = cellIterator.next();
						switch (headers.get(index)) {
						case "studentId":
							id = currCell.toString();  // Student ID
							index++;
							break;
						case "score":
							score = (int) Double.parseDouble(currCell.toString()); // Student score
							index++;
							break;
						}
					}

				}
				if (!firstRow) { // Don't want to execute on header row
					if (!scoreMap.containsKey(id)) {
						scoreMap.put(id, score);  // If map doesn't contain student id, add it
					} else if (scoreMap.containsKey(id) && scoreMap.get(id) < score) {
						scoreMap.put(id, score); // If it does contain student id and old score is lower, update it
					}
				}

				firstRow = false;

			}
		}
		System.out.println("Complete");

	}

	public static int calcClassAverage(HashMap<String, Integer> studentToScore) {
		// Calc class average of test grade
		double classAverage = 0;
		
		System.out.println("Calculating class average for test");
		for (int x : studentToScore.values()) {
			classAverage += x;
		}
		
		int mapSize = studentToScore.size();
		classAverage /= mapSize;
		System.out.println("Complete");
		
		return (int) Math.round(classAverage); // Round to nearest whole number and return average
	}

	public static ArrayList<String> getFemaleCSStudents(Set<Student> studentSet) {
		// List with all female CS students
		ArrayList<String> list = new ArrayList<String>();
		
		System.out.println("Scanning for female CS students");
		for (Student x : studentSet) {
			if (x.getGender() == 'F' && x.getMajor().equalsIgnoreCase("Computer Science")) {
				list.add(x.getId());
			}
		}
		
		Collections.sort(list); // Sort the list
		System.out.println("Complete");
		return list; // List with female CS students
	}

	public static void postRequest(String path, int avg, ArrayList<String> ids) throws IOException {
		// JSON object to be sent
		JSONObject json = new JSONObject();
		json.put("id", "pdhruv2477@gmail.com");
		json.put("name", "Dhruv Patel");
		json.put("average", avg);
		json.put("studentIds", ids);

		CloseableHttpClient httpClient = HttpClientBuilder.create().build();

		try {
			HttpPost request = new HttpPost(path);
			StringEntity params = new StringEntity(json.toString());
			request.setHeader("content-type", "application/json");
			request.setEntity(params);
			System.out.println("Sending post request");
			HttpResponse httpResponse = httpClient.execute(request);
			
			if(httpResponse.getStatusLine().getStatusCode() == 200) {
				System.out.println("POST success");
			}else {
				System.out.println("POST error :" + httpResponse.getStatusLine().getStatusCode());
			}

			HttpEntity entity = httpResponse.getEntity();
			String result = EntityUtils.toString(entity);

			System.out.println(result); // Print response for verification

		} catch (Exception ex) {
			ex.printStackTrace(); 
		} finally {
			httpClient.close();
		}

	}
}
