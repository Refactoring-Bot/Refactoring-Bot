package refactoring;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class RefactorBot {

	static List<String> issuesDone = new ArrayList<String>();

	/**
	 * Get needed Issues from Sonarqube and parse it to a JSON Array
	 * 
	 * @throws IOException
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

	public static int getNumberOfOpenPullRequests(String project) throws IOException {

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
					if (login.equals("TimoPfaff")) {
						numberOfPullRequests++;
					}

				}
			}
			return numberOfPullRequests;

		}
	}

	public static void main(String[] args) throws IOException, InterruptedException {

		Config config = new Config();
		int maxPullRequests = config.getMaxAnzahlOpenPullRequests();
		int numberOfOpenPullRequests = getNumberOfOpenPullRequests(config.githubProject);
		boolean refactoringDone = false;
		int issuePosition = 0;
		String refactoredIssue = "";
		JSONArray issues = getSonarqubeIssues(config.getSonarCloudProjectName());

		/**
		 * The Refactoring itself
		 */
		while (numberOfOpenPullRequests < maxPullRequests) {
			while (!refactoringDone && issuePosition < issues.length()) {
				String rule = issues.getJSONObject(issuePosition).getString("rule");
				if (!issuesDone.contains(issues.getJSONObject(issuePosition).getString("key"))) {

					if (rule.equals("squid:S1068")) {
						VariableDeletor deletor = new VariableDeletor();
						deletor.RemoveUnusedVariable(issues.getJSONObject(issuePosition), config.getFileLocation());
						refactoredIssue = issues.getJSONObject(issuePosition).getString("key");
						issuesDone.add(refactoredIssue);
						refactoringDone = true;
					}

					else if (rule.equals("squid:S1161")) {
						AddOverrideAnnotation annotation = new AddOverrideAnnotation();
						annotation.addOverrideAnnotation(issues.getJSONObject(issuePosition), config.getFileLocation());
						refactoredIssue = issues.getJSONObject(issuePosition).getString("key");
						issuesDone.add(refactoredIssue);
						refactoringDone = true;
					}

					else if (rule.equals("squid:ModifiersOrderCheck")) {
						ReorderModifier modifier = new ReorderModifier();
						modifier.reorderModifier(issues.getJSONObject(issuePosition), config.getFileLocation());
						refactoredIssue = issues.getJSONObject(issuePosition).getString("key");
						issuesDone.add(refactoredIssue);
						refactoringDone = true;
					}

					else if (rule.equals("squid:S1172")) {
						RemoveUnusedMethodParameter remover = new RemoveUnusedMethodParameter();
						remover.removeUnusedMethodParameter(issues.getJSONObject(issuePosition),
								config.getFileLocation());
						refactoredIssue = issues.getJSONObject(issuePosition).getString("key");
						issuesDone.add(refactoredIssue);
						refactoringDone = true;

					}
					issuePosition++;

				}
			}
			if (refactoringDone) {

				/**
				 * execute script to commit changes and create pull request on GitHub
				 */

				String bash = "c:/Programme/Git/bin/bash.exe";
				String filename = "c:/Users/Timo/pull-request.sh";
				String[] command = new String[] { bash, filename };
				ProcessBuilder p = new ProcessBuilder(command).inheritIO();
				Map<String, String> environment = p.environment();
				environment.put("location", config.getFileLocation());
				environment.put("commitMessage", "Refactoring-" + refactoredIssue);
				environment.put("branchName", "Refactoring-" + refactoredIssue);
				Process pb = p.start();
				pb.waitFor();
				refactoringDone = false;
				numberOfOpenPullRequests = getNumberOfOpenPullRequests(config.githubProject);
				System.out.println(numberOfOpenPullRequests);

			} else {
				System.out.println("Nothing to refactor found or Bot does not Support this Refactoring yet");
			}

		}

		System.out.println("Maxmimal Number of Pull Requests reached");
	}
}
