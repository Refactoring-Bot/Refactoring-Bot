package de.refactoringbot.resources.removecomments;

public class TestDataClassRemoveComments {

	protected int add(int a, int b) {
		// int d = a + b;
		// int e = a + b;
		// Normal comment - This one shouldn't be removed
		int c = a + b;
		/*
		 * Integer.toString(c);
		 */
		return c;
	}

	protected int commentedOutForLoop(int a) {
		// for (int i = 0; i < 10; ) {
		// System.out.println(i);
		// i++;
		// }
		return 2 * a;
	}

	protected int commentedOutLinesWithoutBracket(int a) {
		// if (a > 5)
		// System.out.println(a);
		// while (a > 5)
		// System.out.println(b);
		// for (int i = 0; i < 5; i++)
		// System.out.println(c);
		return 2 * a;
	}

	protected int commentedOutSwitch(int a) {
		// switch (a) {
		// case 0:
		// System.out.println(a);
		// break;
		// case 1:
		// System.out.println(1 + a);
		// break ;
		// default:
		// System.out.println(2 + a);
		// }
		return 2 * a;
	}

	protected int commentedOutReturn(int a) {
		// return a * 2;
		return 2 * a;
	}

	// private class InnerClass {
	// public int commentedOutMethod() {
	// return 0;
	// }
	// }

}
