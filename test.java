package cn.stanliski.spider.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import cn.stanliski.spider.entity.News;
import cn.stanliski.spider.entity.SpiderSettings;
import cn.stanliski.spider.tools.Encoding;


/**
 * 
 * Spider which crawle 
 * 			and analysis web page.
 * 
 * @author Stanley
 *
 */
public abstract class Spider {

	/** user agent. */
	private static String USER_AGENT = "Mozilla/5.0";

	/** Access web page url. */
	protected String REMOTE_WEBPAGE_URL = "";

	/** Cookies */
	public String cookies;
	
	/** Spider Settings */
	public SpiderSettings settings;
	
	public SpiderSettings getSettings() {
		return settings;
	}

	public void setSettings(SpiderSettings settings) {
		this.settings = settings;
	}

	public String getCookies() {
		return cookies;
	}

	public void setCookies(String cookies) {
		this.cookies = cookies;
	}

	/**
	 *	Cookie request. 
	 */
	public String cookieRequest(String url){

		BufferedReader rd = null;
		StringBuffer result = new StringBuffer();
		try {
			HttpClient client = HttpClientBuilder.create().build();
			HttpGet request = new HttpGet(url);
			// add request header
			request.addHeader("User-Agent", USER_AGENT);
			request.addHeader("Host", "www.newsmth.net");
			request.addHeader("Accept", "*/*");
			request.addHeader("Accept-Language", "zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
			request.addHeader("Accept-Encoding", "gzip, deflate");
			request.addHeader("X-Requested-With", "XMLHttpRequest");
			request.addHeader("Referer", "http://www.newsmth.net/nForum/");
			request.addHeader("Connection", "keep-alive");
			HttpResponse response;
			response = client.execute(request);
			System.out.println("Response Code : " 
					+ response.getStatusLine().getStatusCode());
			setCookies(response.getFirstHeader("Set-Cookie") == null ? "" : 
	            response.getFirstHeader("Set-Cookie").toString());
			rd = new BufferedReader(
					new InputStreamReader(response.getEntity().getContent()));
			String line = rd.readLine();
			while ((line = rd.readLine()) != null) {
				result.append(line + "\n");
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return rd.toString();
	}

	/**
	 * Request a web page.
	 * @param url
	 * @return
	 */
	public String request(String url){
		BufferedReader rd = null;
		StringBuffer result = new StringBuffer();
		try {
			HttpClient client = HttpClientBuilder.create().build();
			HttpGet request = new HttpGet(url);
			// add request header
			request.addHeader("User-Agent", USER_AGENT);
			HttpResponse response;
			response = client.execute(request);
			System.out.println("Response Code : " 
					+ response.getStatusLine().getStatusCode());
			int responseCode = response.getStatusLine().getStatusCode();
			rd = new BufferedReader(
					new InputStreamReader(response.getEntity().getContent(), Encoding.GBK));
			String line = "";
			while ((line = rd.readLine()) != null) {
				result.append(line + "\n");
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			IOUtils.closeQuietly(rd);
		}
		return result.toString();
	}

	
	public String varifyImgTag(String html){
		Document doc = Jsoup.parse(html);
		Elements elements = doc.select("img[src]");
		for(int i = 0; i < elements.size(); i++){
			Element e = elements.get(i);
			String src = e.attr("src");
			if(!src.startsWith("www")){
				src = settings.getOriginHtml() + src;
			}
			String newImg = "<img src=\"" + src + "\" />";
			html = html.substring(0, html.indexOf(e.toString())) + newImg +
					html.substring(html.indexOf(e.toString()) + e.toString().length());
		}
		return html;
	}
	
	/**
	 * Extract info from page.
	 * @param html
	 */
	public abstract List<News> extractPage(String html);
	
	/**
	 * extract page detail.
	 * @param html
	 * @param condition
	 * @return
	 */
	public abstract String extractPageDetail(String html, String condition);

	/**
	 * Crawler a page.
	 */
	public abstract List crawler();

}
