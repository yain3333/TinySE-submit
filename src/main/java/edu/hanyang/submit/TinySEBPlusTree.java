import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import edu.hanyang.indexer.BPlusTree;

public class TinySEBPlusTree implements BPlusTree{
	
	private TreeNode root;
    private TreeNode aChild;
    private RandomAccessFile treeFile;
    private BPlusConfiguration conf;
    private LinkedList<Long> freeSlotPool;
    private LinkedList<Long> lookupPagesPool;
    private long firstPoolNextPointer;
    private long totalTreePages;
    private long maxPageNumber;
    private int deleteIterations;
    private BPlusTreePerformanceCounter bPerf = null;

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void insert(int key, int val) throws IOException {
		Block block = searchNode(key);
		
		if (block.nkeys + 1 > maxKeys) {
			Block newnode = split(block, key, val);
			insertInternal(block.parent, newnode.my_pos);
		} else {
			//...
		}
		
	}

	@Override
	public void open(String metapath, String filepath, int blocksize, int nblocks) throws IOException {
		this.blocksize = blocksize; 
	    this.nblocks = nblocks; 
	    this.buf = new byte[blocksize]; 
	    this.buffer = ByteBuffer.wrap(buf); 
	    this.maxKeys = (blocksize - 16) / 8; 

	    raf = new RandomAccessFile(filepath, "rw");	
	}

	@Override
	public int search(int key) throws IOException {
		Block rb = readBlock(rootindex);
		return _search(rb, key);
	}
	
	private int _search(Block b, int key) throws IOException {
		if (b.type == 1) { //non-leaf
			//...
			if (block.keys[i] < key) {
				child = readBlock(b.vals[i]);
			}
			//...
		} else { //leaf
			/* binary or linear search */
			// if exists,
			return val;
			// else
			return -1;
		}
	}
}
