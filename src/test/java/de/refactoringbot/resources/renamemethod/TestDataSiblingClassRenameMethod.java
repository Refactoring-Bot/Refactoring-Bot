package de.refactoringbot.resources.renamemethod;

public class TestDataSiblingClassRenameMethod extends TestDataSuperClassRenameMethod {

	@Override
	public int getLineOfMethodToBeRenamed(boolean dummyParm) {
		return 6;
	}

	public int getLineNumberOfCallerInSiblingClass() {
		getLineOfMethodToBeRenamed(true);
		return 10;
	}

}
