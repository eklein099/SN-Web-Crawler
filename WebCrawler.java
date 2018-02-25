import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Scanner;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class WebCrawler {
	private ArrayList<String> open;
	private ArrayList<String> closed;
	private ArrayList<String> found;
	private int index;
	private boolean connected;
	//private String html;
	private String domain;
	private String target;
	
	public WebCrawler(String domain){
		open = new ArrayList<String>();
		closed = new ArrayList<String>();
		index = 0;
		found = new ArrayList<String>();
		this.domain = domain;
	}
	
	public String connect(String url){
		String html = "";
		try{
			URL u = new URL(url);
			HttpURLConnection con;
			
			if(url.substring(0,8).equals("https://")){
				TrustManager[] trustAll = new TrustManager[]{new X509TrustManager(){

					@Override
					public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {						
					}

					@Override
					public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {						
					}

					@Override
					public X509Certificate[] getAcceptedIssuers() {
						return null;
					}
					}
				};
				
				SSLContext context = SSLContext.getInstance("SSL");
				context.init(null, trustAll,new java.security.SecureRandom());
				
				HostnameVerifier verifyAll = new HostnameVerifier(){
					@Override
					public boolean verify(String hostname, SSLSession session) {
						return true;
					}
				};
				
				HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
				HttpsURLConnection.setDefaultHostnameVerifier(verifyAll);
				con = (HttpsURLConnection) u.openConnection();
			}else if(url.substring(0,7).equals("http://")){
				con = (HttpURLConnection) u.openConnection();
			}else{
				System.out.println("invalid url start");
				throw new Exception();
			}
		Scanner s = new Scanner(con.getInputStream());
		String out = "";
		while(s.hasNext())
			out += s.nextLine();
		s.close();
		html = out;
		connected = true;
		}catch(Exception e){
			connected = false;
			html = "";
			System.out.println("exception in connect");
			e.printStackTrace();
			return "";
		}
		return html;
	}
	
	public void getLinks(String html){
		String data = ""+html;
		System.out.println(data);
		while(data.indexOf("href")!= -1){
			data = data.substring(data.indexOf("href")+4);
			int occ = 0;
			int[] indeces = new int[2];
			for(int i = 0; occ < 2; i++){
				if(data.charAt(i)=='"'){
					indeces[occ] = i;
					occ++;
				}
			}
			String link = data.substring(indeces[0]+1, indeces[1]);
			if(!link.contains("://www."))
				link = domain + link;
			if(link.contains(domain) && !closed.contains(link) &&!open.contains(link))
				open.add(link);
			else
				System.out.println(link);
		}
	}
	
	public void search(){
		if(open.size() > 0){
			String html = connect(open.get(0));
			closed.add(open.get(0));
			if(connected){
				getLinks(html);
				System.out.println(open.size());
				if(html.indexOf(target) != -1){
					System.out.println("FOUND" + open.get(0));
					found.add(open.get(0));
				}
				if(open.size() % 100 == 0)
					System.out.println(open +" - "+ closed.size());
			}
			open.remove(0);
			search();
			
		}
	}
	
	public void add(String u){
		open.add(u);
	}
	
	public ArrayList<String> getOpen(){
		return open;
	}
	
	public ArrayList<String> getClosed(){
		return closed;
	}
	
	public void setTarget(String target){
		this.target = target;
	}
	
	public ArrayList<String> getFound(){
		return found;
	}
	
	public boolean done(){
		return open.size() == 0;
	}
	
	public void setDomain(String domain){
		this.domain = domain;
	}
}
