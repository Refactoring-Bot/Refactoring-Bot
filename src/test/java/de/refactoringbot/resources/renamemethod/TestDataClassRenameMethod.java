package de.refactoringbot.resources.renamemethod;

public class TestDataClassRenameMethod {

	/**
	 * Method to be renamed
	 * 
	 * @param dummyParm
	 * @return
	 */
	public int getLineOfMethodToBeRenamed(boolean dummyParm) {
		return 11;
	}

	/**
	 * Method with the same name as the method to be renamed (but without parameter)
	 * 
	 * @return
	 */
	public int getLineOfMethodToBeRenamed() {
		return 20;
	}

	/**
	 * Refactoring should only change the first line in this method
	 * 
	 * @return
	 */
	public int getLineOfMethodThatCallsMethodToBeRenamed() {
		getLineOfMethodToBeRenamed(true);
		getLineOfMethodToBeRenamed();
		return 29;
	}

}
