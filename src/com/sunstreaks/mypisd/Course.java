package com.sunstreaks.mypisd;

public class Course {
	
	String name;
	String teacher;
	int average;
	int[] daily;
	int[] major;
	public Course(String name1, String teacher1, int average1, int[] daily1, int[] major1)
	{
		name = name1;
		teacher = teacher1;
		average = average1;
		for(int i = 0; i< daily1.length; i++)
		{
			daily[i]=daily1[i];
		}
		for(int i = 0; i< major1.length; i++)
		{
			major[i]=major1[i];
		}
	}
	public String getName()
	{
		return name;
	}
	public String getTeacher()
	{
		return teacher;
	}
	public int getAverage()
	{
		return average;
	}
	public int[] getDaily()
	{
		return daily;
	}
	public int[] getMajor()
	{
		return major;
	}
	

}
