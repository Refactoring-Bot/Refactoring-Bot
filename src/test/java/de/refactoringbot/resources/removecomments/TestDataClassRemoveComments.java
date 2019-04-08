package de.refactoringbot.resources.removecomments;

public class TestDataClassRemoveComments {

	public int add(int a, int b) {
		// int d = a + b;
		// int e = a + b;
		// Normal comment - This one shouldn't be removed
		int c = a + b;
		/*
		 * Integer.toString(c); 
		 */
		return c;
	}
}
