package com.sunstreaks.mypisd.net;
import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class Parser {
	
	public static final String LOGIN_FAILURE_TITLE = "myPISD - Login";
	public static final String GRADEBOOK_BAD_USERNAME = "NotSet";
	
	/*
	 * returns value of pageUniqueId from htm
	 * String must contain the following block:
	 * <input type="hidden" name="PageUniqueId" id="PageUniqueId" value="8bdc977f-ccaf-4a18-b4dd-20d1406fad6a" />
	 */
	
	public static String pageUniqueId (String html) {
		Elements inputElements = Jsoup.parse(html).getElementsByTag("input");
	
		for (Element e: inputElements)
			if (e.attr("name").equals("PageUniqueId"))
				return e.attr("value");

		return null;
	}
	
	
	/*
	 * must take in html for [Domain].loginAddress
	 */
	public static boolean accessGrantedEditure (String html) {
		Element doc = Jsoup.parse(html);
		String title = doc.getElementsByTag("title").text();
		return !title.equals(LOGIN_FAILURE_TITLE) ? true : false;
	}
	
	/*
	 * must take in html for "https://parentviewer.pisd.edu/EP/PIV_Passthrough.aspx?action=trans&uT=" + userType + "&uID=" + uID
	 */
	public static boolean accessGrantedGradebook (String[] gradebookCredentials) {
		System.out.println(Arrays.deepToString(gradebookCredentials));
		return  ! (gradebookCredentials[0].equals(GRADEBOOK_BAD_USERNAME));
	}
	
	
	public static String[] getGradebookCredentials (String html) {
		Element doc = Jsoup.parse(html);
		Elements userIdElements = doc.getElementsByAttributeValue("name", "userId");
		Elements passwords = doc.getElementsByAttributeValue("name", "password");
		String userId = userIdElements.attr("value");
		String password = passwords.attr("value");
		return new String[] {userId, password};
	}

	/*
	 * must take html from [Domain].portalAddress or [Domain].portalAddress/myclasses
	 */
	public static String[] passthroughCredentials (String html) {
		Element doc = Jsoup.parse(html);
		String uT = doc.getElementsByAttributeValue("name","uT").attr("value");
		String uID = doc.getElementsByAttributeValue("name","uID").attr("value");
		return new String[] {uT, uID};
	}
	
	public static int studentIdPinnacle (ArrayList<String> cookies) {
		for (String c : cookies)
			if (c.substring(0,c.indexOf('=')).equals("PinnacleWeb.StudentId"))
				return Integer.parseInt(c.substring(c.indexOf('=')+1));

		return -1;
	}
	
	public static JSONArray detailedReport (String html) throws JSONException {
		Element doc = Jsoup.parse(html);
//		System.out.println(html);
		Element assignments = doc.getElementsByAttributeValue("id", "Assignments").get(0);
		Elements tableRows = assignments.getElementsByTag("tbody").get(0).getElementsByTag("tr");
		
		JSONArray grades = new JSONArray();
		
		for (Element tr : tableRows) {
			JSONObject assignment = new JSONObject();
			
//			I don't want the numbers!
//				//stores the displayed index of the assignment (starting from 1) for (optional) chronological display.
//				assignment.put("Number", Integer.parseInt(tr.getElementsByTag("th").text()));
			
			Elements columns = tr.getElementsByTag("td");
			

			for (int i = 0; i < columns.size(); i++) {
				String value = columns.get(i).text();
				// do not store empty values!
				if (value.equals(""))
					continue;
				// first try to cast as int.
				try {
					assignment.putOpt(assignmentTableHeader(i), Integer.parseInt(value));
					// if not int, try double
				} catch (NumberFormatException e) {
					try {
						assignment.putOpt(assignmentTableHeader(i), Double.parseDouble(value));
						// if not double, use string
					} catch (NumberFormatException f) {
						assignment.putOpt(assignmentTableHeader(i), value);
					}
				}
			}
			grades.put(assignment);
		}
//		System.out.println((grades));
		return grades;
	}
	
	/*
	 * Reads assignment view page and returns teacher name.
	 * 
	 * Parses from this table:
	 * 
	 * <table id='classStandardInfo'> <tbody> <tr>  
	 * <td>       <div class='classInfoHeader'>Kapur, Sidharth (226344)</div>2013-08-29   <td>    
	 * <table>    
	 * 		<tr>      <th style='width:1%'>Course:</th>      <td><a href='javascript:ClassDetails.getClassDetails(2976981);' id='ClassTitle'>CHEM  AP(00)</a></td></tr>    
	 * 		<tr>      <th>Term:</th>      <td>1st Six Weeks</td>     </tr>
	 * 		<tr>      <th>Teacher:</th>      <td><a href="mailto:Nicole.Lyssy@pisd.edu" title="Nicole.Lyssy@pisd.edu">Lyssy, Carol</a></td>     </tr>
	 * </table>
	 * <td>  </tr> </tbody></table>
	 */
	public static String[] teacher (String html)  {
		Element doc = Jsoup.parse(html);
		Element classStandardInfo = doc.getElementById("classStandardInfo");
		// teacher is the third row in this table
		Element teacher = classStandardInfo.getElementsByTag("table").get(0).getElementsByTag("tr").get(3).getElementsByTag("td").get(0);
//		System.out.println(teacher);
		String email = "";
		try {
			email = teacher.getElementsByTag("a").get(0).attr("title");
		} catch (IndexOutOfBoundsException e) {
			// Senior release teacher have NO email. The <a> tag does not exist.
		}
		String teacherName = teacher.text();
		return new String[] {teacherName, email};
	}
	
	public static JSONArray termCategoryGrades (String html) throws JSONException {
		JSONArray termCategoryGrades = new JSONArray();
		
		Element doc = Jsoup.parse(html);
		Element categoryTable = doc.getElementById("Category");
		Elements rows = categoryTable.getElementsByTag("tbody").get(0).getElementsByTag("tr");
		for (Element row : rows) {
			JSONObject category = new JSONObject();
			Elements columns = row.getElementsByTag("td");
			for (int i = 0; i < columns.size(); i++) {
				
				String value = columns.get(i).text();
				// do not store empty values!
				if (value.equals(""))
					continue;
				// first try to cast as int.
				try {
					category.putOpt(categoryTableHeader(i), Integer.parseInt(value));
					// if not int, try double
				} catch (NumberFormatException e) {
					try {
						category.putOpt(categoryTableHeader(i), Double.parseDouble(value));
						// if not double, use string
					} catch (NumberFormatException f) {
						category.putOpt(categoryTableHeader(i), value);
					}
				}
			}
			termCategoryGrades.put(category);
		}
		
		return termCategoryGrades;
	}
	
	/*
	 * We should probably replace this with an Enum
	 */
	public static String assignmentTableHeader (int column) {
		// starts at one (because Number is a th, not a td)
		switch (column+1) {
		// if we return null, the machine won't put it
		case 0:
			return null;
			//return "Number";
		case 1:
			return "Description";
		case 2:
			return "Due Date";
		case 3:
			return "Category";
		case 4:
			return "Grade";
		case 5:
			return "Max";
		case 6:
			return "Letter";
		case 7:
			return "Comments";
		default:
			return null;
		}
	}
	
	public static String categoryTableHeader (int column) {
		switch (column) {
		case 0:
			 return "Category";
		case 1:
			return "Weight";
		case 2:
        	return "Points / Max pts.";
		case 3:
			return "Percent";
		case 4:
			return "Letter";
		default:
			return null;
		}
	}
	
}
