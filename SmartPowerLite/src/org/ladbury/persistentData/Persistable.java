package org.ladbury.persistentData;

public interface Persistable<E> {
	/**
	 * 
	 */
	//
	// this method gets the key field of the object
	//
    long id();
	//
	// This Method generates or returns the name of the element
	// this can be used for debug or as the contents of a combo list in the UI
	//
    String name();
	//
	// This Method generates a comma separated String from the elements of class
	// this can be used for debug or as the contents of a combo list in the UI
	//
    String toCSV();
	//
	// this method copies the fields of the object excluding the key "ID" field
	// from one object to another without creating a new object so that persistence
	//of the updated object is preserve.
	//
    void updateFields(E element);
	//
	// standard id display string 	"["+id()+"] "+name()
	//
    String idString();
}
