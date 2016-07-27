package org.ladbury.persistentData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PersistentList<E extends Persistable<E> & Comparable<? super E> > extends ArrayList <E>{
	/**
	 * This implements a list and keeps the list in sync with the persistent copy
	 * i.e operations are carried out on the list and the database within each
	 * of the list access functions. using JPA javax.persistence functions
	 * it is a precondition that the list is sorted as it  loaded from the database, and
	 * resorted whenever new items are added. There is an assumption that updates don't
	 * change the sort order (if they do the list should be resorted manually)
	 * the class shouldn't throw exceptions, but will return a size of 0
	 * 
	 * @author GJWood
	 * @version 1.0 2012/11/29
	 * @param <E> The element type to be stored in the list, which must implement Comparable and Persistable
	 */
	private static final long serialVersionUID = -1749169063106062657L;


	public PersistentList(List<E> resultList) {
		super();
		int i;
		if(resultList!=null)
			for (i=0; i<resultList.size();i++){
				super.add(resultList.get(i));
			}
		else super.clear();
		Collections.sort(this);
	}

	public E get(int row) {
		if (rangeCheck(row)){
			return super.get(row);
		}
		try {
			System.out.println("Attempt to get more entries than exist: "+row);
			throw new Exception();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
		public int size() {
		try{
			return super.size();
		}
		catch(Exception e){
			return 0;
		}
	}
		
	public boolean add(E p) {
		//Collections.sort(this); //sort order depends on element type defined comparators
		return super.add(p);
	}
	public boolean softAdd(E p) {
		return super.add(p);
	}
	public E remove(int row){
		E p;
		if (rangeCheck(row)) {
			p = super.remove(row);
			return p;
		}
		return null;
	}
	public void update(int row, E p){
		if (rangeCheck(row)) {
			super.get(row).updateFields(p);
		}
	}
	public void outputCSV(){
		int i;
		E p;
		for( i = 0; i<this.size();i++){
			p = this.get(i);
			System.out.println(p.toCSV());
		}
	}
	public boolean rangeCheck(int row){
		return (row >=0) && (row<super.size());
	}
	public int findID(long id){
		int row=0;
		while(row < this.size()){
			if (this.get(row).id() == id) return row;
			row++;
		}
		return -1;
	}
}