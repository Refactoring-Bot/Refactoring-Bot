package de.refactoringbot.resources.refactoringhelper;

public class TestDataClassRefactoringHelper extends TestDataSuperClassRefactoringHelper {

	public static int lineNumberOfFieldDeclaration = 5;
	public static double lineNumberOfAnotherFieldDeclaration = 6;

	/**
	 * A method
	 * 
	 * @param parm
	 *            dummy parameter
	 * @return
	 */
	public static int getLineOfMethod(boolean parm) {
		return 15;
	}

	protected class InnerClass {

	}
}
