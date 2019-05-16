package edu.hanyang.submit;

import java.util.ArrayList;
import java.util.List;

import edu.hanyang.indexer.Tokenizer;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.tartarus.snowball.ext.PorterStemmer;

import java.io.IOException;
import java.io.StringReader;

public class TinySETokenizer implements Tokenizer {

	private SimpleAnalyzer simpleAnalyzer;
	private PorterStemmer porterStemmer;
	
	public void setup() {
		simpleAnalyzer = new SimpleAnalyzer();
		porterStemmer = new PorterStemmer();
	}

	public List<String> split(String text) {
		List<String> result = new ArrayList<>();
		TokenStream tokenStream = simpleAnalyzer.tokenStream(null, new StringReader(text));
	    CharTermAttribute attr = tokenStream.addAttribute(CharTermAttribute.class);
	    
	    try {
			tokenStream.reset();
			while(tokenStream.incrementToken())
			{
				result.add(attr.toString());
			} 
			tokenStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
	    for (int i = 0; i < result.size(); i++)
		{
			porterStemmer.setCurrent(result.get(i));
			porterStemmer.stem();
			result.remove(i);
		    result.add(i, porterStemmer.getCurrent());
		}
	    
		return result;
	}
	
	// I don't know what should be in here.
	public void clean() {
		
	}
}

// I feel so sorry that I've been too late. This is because Maven didn't work well...