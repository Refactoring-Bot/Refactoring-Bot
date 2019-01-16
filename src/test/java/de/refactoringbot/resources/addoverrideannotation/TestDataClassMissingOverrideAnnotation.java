package de.refactoringbot.resources.addoverrideannotation;

public class TestDataClassMissingOverrideAnnotation extends TestDataSuperClassMissingOverrideAnnotation {

	public int getLineOfMethodWithMissingOverrideAnnotation() {
		return 5;
	}

	@SuppressWarnings("all")
	public int getLineOfMethodWithMissingOverrideAnnotation2() {
		return 10;
	}

	/**
	 * A comment.
	 */
	public int getLineOfMethodWithMissingOverrideAnnotation3() {
		return 17;
	}

	/**
	 * A comment.
	 */
	@SuppressWarnings("all")
	public int getLineOfMethodWithMissingOverrideAnnotation4() {
		return 25;
	}
	
	/**
	 * A comment.
	 */
	@Override
	@SuppressWarnings("all")
	public int getLineOfMethodWithoutMissingOverrideAnnotation() {
		return 34;
	}

}
