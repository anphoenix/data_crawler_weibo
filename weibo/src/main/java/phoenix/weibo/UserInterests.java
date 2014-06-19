package phoenix.weibo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
//import org.apache.commons.httpclient.params.HttpMethodParams;
//import org.guangxiang.weibo.WeiboLogin;
import org.apache.commons.httpclient.params.HttpMethodParams;

public class UserInterests {
	
	private int statusCode;
	private HttpMethod getMethod;
	private String Page;
	
	private Map<String,String> streMap;
	private Map<String,String> labelMap;
	
	public UserInterests() {
		streMap = new HashMap<String,String>();
		labelMap = new HashMap<String,String>();
	}
	
	
//	public void updateHBase(String tableName, String rowKey, Map<String, String> map) {
//		HBase hbase = new HBase();
//		hbase.addData(tableName, rowKey, map);
//	}
	
	public String getStrength(WeiboLogin ln, String uid) {
		String url = "http://weibo.cn/" + uid + "/info?rl=" + ln.get_rl() + "&vt=" + ln.get_vt() + "&gsid=" + ln.get_gsid();
		String infoPage = getPage(url, ln);
		String res = null;
		String stre = null;
		String regex = "达人:.*?<br/>";
		res = regularExpression(regex, infoPage);
		
		if(res == null) {
			return null;
		}
		else
		{
			res = res.substring(3, res.length()-5);
			String[] list = res.split(" ");
			for(int i=0; i<list.length; i++){
				if(stre == null) {
					stre = list[i];
				} else {
					stre = stre + "；"+ list[i];
				}
			}
			return stre;
		}	
	}
	
	public Map<String,String> getStreMap() {
		
		return streMap;
	}
	

	public String getLabels(WeiboLogin ln, String uid) {
		String res = null;
		
		String url = "http://weibo.cn/account/privacy/tags/?uid=" + uid + "&vt=" + ln.get_vt() + "&gsid=" + ln.get_gsid();
		
		String page1 = getPage(url, ln);
		
		String regex = "的标签:<br/>.*?<br />";
		
		res = regularExpression(regex, page1);
		
		if(res == null)
			return res;
		else {
			String[] list = res.substring(9).split("</a>");
			
			res = "";
			for(int i = 0; i < list.length - 1; i++) {
				if(i == (list.length - 2))
					res = res + list[i].split(">")[1];
				else
					res = res + list[i].split(">")[1] + "；";
			
			}
			return res;
		}
	}
	
	public Map<String,String> getLabelMap() {
		
		return labelMap;
	}
	
private String getPage(String url, WeiboLogin ln) {
		
	    HttpClient client = new HttpClient();
		
		client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
		
		client.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "utf-8");
		
		client.getHttpConnectionManager().getParams().setConnectionTimeout(1000);
		
		client.getParams().setSoTimeout(60000);
		
		try{
			client.getHostConfiguration().setProxy(ln.getProxy(), 3128);
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println(ln.proxy[ln.ipx]);
			System.exit(1);
		}
		
		this.getMethod = null;
		
		this.statusCode = 301;
		
		this.Page = null;
		
		while(this.statusCode != HttpStatus.SC_OK) {
			
			this.getMethod = new GetMethod(url);
			
			try {
				this.statusCode = client.executeMethod(this.getMethod);
			} catch (HttpException e) {
				// TODO Auto-generated catch block
//				e.printStackTrace();
				ln.changeProxy();
				this.statusCode = 301;
			} catch (IOException e) {
				// TODO Auto-generated catch block
//				e.printStackTrace();
				ln.changeProxy();
				this.statusCode = 301;
			}
			
			if(this.statusCode != HttpStatus.SC_OK) {
				try{
					client.getHostConfiguration().setProxy(ln.getProxy(), 3128);
				} catch(Exception e) {
					e.printStackTrace();
					System.out.println(ln.proxy[ln.ipx]);
					System.exit(1);
				}
			}
			else {
				
				Thread t = new Thread(new Runnable() { 
					public void run() {
						try {
							Page = getMethod.getResponseBodyAsString();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							statusCode = 301;
						}
					}
				}, "Timeout guard");
				
				t.setDaemon(true);
				t.start();
				try{
					t.join(300000l);
				} catch(InterruptedException e) {
					this.statusCode = 301;
					ln.changeProxy();
					try{
						client.getHostConfiguration().setProxy(ln.getProxy(), 3128);
					} catch(Exception ee) {
						ee.printStackTrace();
						System.out.println(ln.proxy[ln.ipx]);
						System.exit(1);
					}
				}
				
				if(t.isAlive()) {
					t.interrupt();
					this.statusCode = 301;
					ln.changeProxy();
					try{
						client.getHostConfiguration().setProxy(ln.getProxy(), 3128);
					} catch(Exception ee) {
						ee.printStackTrace();
						System.out.println(ln.proxy[ln.ipx]);
						System.exit(1);
					}
				}		
			}
			
			if(this.statusCode != HttpStatus.SC_OK) {
				ln.changeProxy();
				try{
					client.getHostConfiguration().setProxy(ln.getProxy(), 3128);
				} catch(Exception e) {
					e.printStackTrace();
					System.out.println(ln.proxy[ln.ipx]);
					System.exit(1);
				}
			}
			
			if(this.statusCode == HttpStatus.SC_OK && this.Page.contains("ϵͳ��æ,���Ժ�����!")) {
				try {
					System.out.println("The sever is busy wait 10 sceonds!");
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				this.statusCode = 301;
			}
			
			if(this.statusCode == HttpStatus.SC_OK && this.Page.contains("<title>΢���㳡</title>")) {
				
				String oldkey = ln.get_gsid();
				
				ln.changeKey();
				
				String[] tmp = url.split(oldkey);
				
				if(tmp.length == 1)
					url = tmp[0] + ln.get_gsid();
				else
					url = tmp[0] + ln.get_gsid() + tmp[1];
				
				ln.changeProxy();
				try{
					client.getHostConfiguration().setProxy(ln.getProxy(), 3128);
				} catch(Exception e) {
					e.printStackTrace();
					System.out.println(ln.proxy[ln.ipx]);
					System.exit(1);
				}
				
				this.statusCode = 301;
				
				System.out.println("key refused, change key!");
			}
			
			this.getMethod.releaseConnection();
		}
		client = null;
		
		Date date = new Date();
		System.out.println("[" + date + "]" + "[url: " + url + "]" + "[proxy: " +  ln.proxy[ln.ipx] + ":3128]");
		ln.writeLog("[" + date + "]" + "[url: " + url + "]" + "[proxy: " + ln.proxy[ln.ipx] + ":3128]\n");
	
		return this.Page;
	}

private String regularExpression(String regex, String base) {
	String res = null;
	
	Pattern p = Pattern.compile(regex);
	Matcher m = p.matcher(base);
	
	if(m.find())
		res = m.group();
	
	return res;
}
	public static void main(String[] arg) throws FileNotFoundException {
		WeiboLogin login = new WeiboLogin("773810516@qq.com", "1107nie8");
		UserInterests ui = new UserInterests();
		File id = new File("id_sample.txt");
		Scanner scan = new Scanner(id);
		
		//put all users with corresponding labels and strength into maps
		while(scan.hasNext()) {
			String uid = scan.nextLine();
			String strength = ui.getStrength(login, uid);
			String label = ui.getLabels(login, uid);
			
			ui.streMap.put(uid, strength);
			ui.labelMap.put(uid, label);
			
		}
		
		
		//test
		//System.out.println(ui.getLabels(login, "1001662750"));
	}
}
