package de.unistuttgart.iste.refactoringbot;

import de.unistuttgart.iste.refactoringbot.refactorings.AddOverrideAnnotation;
import de.unistuttgart.iste.refactoringbot.refactorings.RemoveUnusedMethodParameter;
import de.unistuttgart.iste.refactoringbot.refactorings.RemoveUnusedVariable;
import de.unistuttgart.iste.refactoringbot.refactorings.ReorderModifier;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class RefactorBot {

	/**
	 * Gets needed issues from SonarQube using a HTTP-Get-Request and parse it to a
	 * JSON array
	 * 
	 * @throws IOException
	 * @return returns JSON array, that contains the SonarQube issues
	 */
	public static JSONArray getSonarqubeIssues(String projectName) throws IOException {

		HttpGet httpGet = new HttpGet(
				"https://sonarcloud.io/api/issues/search?projects=" + projectName + "&resolved=false&format=json");
		try (CloseableHttpClient httpClient = HttpClients.createDefault();
				CloseableHttpResponse response = httpClient.execute(httpGet);) {
			HttpEntity entity = response.getEntity();
			String json = EntityUtils.toString(entity);
			JSONObject obj = new JSONObject(json);
			System.out.println(obj);
			return obj.getJSONArray("issues");

		}
	}

	/**
	 * Gets the number of open pull-requests from the bot for a certain project
	 * 
	 * @param project:
	 *            the project that gets refactored and where the pull-requests are
	 *            created
	 * @param gitHubLoginName:
	 *            the login name of the user, that does the commits and the
	 *            pull-requests on GitHub
	 * @return number of open pull Requests
	 * @throws IOException
	 */
	public static int getNumberOfOpenPullRequests(String project, String gitHubLoginName) throws IOException {

		HttpGet httpGet = new HttpGet("https://api.github.com/repos/" + project + "/pulls");
		try (CloseableHttpClient httpClient = HttpClients.createDefault();
				CloseableHttpResponse response = httpClient.execute(httpGet);) {
			HttpEntity entity = response.getEntity();
			String json = EntityUtils.toString(entity);
			JSONArray arr = new JSONArray(json);
			int numberOfPullRequests = 0;
			if (arr.length() != 0) {
				for (int i = 0; i < arr.length(); i++) {
					JSONObject obj = arr.getJSONObject(i);
					JSONObject user = obj.getJSONObject("user");
					String login = user.getString("login");
					if (login.equals(gitHubLoginName)) {
						numberOfPullRequests++;
					}

				}
			}
			return numberOfPullRequests;

		}
	}

	public static void main(String[] args) throws IOException, InterruptedException {

		// load needed values from Properties File
		Properties properties = new Properties();
		try (FileReader in = new FileReader(args[0])) {
			properties.load(in);
		}
		int maxPullRequests = Integer.valueOf(properties.getProperty("maxNumberOfOpenPullRequests"));
		String gitHubProject = properties.getProperty("gitHubProject");
		String gitHubProjectName = gitHubProject.substring(gitHubProject.indexOf("/") + 1, gitHubProject.length());
		String listOfDoneIssuesLocation = properties.getProperty("listOfDoneIssuesLocation");
		String fileLocation = properties.getProperty("fileLocation");
		String gitHubLoginName = properties.getProperty("gitHubLoginName");

		// initialize variables
		String branchName = "";
		String refactoredIssue = "";
		String commitMessage = "";
		boolean refactoringDone = false;
		int issuePosition = 0;
		int numberOfOpenPullRequests = getNumberOfOpenPullRequests(gitHubProject, gitHubLoginName);
		String repoOwner = gitHubProject.substring(0, gitHubProject.indexOf("/"));

		// If a file with done issues already exists, load the issues into the List
		List<String> issuesDone = new ArrayList<String>();
		String issuesDoneFileName = "IssuesDone" + gitHubProjectName + ".txt";
		File issuesDoneFile = new File(listOfDoneIssuesLocation, issuesDoneFileName);
		if (issuesDoneFile.exists() && !issuesDoneFile.isDirectory()) {
			Scanner scan = new Scanner(new File(listOfDoneIssuesLocation, issuesDoneFileName));

			while (scan.hasNext()) {

				issuesDone.add(scan.next());
			}
			scan.close();
		}

		JSONArray issues = getSonarqubeIssues(properties.getProperty("sonarCloudProjectName"));

		// The Refactoring
		while (numberOfOpenPullRequests < maxPullRequests) {
			while (!refactoringDone && issuePosition < issues.length()) {
				String rule = issues.getJSONObject(issuePosition).getString("rule");
				if (!issuesDone.contains(issues.getJSONObject(issuePosition).getString("key"))) {

					switch (rule) {
					case RefactoringRules.REMOVE_UNUSED_VARIABLE:

						RemoveUnusedVariable deletor = new RemoveUnusedVariable();
						deletor.removeUnusedVariable(issues.getJSONObject(issuePosition), fileLocation);
						refactoredIssue = issues.getJSONObject(issuePosition).getString("key");
						commitMessage = deletor.getCommitMessage();
						branchName = "RefactoringBranch-" + refactoredIssue;
						issuesDone.add(refactoredIssue);
						refactoringDone = true;
						break;
					case RefactoringRules.ADD_OVERRIDE_ANNOTATION:
						AddOverrideAnnotation annotation = new AddOverrideAnnotation();
						annotation.addOverrideAnnotation(issues.getJSONObject(issuePosition), fileLocation);
						refactoredIssue = issues.getJSONObject(issuePosition).getString("key");
						commitMessage = annotation.getCommitMessage();
						branchName = "RefactoringBranch-" + refactoredIssue;

						issuesDone.add(refactoredIssue);
						refactoringDone = true;
						break;
					case RefactoringRules.REORDER_MODIFIER:
						ReorderModifier modifier = new ReorderModifier();
						modifier.reorderModifier(issues.getJSONObject(issuePosition), fileLocation);
						refactoredIssue = issues.getJSONObject(issuePosition).getString("key");
						commitMessage = modifier.getCommitMessage();
						branchName = "RefactoringBranch-" + refactoredIssue;
						issuesDone.add(refactoredIssue);
						refactoringDone = true;
						break;
					case RefactoringRules.REMOVE_UNUSED_METHOD_PARAMETER:
						RemoveUnusedMethodParameter remover = new RemoveUnusedMethodParameter();
						remover.removeUnusedMethodParameter(issues.getJSONObject(issuePosition), fileLocation);
						refactoredIssue = issues.getJSONObject(issuePosition).getString("key");
						commitMessage = remover.getCommitMessage();
						branchName = "RefactoringBranch-" + refactoredIssue;
						issuesDone.add(refactoredIssue);
						refactoringDone = true;
						break;
					}

				}
				issuePosition++;
			}
			if (refactoringDone) {

				//execute script to commit changes and create pull-request on GitHub
				String bash = properties.getProperty("bashLocation");
				String filename = properties.getProperty("pullRequestScriptLocation");
				String[] command = new String[] { bash, filename };
				ProcessBuilder p = new ProcessBuilder(command).inheritIO();
				Map<String, String> environment = p.environment();
				environment.put("location", fileLocation);
				environment.put("commitMessage", commitMessage);
				environment.put("branchName", branchName);
				environment.put("repoOwner", repoOwner);
				Process pb = p.start();
				pb.waitFor();
				refactoringDone = false;
				numberOfOpenPullRequests = getNumberOfOpenPullRequests(gitHubProject, gitHubLoginName);
				System.out.println(numberOfOpenPullRequests);

			} else {
				System.out.println("Nothing to refactor found or Bot does not support this Refactoring yet");
			}
		}

		System.out.println("Maximal Number of Pull Requests reached");
		//write done issues into file before terminating
		PrintWriter out = new PrintWriter(
				new BufferedWriter(new FileWriter(new File(listOfDoneIssuesLocation, issuesDoneFileName))));

		for (int i = 0; i < issuesDone.size(); i++) {

			out.println(issuesDone.get(i));
		}

		out.close();
	}
}
