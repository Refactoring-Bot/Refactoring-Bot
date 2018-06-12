package refactoring;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;

public class RefactorBot {

	public static void main(String[] args) throws IOException {

		String path = "";
		HttpGet httpGet = new HttpGet("http://localhost:9000/api/issues/search?resolved=false&format=json");
		try (CloseableHttpClient httpClient = HttpClients.createDefault();
				CloseableHttpResponse response = httpClient.execute(httpGet);) {
			HttpEntity entity = response.getEntity();
			String json = EntityUtils.toString(entity);
			JSONObject obj = new JSONObject(json);
			System.out.println(obj);
			JSONArray arr = obj.getJSONArray("issues");

			for (int i = 0; i < arr.length(); i++) {
				String rule = arr.getJSONObject(i).getString("rule");
				if (rule.equals("squid:S1068")) {
					String project = arr.getJSONObject(i).getString("project");
					String component = arr.getJSONObject(i).getString("component");
					path = component.substring(project.length() + 1, component.length());
					String message = arr.getJSONObject(i).getString("message");
					String name = StringUtils.substringBetween(message, "\"", "\"");
					FileInputStream in = new FileInputStream("c://Users/Timo/eclipse-workspace/Test/" + path);
					CompilationUnit compilationUnit = JavaParser.parse(in);
					System.out.println(compilationUnit.toString());
					VariableDeletor visitor = new VariableDeletor();
					visitor.setVariableName(name);
					visitor.RemoveUnusedVariable(compilationUnit);
					System.out.println(compilationUnit.toString());
					
					//Actually applies changes
					/*
					PrintWriter out = new PrintWriter("c://Users/Timo/eclipse-workspace/Test/" + path);
					out.println(compilationUnit.toString());
					out.close();
					*/
				}
			}

		}

	}
}
