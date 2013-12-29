import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Pattern;

import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import com.sun.xml.internal.ws.api.message.Attachment;

/**
 * @author TYM  
 * @version 1.0.0
 * 
 */
public class MainClass {
	public final static String PATH = "dt/";
	//�ص������ļ�
	public final static String FL_DATASET = PATH+"allpoi.txt";
	//�ص������ļ�
	public final static String FL_PARA = PATH+"allpoi_title.txt";
	public final static String FL_CLUSTER_PATH = "out/";
	public final static int EXIT_IOEXCEPTION = 1;
	private static ArrayList<float[]> dataset = new ArrayList<>();
	private static int DIM = 0;
	private static int clusterNum = 0;
	private static int[] cluster = null;
	private static float[][] center = null;
	
	public static void main(String[] args) {
		try {
			System.out.println("��\"" + FL_DATASET + "\"���о��ࡣ");
			
			//ȷ��������
			BufferedReader in = new BufferedReader(
					new InputStreamReader(System.in));
			
			String input = "";
			Pattern pattern = Pattern.compile("[1-9][0-9]*");
			do {
				System.out.println("Input the number of clusters:");
				input = in.readLine();
			} while (!pattern.matcher(input).matches());
			
			clusterNum = Integer.valueOf(input);
			
			loadDataset();
			kmeans();

			File path = new File(FL_CLUSTER_PATH);
			if (path.exists()) {
				File[] files = path.listFiles();
				for (int i=0; i<files.length; i++) {
					files[i].delete();
				}
			}
			else {
				path.mkdir();
			}
			
			//�Ѿ�����д���ļ�
			for (int i=0; i<clusterNum; i++) {			
				int count = 0;
				FileWriter writer = new FileWriter(
						new File(FL_CLUSTER_PATH + i + ".txt"));
				for (int j=0; j<cluster.length; j++) {
					if (cluster[j] == i) {
						count++;
						
						//�����ص��Ŷ�Ӧ�ĵص����� �����޶�Ӧ�ļ�����ע�ͣ�
						String matchPair = "";				
						BufferedReader reader = new BufferedReader(
								new FileReader(new File(FL_PARA)));
						
						for (int l=0; l<=j; l++) {
							matchPair = reader.readLine();
						}
						
						writer.append(j + " " + matchPair);
						writer.append("\n");
						//[~]�����ص��Ŷ�Ӧ�ĵص����� �����޶�Ӧ�ļ�����ע�ͣ�
					}
				}
				writer.append(count + "");
				writer.close();
			}
			System.out.println("�������������\"" + FL_CLUSTER_PATH + "\"·����");
		}
		catch (IOException e) {
			System.err.println(e);
			System.exit(EXIT_IOEXCEPTION);
		}
	}
	
	//�����ݼ�����dataset������,����ʼ��
	private static void loadDataset() throws IOException {
		BufferedReader reader = new BufferedReader(
				new FileReader(new File(FL_DATASET)));
		
		String line = reader.readLine();
		while (null != line) {
			String[] seg = line.split("\\s+");
			float[] att = new float[seg.length];
			for (int i=0; i<seg.length; i++) {
				att[i] = Float.valueOf(seg[i]);
			}
			dataset.add(att);
			line = reader.readLine();
		}
		
		//��ʼ��
		cluster = new int[dataset.size()];
		for (int i=0; i<cluster.length; i++) {
			cluster[i] = -1;
		}
		DIM = dataset.get(0).length;
		center = new float[clusterNum][DIM];
	}
	
	//����x��y����֮��ľ���
	public static double distance (float[] x, float[] y) {
		if (null != x && null != y) {
			if (x.length == y.length) {
				float squareSum = 0;
				for (int i=0; i<x.length; i++) {
					squareSum += (x[i]-y[i])*(x[i]-y[i]);
				}
				return Math.sqrt(squareSum);
			}
			else {
				return 0;
			}
		}
		else {
			return 0;
		}
	}
	
	//����
	private static void kmeans() {
		//���ѡȡ��ʼ���ĵ�
		Random random = new Random();
		for (int i=0; i<clusterNum; i++) {
			int index = 0;
			do {
				index = random.nextInt(cluster.length);
			}
			while (-1 != cluster[index]);
			cluster[index] = i;
			for (int j=0; j<DIM; j++) {
				center[i][j] = dataset.get(index)[j];
			}
		}
		
		//ѭ���������ĵ�ͷ���
		while (cluster()) {
			refreshCenter();
		}
	}
	
	private static boolean cluster() {
		boolean clusterChanged = false;
		
		for (int i=0; i<dataset.size(); i++) {
			double minDistance = 0;
			if (-1 == cluster[i]) {
				minDistance = Double.MAX_VALUE;
			}
			else {
				minDistance = distance(dataset.get(i), center[cluster[i]]);
			}
			for (int j=0; j<clusterNum; j++) {
				double currentDistance = distance(dataset.get(i), center[j]);
				if (currentDistance < minDistance) {
					minDistance = currentDistance;
					cluster[i] = j;
					clusterChanged = true;
				}
				else {
					continue;
				}
			}
		}
		
		return clusterChanged;
	}
	

	private static void refreshCenter() {
		for (int i=0; i<clusterNum; i++) {
			for (int k=0; k<DIM; k++) {
				center[i][k] = 0;
			}
			
			int count = 0;
			for (int j=0; j<cluster.length; j++) {
				//����j���¼���ڵ�i����
				if (cluster[j] == i) {
					count++;
					float[] record = dataset.get(j);
					for (int k=0; k<DIM; k++) {
						center[i][k] += record[k];
					}
				}
				else {
					continue;
				}
			}
			
			//��ƽ������
			if (count != 0) {
				for (int k=0; k<DIM; k++) {
					center[i][k] /= count;
				}
			}
			else {
				continue;
			}
		}
	}
}
