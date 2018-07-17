package refactoring;

import java.io.IOException;

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

		JSONArray issues = getSonarqubeIssues("Test:Test:master");
		
		/**
		 * The Refactoring itself
		 */
		
		for (int i = 0; i < issues.length(); i++) {
			String rule = issues.getJSONObject(i).getString("rule");
			
			if (rule.equals("squid:S1068")) {				
				VariableDeletor deletor = new VariableDeletor();
				deletor.RemoveUnusedVariable(issues.getJSONObject(i), "c://Users/Timo/Test/git/Calculator/");
			}
				
			else if(rule.equals("squid:S1161")) {
				AddOverrideAnnotation annotation = new AddOverrideAnnotation();
				annotation.addOverrideAnnotation(issues.getJSONObject(i), "c://Users/Timo/Test/git/Calculator/");
			}
			
			else if(rule.equals("squid:ModifiersOrderCheck")) {
				ReorderModifier modifier = new ReorderModifier();
				modifier.reorderModifier(issues.getJSONObject(i), "c://Users/Timo/Test/git/Calculator/");
			}
			
			else if(rule.equals("squid:S1172")) {
				RemoveUnusedMethodParameter remover = new RemoveUnusedMethodParameter();
				remover.removeUnusedMethodParameter(issues.getJSONObject(i), "c://Users/Timo/Test/git/Calculator/");
			}
		}
		
		/**
		 * execute script to commit changes and create pull request on GitHub
		 */
		
		String bash = "c:/Programme/Git/bin/bash.exe"; 
		String filename = "c:/Users/Timo/pull-request.sh"; 
		String[] command = new String[] { bash, filename }; 
		ProcessBuilder p = new ProcessBuilder(command).inheritIO();
		Process pb = p.start(); 
		pb.waitFor();
		
		
	}

}
