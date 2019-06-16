package edu.hanyang.submit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.hanyang.indexer.DocumentCursor;
import edu.hanyang.indexer.PositionCursor;
import edu.hanyang.indexer.IntermediateList;
import edu.hanyang.indexer.IntermediatePositionalList;
import edu.hanyang.indexer.QueryPlanTree;
import edu.hanyang.indexer.QueryProcess;
import edu.hanyang.indexer.StatAPI;
import edu.hanyang.indexer.QueryPlanTree.NODE_TYPE;
import edu.hanyang.indexer.QueryPlanTree.QueryPlanNode;

public class TinySEQueryProcess implements QueryProcess {

	@Override
	public void op_and_w_pos(DocumentCursor op1, DocumentCursor op2, int shift, IntermediatePositionalList out)
			throws IOException {
		
		int docID1, docID2;
		PositionCursor pc1, pc2;
		int pos1, pos2;
		
		while (!op1.is_eol() && !op2.is_eol()) {
			docID1 = op1.get_docid();
			docID2 = op2.get_docid();
			
			if (docID1 < docID2)	op1.go_next();
			else if (docID1 > docID2) op2.go_next();
			else {
				pc1 = op1.get_position_cursor();
				pc2 = op2.get_position_cursor();
				
				while (!pc1.is_eol() && !pc2.is_eol()) {
					pos1 = pc1.get_pos();
					pos2 = pc2.get_pos();
					
					if (pos1 + shift < pos2) pc1.go_next();
					else if (pos1 + shift > pos2) pc2.go_next();
					else {
						out.put_docid_and_pos(docID1, pos1);
						pc1.go_next();
						pc2.go_next();
					}
				}
				op1.go_next();
				op2.go_next();
			}
		}
	}
	
	@Override
	public void op_and_wo_pos(DocumentCursor op1, DocumentCursor op2, IntermediateList out) throws IOException {
		
		int docID1, docID2;
		
		while (!op1.is_eol() && !op2.is_eol()) {
			docID1 = op1.get_docid();
			docID2 = op2.get_docid();
			
			if (docID1 < docID2) op1.go_next();
			else if (docID1 > docID2) op2.go_next();
			else {
				out.put_docid(docID1);
				op1.go_next();
				op2.go_next();
			}
		}
	}

	@Override
	public QueryPlanTree parse_query(String query, StatAPI stat) throws Exception {
		
		QueryPlanTree queryplantree = new QueryPlanTree();
		QueryPlanNode oprand1, oprand2;
		List<QueryPlanNode> oprandList = new ArrayList<>();
		int shift = 0;
		Boolean in_phase = false;
		
		String[] str = query.split(" ");
		for (int i = 0; i < str.length; i++) {
			if (str[i].charAt(0) == '"') {
				in_phase = true;
				shift = 0;
			}
			
			oprand1 = queryplantree.new QueryPlanNode();
			oprand1.type = NODE_TYPE.OPRAND;
			oprand1.termid = Integer.parseInt((str[i].replace('"', ' ')).trim());

			if (!in_phase) {
				oprand2 = queryplantree.new QueryPlanNode();
				oprand2.type = NODE_TYPE.OP_REMOVE_POS;
				oprand2.left = oprand1;
				oprand1 = oprand2;
				
				if (oprandList.isEmpty()) oprandList.add(oprand1);
				else {
					oprand2 = queryplantree.new QueryPlanNode();
					oprand2.type = NODE_TYPE.OP_AND;
					oprand2.left = oprandList.get(oprandList.size()-1);
					oprandList.remove(oprandList.size()-1);
					oprand2.right = oprand1;
					oprandList.add(oprand2);
				}
			}
			else {
				if (!oprandList.isEmpty() && (oprandList.get(oprandList.size() - 1).type == NODE_TYPE.OPRAND || 
						oprandList.get(oprandList.size() - 1).type == NODE_TYPE.OP_SHIFTED_AND)) {
					oprand2 = queryplantree.new QueryPlanNode();
					oprand2.type = NODE_TYPE.OP_SHIFTED_AND;
					oprand2.shift = shift;
					oprand2.left = oprandList.get(oprandList.size()-1);
					oprandList.remove(oprandList.size()-1);
					oprand2.right = oprand1;
					oprandList.add(oprand2);
				}
				else{
					oprandList.add(oprand1);
				}
				shift++;
			}

			if (str[i].charAt(str[i].length() - 1) == '"') {
				in_phase = false;
				oprand1 = queryplantree.new QueryPlanNode();
				oprand1.type = NODE_TYPE.OP_REMOVE_POS;
				oprand1.left = oprandList.get(oprandList.size() - 1);
				oprandList.remove(oprandList.size() - 1);
				
				if (oprandList.isEmpty()) oprandList.add(oprand1);
				else {
					oprand2 = queryplantree.new QueryPlanNode();
					oprand2.type = NODE_TYPE.OP_AND;
					oprand2.left = oprandList.get(oprandList.size() - 1);
					oprandList.remove(oprandList.size()-1);
					oprand2.right = oprand1;
					oprandList.add(oprand2);
				}
			}
		}
		queryplantree.root = oprandList.get(0);
		return queryplantree;
	}
}
