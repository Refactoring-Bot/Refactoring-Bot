package sonarqube;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;


public class SonarQubeTest {

	public static void main(String[] args) throws IOException{
		HttpGet httpGet =  new HttpGet("http://localhost:9000/api/issues/search?&format=json");
		
		
		try(CloseableHttpClient httpClient = HttpClients.createDefault();
				CloseableHttpResponse response = httpClient.execute(httpGet);){
			System.out.println(response.getStatusLine());
			HttpEntity entity = response.getEntity();
			String json = EntityUtils.toString(entity);
			//System.out.println(EntityUtils.toString(entity));
			JSONObject obj = new JSONObject(json);
			System.out.println(obj);
			JSONArray arr = obj.getJSONArray("issues");
			for(int i = 0; i< arr.length(); i++) {
				String rule =  arr.getJSONObject(i).getString("rule");
				System.out.println(rule);
				HttpGet get = new HttpGet("http://localhost:9000/api/rules/show?key=" + rule);
				CloseableHttpResponse responseRule = httpClient.execute(get);
				HttpEntity entitiyRule = responseRule.getEntity();
				System.out.println(EntityUtils.toString(entitiyRule));
				
			}
		}
	}

}
