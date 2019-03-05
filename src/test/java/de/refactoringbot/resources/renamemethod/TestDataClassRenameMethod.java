package de.refactoringbot.resources.renamemethod;

public class TestDataClassRenameMethod extends TestDataSuperClassRenameMethod {

	/**
	 * Method to be renamed
	 * 
	 * @param dummyParm
	 * @return
	 */
	@Override
	public int getLineOfMethodToBeRenamed(boolean dummyParm) {
		return 12;
	}

	/**
	 * Method with the same name as the method to be renamed (but without parameter)
	 * 
	 * @return
	 */
	public int getLineOfMethodToBeRenamed() {
		return 21;
	}

	/**
	 * Refactoring should only change the first line in this method
	 * 
	 * @return
	 */
	public int getLineOfMethodThatCallsMethodToBeRenamed() {
		getLineOfMethodToBeRenamed(true);
		getLineOfMethodToBeRenamed();
		return 30;
	}

	public class TestDataInnerClassRenameMethod {
		public int getLineNumberOfCallerInInnerClass() {
			new TestDataClassRenameMethod().getLineOfMethodToBeRenamed(true);
			return 36;
		}

		public int getLineNumberOfCallerThatShouldRemainUnchanged() {
			getLineOfMethodToBeRenamed(true);
			return 42;
		}

		public int getLineOfMethodToBeRenamed(boolean dummyParm) {
			return 47;
		}
	}

}
