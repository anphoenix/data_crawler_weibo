package phoenix.weibo;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Random;

public class WeiboLogin {
	
	private int rl;
	private int vt;
	private String gsid;
	private String[] keys;
	private int idx;
	public String[] proxy;
	public int ipx;
	private int pxywindow;
	private FileWriter log;
	private Random random;

	
	public WeiboLogin(String user, String passwd) {
		
		try {
			this.log = new FileWriter("log.txt", true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
		
		this.rl = 0;
		this.vt = 4;
		
		this.keys = new String[47];
		for(int i = 0; i < 47; i++){
			this.keys[i] = "4uTl4aab1D0ftNk2ueCoz8N7KfP";
		}
		
		this.random = new Random();
		
		this.idx = Math.abs(this.random.nextInt()) % this.keys.length;
		
		this.gsid = this.keys[this.idx];
		
		RandomAccessFile file = null;
		try {
			file = new RandomAccessFile("./available.txt", "r");
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		ArrayList<String> alist = new ArrayList<String>();
		
		String temp = null;
		try {
			temp = file.readLine();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.exit(1);
		}
		
		while(temp != null) {
			alist.add(temp);
			try {
				temp = file.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(1);
			}
		}
		
		try {
			file.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
		file = null;
		
		this.proxy = new String[alist.size()];
		
		for(int i = 0; i < this.proxy.length; i++)
			this.proxy[i] = alist.get(i).split(":")[0];
		
		alist = null;
		
		this.ipx = Math.abs(this.random.nextInt()) % this.proxy.length;
		this.pxywindow = 0;

	}
	
	public void writeLog(String str) {
		
		try {
			this.log.write(str);
			this.log.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String changeProxy() {
		
		this.ipx = Math.abs(this.random.nextInt()) % this.proxy.length;
		
		return this.proxy[this.ipx];
	}
	
	public String getProxy() {
		this.pxywindow++;
		if(this.pxywindow == 10)
		{
			this.pxywindow = 0;
			
			this.ipx = Math.abs(this.random.nextInt()) % this.proxy.length;
		}
		
		return this.proxy[this.ipx];
	}
	
	public void changeKey() {
		
		this.idx = Math.abs(this.random.nextInt()) % this.keys.length;
		
		this.gsid = this.keys[this.idx];
	}
	
	public int get_rl() {
		return rl;
	}
	
	public int get_vt() {
		return vt;
	}
	
	public String get_gsid() {
		return gsid;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
