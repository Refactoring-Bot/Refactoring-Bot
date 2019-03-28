package de.refactoringbot.resources.renamemethod;

public class TestDataClassWithCallOfTargetMethod {

	public int getLineOfCallerMethodInDifferentFile() {
		new TestDataClassRenameMethod().getLineOfMethodToBeRenamed(true);
		return 5;
	}

}
