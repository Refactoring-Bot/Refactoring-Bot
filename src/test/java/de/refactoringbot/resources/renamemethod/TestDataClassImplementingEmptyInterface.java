package de.refactoringbot.resources.renamemethod;

public class TestDataClassImplementingEmptyInterface implements TestDataEmptyInterface {

	public int getLineOfMethodToBeRenamed() {
		return 5;
	}

	public class TestDataInnerClassImplementingEmptyInterface implements TestDataEmptyInterface {
		public int getLineOfMethodToBeRenamed() {
			return 10;
		}
	}
}
