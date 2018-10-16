package de.unistuttgart.iste.refactoringbot;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class Main {

	public static void main(String[] args) throws IOException, InterruptedException {
		String propertiesFilePath = args[0];
		RefactoringProperties properties = RefactoringProperties.createFromPropertiesFile(propertiesFilePath);
		boolean refactoringDone = false;
		int numberOfOpenPullRequests = getNumberOfOpenPullRequests(properties.getTargetProjectGitHubPath(), properties.getGitHubLoginName());
		JSONArray sonarQubeIssues = getSonarqubeIssues(properties.getSonarCloudProjectKey());
		HashMap<String, Class<?>> sonarQubeRuleToRefactoringClassMapping = RefactoringRules.getSonarQubeRuleToRefactoringClassMapping();
		List<String> issuesDone = readDoneIssuesFromFile(properties);
		
		for (int i = 0; i < sonarQubeIssues.length(); i++) {
			JSONObject currentSonarQubeIssue = sonarQubeIssues.getJSONObject(i);
			
			if (numberOfOpenPullRequests >= properties.getMaxNumberOfOpenPullRequests()) {
				System.out.println("Maximal Number of Pull Requests reached");
				break;
			}
			
			if (issuesDone.contains(currentSonarQubeIssue.getString("key"))) {
				// this issue has already been dealt with
				continue;
			}
			
			Class<?> refactoringImplementationClazz = sonarQubeRuleToRefactoringClassMapping.get(currentSonarQubeIssue.getString("rule"));
			if (refactoringImplementationClazz != null) {
				try {
					Constructor<?> ctor = refactoringImplementationClazz.getConstructor();
					Object object = ctor.newInstance(new Object[] {});
					refactoringImplementationClazz.getMethod("performRefactoring", JSONObject.class, String.class).invoke(object, currentSonarQubeIssue, properties.getTargetProjectFileLocation());
					String commitMessage = (String) refactoringImplementationClazz.getMethod("getCommitMessage").invoke(object);
					String refactoredIssue = currentSonarQubeIssue.getString("key");
					String branchName = "RefactoringBranch-" + refactoredIssue;
					refactoringDone = true;
					System.out.println("Refactoring performed. Commit message: " + commitMessage + ", Refactored issue (key): " + refactoredIssue);
					
					commitAndCreatePullRequest(properties, branchName, commitMessage);
					issuesDone.add(refactoredIssue);
					numberOfOpenPullRequests = getNumberOfOpenPullRequests(properties.getTargetProjectGitHubPath(), properties.getGitHubLoginName());
				} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		if (!refactoringDone) {
			System.out.println("Nothing to refactor found or Bot does not support this Refactoring yet");
		}
		
		writeDoneIssuesToFile(properties, issuesDone);
	}

	/**
	 * 
	 * @param properties
	 * @return
	 * @throws FileNotFoundException
	 */
	private static List<String> readDoneIssuesFromFile(RefactoringProperties properties) throws FileNotFoundException {
		List<String> issuesDone = new ArrayList<String>();
		File issuesDoneFile = new File(properties.getProcessedSonarIssuesFileLocation(), properties.getProcessedSonarIssuesFileName());
		if (issuesDoneFile.exists() && !issuesDoneFile.isDirectory()) {
			Scanner scan = new Scanner(new File(properties.getProcessedSonarIssuesFileLocation(), properties.getProcessedSonarIssuesFileName()));
			while (scan.hasNext()) {
				issuesDone.add(scan.next());
			}
			scan.close();
		}
		return issuesDone;
	}

	/**
	 * 
	 * @param properties
	 * @param issuesDone
	 * @throws IOException
	 */
	private static void writeDoneIssuesToFile(RefactoringProperties properties, List<String> issuesDone) throws IOException {
		PrintWriter out = new PrintWriter(
				new BufferedWriter(new FileWriter(new File(properties.getProcessedSonarIssuesFileLocation(), properties.getProcessedSonarIssuesFileName()))));
		for (int i = 0; i < issuesDone.size(); i++) {
			out.println(issuesDone.get(i));
		}
		out.close();
	}

	/**
	 * 
	 * @param properties
	 * @param branchName
	 * @param commitMessage
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private static void commitAndCreatePullRequest(RefactoringProperties properties, String branchName,
			String commitMessage) throws IOException, InterruptedException {
		String bash = properties.getBashLocation();
		String filename = properties.getPullRequestScriptFileLocation();
		String[] command = new String[] { bash, filename };
		ProcessBuilder p = new ProcessBuilder(command).inheritIO();
		Map<String, String> environment = p.environment();
		environment.put("location", properties.getTargetProjectFileLocation());
		environment.put("commitMessage", commitMessage);
		environment.put("branchName", branchName);
		environment.put("repoOwner", properties.getTargetProjectGitHubOwner());
		Process pb = p.start();
		pb.waitFor();
	}
	
	/**
	 * Gets needed issues from SonarQube using a HTTP-Get-Request and parse it to a
	 * JSON array
	 * 
	 * @throws IOException
	 * @return returns JSON array, that contains the SonarQube issues
	 */
	private static JSONArray getSonarqubeIssues(String projectName) throws IOException {

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
	private static int getNumberOfOpenPullRequests(String project, String gitHubLoginName) throws IOException {

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
	
}
