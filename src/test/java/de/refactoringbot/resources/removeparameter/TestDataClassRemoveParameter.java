package de.refactoringbot.resources.removeparameter;

public class TestDataClassRemoveParameter extends TestDataSuperClassRemoveParameter
		implements TestDataInterfaceRemoveParameter {

	/**
	 * @param a
	 * @param b
	 *            this is the unused parameter
	 * @param c
	 * @return
	 */
	@Override
	public int getLineOfMethodWithUnusedParameter(int a, int b, int c) {
		int result = (a + c) * 0;
		result = 14;

		// call another method with same parameter name 'b'. Unused parameter should
		// still be removed. Called method should remain unchanged
		getLineOfMethodWithSameParameterName(2);

		return result;
	}

	public int getLineNumberOfDummyMethod(int a, int b, int c) {
		return 25;
	}

	public int getLineNumberOfCaller() {
		getLineOfMethodWithUnusedParameter(1, 2, 3);
		return 29;
	}

	public int getLineOfMethodWithSameParameterName(int b) {
		return (b * 0) + 34;
	}

	public class TestDataInnerClassRemoveParameter {
		public int getLineNumberOfCallerInInnerClass() {
			new TestDataClassRemoveParameter().getLineOfMethodWithUnusedParameter(1, 2, 3);
			return 39;
		}

		public int getLineNumberOfCallerThatShouldRemainUnchanged() {
			getLineOfMethodWithUnusedParameter(1, 2, 3);
			return 44;
		}

		public int getLineOfMethodWithUnusedParameter(int a, int b, int c) {
			int result = a + b + c;
			result = 49;
			return result;
		}
		
		public int getLineOfMethodPlacedInAndAfterInnerClass(int a, int b, int c) {
			return 55;
		}
	}

	public class TestDataInnerClassWithInterfaceImpl implements TestDataInterfaceRemoveParameter {
		@Override
		public int getLineOfMethodWithUnusedParameter(int a, int b, int c) {
			return 62;
		}
	}

	public int getLineOfMethodPlacedInAndAfterInnerClass(int a, int b, int c) {
		return 67;
	}
}
