package de.refactoringbot.services.main;

import org.assertj.core.api.SoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class GitServiceTest {

	@Rule
	public final ExpectedException exception = ExpectedException.none();
	
	@Test
	public void getLineNumberOfLastLineInDiffHunk() {
		// The following diffHunks are examples from PR #36 and returned by the API
		// exactly like this.
		// https://api.github.com/repos/Refactoring-Bot/Refactoring-Bot/pulls/36/comments
		String diffHunk0 = "@@ -115,30 +112,30 @@ private void testRemoveParameter(int lineNumberOfMethodWithParameterToBeRemoved,\n \t\tMethodDeclaration refactoredMethod = getMethodByName(methodName, cu);\n \n \t\t// assert that parameter has been removed from the target method\n-\t\tassertNotNull(refactoredMethod);\n-\t\tassertFalse(refactoredMethod.getParameterByName(parameterName).isPresent());\n+\t\tassertThat(refactoredMethod).isNotNull();\n+\t\tassertThat(refactoredMethod.getParameterByName(parameterName).isPresent()).isFalse();\n \n \t\t// assert that parameter has also been removed from the javadoc\n \t\tList<JavadocBlockTag> javadocBlockTags = refactoredMethod.getJavadoc().get().getBlockTags();\n \t\tfor (JavadocBlockTag javadocBlockTag : javadocBlockTags) {\n-\t\t\tassertFalse(javadocBlockTag.getTagName().equals(\"param\")\n-\t\t\t\t\t&& javadocBlockTag.getName().get().equals(parameterName));\n+\t\t\tassertThat(javadocBlockTag.getTagName().equals(\"param\")";
		Integer expectedCommentPosition0 = 121;
		String diffHunk1 = "@@ -91,9 +88,9 @@ private void testRemoveParameter(int lineNumberOfMethodWithParameterToBeRemoved,\n \t\t\t\tremoveParameterTestClass.getLineNumberOfDummyMethod(0, 0, 0), cuOriginalFile);\n \t\tMethodDeclaration originalCallerMethod = RefactoringHelper\n \t\t\t\t.getMethodByLineNumberOfMethodName(removeParameterTestClass.getLineNumberOfCaller(), cuOriginalFile);\n-\t\tassertNotNull(originalMethod);\n-\t\tassertNotNull(originalDummyMethod);\n-\t\tassertNotNull(originalCallerMethod);\n+\t\tassertThat(originalMethod).isNotNull();";
		Integer expectedCommentPosition1 = 91;
		String diffHunk2 = "@@ -53,7 +52,7 @@ public void testRenameMethod() throws Exception {\n \t\tString originalSecondMethodName = RefactoringHelper\n \t\t\t\t.getMethodByLineNumberOfMethodName(lineNumberOfSecondMethodNotToBeRenamed, cuOriginalFile)\n \t\t\t\t.getNameAsString();\n-\t\tassertEquals(originalMethodName, originalSecondMethodName);\n+\t\tassertThat(originalSecondMethodName).isEqualTo(originalMethodName);";
		Integer expectedCommentPosition2 = 55;
		String diffHunk3 = "@@ -34,18 +34,18 @@ public void testFindJavaRoots() throws IOException {\n \n \t\t// act\n \t\tList<String> javaRoots = fileService.findJavaRoots(allJavaFiles);\n-\t\n+\n \t\t// assert\n-\t\tassertTrue(javaRoots.size() > 0);\n-\t\tassertTrue(javaRoots.contains(getAbsoluteJavaRootPathOfThis()));\n+\t\tassertThat(javaRoots).size().isGreaterThan(0);\n+\t\tassertThat(javaRoots).contains(getAbsoluteJavaRootPathOfThis());";
		Integer expectedCommentPosition3 = 40;
		String diffHunk4 = "@@ -53,9 +49,9 @@ public void testCommentToIssueMappingOverrideAnnotation() throws Exception {\n \n \t\t// assert\n \t\tString refactoringOperationKey = \"Add Override Annotation\";\n-\t\tassertTrue(ruleToClassMapping.containsKey(refactoringOperationKey));\n-\t\tassertEquals(refactoringOperationKey, botIssue.getRefactoringOperation());\n-\t\tassertEquals(Integer.valueOf(5), botIssue.getLine());\n+\t\tassertThat(ruleToClassMapping).containsKey(refactoringOperationKey);";
		Integer expectedCommentPosition4 = 52;

		GitService gitService = new GitService();

		Integer actual0 = gitService.getLineNumberOfLastLineInDiffHunk(diffHunk0);
		Integer actual1 = gitService.getLineNumberOfLastLineInDiffHunk(diffHunk1);
		Integer actual2 = gitService.getLineNumberOfLastLineInDiffHunk(diffHunk2);
		Integer actual3 = gitService.getLineNumberOfLastLineInDiffHunk(diffHunk3);
		Integer actual4 = gitService.getLineNumberOfLastLineInDiffHunk(diffHunk4);

		SoftAssertions softAssertions = new SoftAssertions();
		softAssertions.assertThat(actual0).isEqualTo(expectedCommentPosition0);
		softAssertions.assertThat(actual1).isEqualTo(expectedCommentPosition1);
		softAssertions.assertThat(actual2).isEqualTo(expectedCommentPosition2);
		softAssertions.assertThat(actual3).isEqualTo(expectedCommentPosition3);
		softAssertions.assertThat(actual4).isEqualTo(expectedCommentPosition4);
		softAssertions.assertAll();
	}
	
	@Test
	public void getLineNumberOfLastLineInDiffHunkExpectException() {
		exception.expect(IllegalArgumentException.class);
		
		String invalidDiffHunk = "+ public void testRenameMethod()";
		GitService gitService = new GitService();
		
		gitService.getLineNumberOfLastLineInDiffHunk(invalidDiffHunk);
	}

}
