package edu.hanyang.submit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import org.apache.commons.lang3.tuple.MutableTriple;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import edu.hanyang.indexer.ExternalSort;

class DataManager {
	DataInputStream input;
	MutableTriple<Integer, Integer, Integer> tuple;
	
	public DataManager(DataInputStream dis) throws IOException{
		this.input = dis;
		this.tuple = new MutableTriple<>();
		if(this.input.available() > 0){
            this.tuple.setLeft(this.input.readInt());
            this.tuple.setMiddle(this.input.readInt());
            this.tuple.setRight(this.input.readInt());
        } else {
            this.tuple = null;
        }
	}
	
	public MutableTriple<Integer, Integer, Integer> getTuple() {
		return this.tuple;
	}
	
	public boolean isEmpty(){
        return this.tuple == null;
    }
	
	public int compareTo(DataManager o) {
		return this.tuple.compareTo(o.tuple);
	}
}


public class TinySEExternalSort implements ExternalSort {
	
	public void sort(String infile, String outfile, String tmpdir, int blocksize, int nblocks) throws IOException {
		
		int nElement = (blocksize * nblocks) / 12;
		
		// 1) initial phase
		ArrayList<MutableTriple<Integer, Integer, Integer>> dataArr = new ArrayList<>(nElement);
		DataInputStream dis = new DataInputStream(new BufferedInputStream(
												new FileInputStream(infile),blocksize));
		
		File file = new File(tmpdir);
		if(!file.exists()) {
			file.mkdir();
		}
		
		while(dis.available() > 0) {
			for(int i = 0; i < nElement; i++) {
				MutableTriple<Integer,Integer,Integer> tmp = new MutableTriple<>();
				tmp.setLeft(dis.readInt());
				tmp.setMiddle(dis.readInt());
				tmp.setRight(dis.readInt());
				dataArr.add(tmp);
			}
			
			Collections.sort(dataArr);
			
			DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(
									new FileOutputStream(file),blocksize));

			for(MutableTriple<Integer,Integer,Integer> tmp2 : dataArr) {
				dos.writeInt(tmp2.getLeft());
				dos.writeInt(tmp2.getMiddle());
				dos.writeInt(tmp2.getRight());
			}
			dos.flush();
			dataArr.clear();
			dos.close();

		}

		dis.close();
		
		// 2) n-way merge
		_externalMergeSort(tmpdir, outfile, 0, nblocks, blocksize);
	}
	
	private void _externalMergeSort(String tmpDir, String outputFile, int step, int nblocks, int blocksize) throws IOException {
		List<DataInputStream> files = new ArrayList<>();
		File[] fileArr = (new File(tmpDir + File.separator + String.valueOf(step))).listFiles();
		if (fileArr.length <= nblocks - 1) {
			for (File f : fileArr) {
				DataInputStream dos = new DataInputStream(new BufferedInputStream(
														new FileInputStream (f.getAbsolutePath()),blocksize));
				files.add(dos);			
			}
			
			n_way_merge(files, outputFile, blocksize);
			
            files.clear();
		}
		else {
			int cnt = 0;
			for (File f : fileArr) {
				DataInputStream dos = new DataInputStream(new BufferedInputStream(
														new FileInputStream (f.getAbsolutePath()),blocksize));
				files.add(dos);	
				cnt++;
				if (cnt == Nblocks - 1) {
					n_way_merge(files, outputFile, blocksize); //fill in the blank
					files.clear();
				}
			}
			if (files.size() != 0) {
				n_way_merge(files, outputFile, blocksize); //fill in the blank
				files.clear();
			}
			_externalMergeSort(tmpDir, outputFile, step+1, nblocks, blocksize);
		}
	}
		
		public void n_way_merge(List<DataInputStream> files, String outputFile, int blocksize) throws IOException {
			
			PriorityQueue<DataManager> queue = new PriorityQueue<> (files.size(), new Comparator<DataManager>() {
				public int compare(DataManager o1, DataManager o2) {
					return o1.tuple.compareTo(o2.tuple);
				}
			});
			
			DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(
					new FileOutputStream(outputFile),blocksize));
			
			ArrayList<MutableTriple<Integer, Integer, Integer>> output = new ArrayList<>();
			
			for(DataInputStream f : files){
				queue.add(new DataManager(f));
			}
			
			while (queue.size() != 0) {
				DataManager dm = queue.poll();
				MutableTriple<Integer, Integer, Integer> tmp = dm.getTuple();
				output.add(tmp);

				if(!dm.isEmpty()){
					queue.add(dm);
				}
				if(output.size() == files.size()){
					 dos.writeInt(tmp.getLeft());
		             dos.writeInt(tmp.getMiddle());
		             dos.writeInt(tmp.getRight());

		             dos.flush();
				}
				dos.close();
			}
		}
}
// It does not work...