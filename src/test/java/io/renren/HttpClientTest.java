package io.renren;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @Author wgsh
 * @Date wgshb on 2019/7/14 13:59
 */
public class HttpClientTest {

	public String getPostResponse(String url,  Map<String, String> parmMap ) throws IOException {
		{
			String result = "";
			PostMethod post = new PostMethod(url);
			HttpClient client = new HttpClient();
			Iterator it = parmMap.entrySet().iterator();
			NameValuePair[] param = new NameValuePair[parmMap.size()];
			int i = 0;
			while (it.hasNext()) {
				Map.Entry parmEntry = (Map.Entry) it.next();
				param[i++] = new NameValuePair((String) parmEntry.getKey(), (String) parmEntry.getValue());
			}


			post.setRequestBody(param);
			try {
				int statusCode = client.executeMethod(post);

				if (statusCode == HttpStatus.SC_MOVED_PERMANENTLY || statusCode == HttpStatus.SC_MOVED_TEMPORARILY) {
					Header locationHeader = post.getResponseHeader("location");
					String location = "";
					if (locationHeader != null) {
						location = locationHeader.getValue();
						result = this.getPostResponse(location, parmMap);//用跳转后的页面重新请求�??
					}
				} else if (statusCode == HttpStatus.SC_OK) {
					result = post.getResponseBodyAsString();
				}
			} catch (IOException ex) {
			} finally {
				post.releaseConnection();
			}
			return result;
		}
	}

	public static void main(String[] args) throws IOException {
		Map<String, String> map = new HashMap<String, String>();
		map.put("tyshxydm", "93220181571118707H");
		Iterator it = map.entrySet().iterator();
		NameValuePair[] param = new NameValuePair[map.size()];
		int i = 0;
		String z =new HttpClientTest().getPostResponse("http://localhost:8080/application_agents/jgdm/querybytyshxydm", map);
		System.out.println(z);
	}
}
