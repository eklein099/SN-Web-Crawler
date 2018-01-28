import java.net.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Scanner;
import java.util.ArrayList;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

 class WebCrawler {
	public static void main(String[] args){
		try {
			//System.out.println(search("https://www.salesnexus.com","https://www.salesnexus.com","<script>(function(html){html.className = html.className.replace(/\bno-js\b/,'js')})(document.documentElement);</script>",new ArrayList<String>()));
			System.out.println(search("https://www.salesnexus.com","https://www.salesnexus.com","login",new ArrayList<String>()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("done");
	}
	
	public static String getPageData(String url) throws Exception{
		String pageData = "";
		URL u = new URL(url);
		HttpURLConnection con;
		if(url.substring(0,url.indexOf(":")).equals("https")){
			
			TrustManager[] trustAll = new TrustManager[]{new X509TrustManager(){
				@Override
				public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {	
				}

				@Override
				public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {	
				}

				@Override
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			}};
			
			SSLContext context = SSLContext.getInstance("SSL");
			context.init(null, trustAll, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
			
			HostnameVerifier validateAll = new HostnameVerifier(){
				@Override
				public boolean verify(String arg0, SSLSession arg1) {
					return true;
				}
			};
			
			HttpsURLConnection.setDefaultHostnameVerifier(validateAll);
			con = (HttpsURLConnection) u.openConnection();
		}
		else
			con = (HttpURLConnection) u.openConnection();
		Scanner s = new Scanner(con.getInputStream());	
		while(s.hasNext())
			pageData += s.nextLine();		
		return pageData;
	}
	
	//creates an ArrayList with all occurrences of a given String within another String
	//(some code within a web page)
	public static ArrayList<String> findBetween(String HTML,String start, String end){
		ArrayList<String> results = new ArrayList<String>();
		String html = "";
		html += HTML;
		
		if(html.indexOf(end) < html.indexOf(start))
			html = html.substring(html.indexOf(start), html.length());
		
		while(html.indexOf(start) > -1 && html.indexOf(end) >-1){ //java.lang.StringIndexOutOfBoundsException: String index out of range: -245 ???????????
			System.out.println(""+html.indexOf(start)+" -- "+html.indexOf(end)+end.length());
			results.add(html.substring(html.indexOf(start),html.indexOf(end)+end.length()));
			html = html.substring(html.indexOf(end)+end.length(),html.length());
		}
		
		return results;
	}
	
	//finds all links at a given on a given webpage
	public static ArrayList<String> getLinks(String url) throws Exception{
		String html = getPageData(url);
		String domain = "";
		if(url.indexOf(".com")!=-1)
			domain = url.substring(0,url.indexOf(".com")+4);
		else if(url.indexOf(".net")!=-1)
			domain = url.substring(0,url.indexOf(".net")+4);
		else if(url.indexOf(".org")!=-1)
			domain = url.substring(0,url.indexOf(".org"));
		else if(url.indexOf(".gov")!=-1)
			domain = url.substring(0,url.indexOf(".gov")+4);
		ArrayList<String> links = findBetween(html,"<a","</a>");
		ArrayList<String> urls = new ArrayList<String>();
		for(String link: links){
			//shorten to only url from <a href="...." ... </a>
			if(link.indexOf("href") != 0 && link.indexOf('"')!=0 && link.indexOf('"')!=link.lastIndexOf('"')){
				link = link.substring(link.indexOf('"',link.indexOf("href"))+1,link.indexOf('"', link.indexOf('"',link.indexOf("href"))+1));
				if(!link.startsWith("http")){
					if(link.charAt(0)!='/')
						link = "/"+link;
					link = domain + link;
				}
				urls.add(link);
			}
		}
		return urls;
	}
	
	//searches for a portion of code on a web page and recursively calls itself for all links on the web page until the entire domain has been searched
	public static String search(String url, String domain, String target, ArrayList done) throws Exception{
		String result = "";
		if(url.substring(0,domain.length()).equals(domain)&&!done.contains(url)){
			done.add(url);
			if(getPageData(url).contains(target))
				result += "\""+url+"\", ";
			ArrayList<String> links = getLinks(url);
			for(String link: links)
				result += search(link,domain,target,done);
		}
		return result;
	}
}


