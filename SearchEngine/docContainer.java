package SearchEngine;
import java.util.ArrayList;
import java.util.List;

public class docContainer {
	private List<Integer> positionList;
	private List<Integer> tagsList;
	
	docContainer(int firstPosition){
		positionList = new ArrayList<Integer>();
		tagsList = new ArrayList<Integer>();
		positionList.add(firstPosition);
	}
	
	docContainer(){
		positionList = new ArrayList<Integer>();
		tagsList = new ArrayList<Integer>();
	}
	
	public void appendToPositionList(int position) {
		positionList.add((Integer)position);
	}
	
	public List<Integer> getPositionList(){
		return positionList;
	}
	
	public List<Integer> getTagsList(){
		return tagsList;
	}
	
	public int getTF(){
		return positionList.size();
	}
	
	public void printPositionList() {
		for(int i=0;i<positionList.size();i++) {
			System.out.println(positionList.get(i));
		}
	}
}
