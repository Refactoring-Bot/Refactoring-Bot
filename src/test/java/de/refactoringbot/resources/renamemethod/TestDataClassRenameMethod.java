package de.refactoringbot.resources.renamemethod;

public class TestDataClassRenameMethod extends TestDataSuperClassRenameMethod implements TestDataInterfaceRenameMethod {

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

	@Override
	public int getLineOfInterfaceMethod() {
		return 26;
	}

	/**
	 * Refactoring should only change the first line in this method
	 * 
	 * @return
	 */
	public int getLineOfMethodThatCallsMethodToBeRenamed() {
		getLineOfMethodToBeRenamed(true);
		getLineOfMethodToBeRenamed();
		return 35;
	}

	public class TestDataInnerClassRenameMethod {
		public int getLineNumberOfCallerInInnerClass() {
			new TestDataClassRenameMethod().getLineOfMethodToBeRenamed(true);
			return 42;
		}

		public int getLineNumberOfCallerThatShouldRemainUnchanged() {
			getLineOfMethodToBeRenamed(true);
			return 47;
		}

		public int getLineOfMethodToBeRenamed(boolean dummyParm) {
			return 52;
		}
		
		public int getLineOfMethodPlacedInAndAfterInnerClass() {
			return 56;
		}
	}

	public class TestDataInnerClassWithInterfaceImpl implements TestDataInterfaceRenameMethod {
		@Override
		public int getLineOfInterfaceMethod() {
			return 63;
		}
	}
	
	public int getLineOfMethodPlacedInAndAfterInnerClass() {
		return 68;
	}

}
