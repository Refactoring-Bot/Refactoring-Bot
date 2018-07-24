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

	public static void main(String[] args) throws IOException, InterruptedException {
		Config config = new Config();
		int maxPullRequests = config.getMaxAnzahlOpenPullRequests();
		boolean refactoringDone = false;
		int issuePosition = 0;
		JSONArray issues = getSonarqubeIssues("Test:Test:master");

		/**
		 * The Refactoring itself
		 */
		while (!refactoringDone && issuePosition < issues.length()) {
			String rule = issues.getJSONObject(issuePosition).getString("rule");
			if (!issuesDone.contains(issues.getJSONObject(issuePosition).getString("key"))) {

				if (rule.equals("squid:S1068")) {
					VariableDeletor deletor = new VariableDeletor();
					deletor.RemoveUnusedVariable(issues.getJSONObject(issuePosition), "c://Users/Timo/Test/git/Calculator/");
					issuesDone.add(issues.getJSONObject(issuePosition).getString("key"));
					refactoringDone = true;
				}

				else if (rule.equals("squid:S1161")) {
					AddOverrideAnnotation annotation = new AddOverrideAnnotation();
					annotation.addOverrideAnnotation(issues.getJSONObject(issuePosition), "c://Users/Timo/Test/git/Calculator/");
					issuesDone.add(issues.getJSONObject(issuePosition).getString("key"));
					refactoringDone = true;
				}

				else if (rule.equals("squid:ModifiersOrderCheck")) {
					ReorderModifier modifier = new ReorderModifier();
					modifier.reorderModifier(issues.getJSONObject(issuePosition), "c://Users/Timo/Test/git/Calculator/");
					issuesDone.add(issues.getJSONObject(issuePosition).getString("key"));
					refactoringDone = true;
				}

				else if (rule.equals("squid:S1172")) {
					RemoveUnusedMethodParameter remover = new RemoveUnusedMethodParameter();
					remover.removeUnusedMethodParameter(issues.getJSONObject(issuePosition), "c://Users/Timo/Test/git/Calculator/");
					issuesDone.add(issues.getJSONObject(issuePosition).getString("key"));
					refactoringDone = true;

				}
				issuePosition++;

			}
		}
		/**
		 * execute script to commit changes and create pull request on GitHub
		 */
		
		 String bash = "c:/Programme/Git/bin/bash.exe";
		 String filename = "c:/Users/Timo/pull-request.sh";
		 String[] command = new String[] { bash,filename};
		 ProcessBuilder p = new ProcessBuilder(command).inheritIO();
		 Map<String, String> environment = p.environment();
		 environment.put("location", "c:/Users/Timo/Test/git/Calculator");
		 Process pb = p.start(); 
		 pb.waitFor();
		 

	}

}
