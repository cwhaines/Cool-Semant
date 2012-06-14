import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * This class may be used to contain the semantic information such as the
 * inheritance graph. You may use it or not as you like: it is only here to
 * provide a container for the supplied methods.
 */
class ClassTable {
	private int semantErrors;
	private PrintStream errorStream;
	
	private class_c obj_const;
	private ArrayList<class_c> table = new ArrayList();
	
	private int curr_class = -1;
	
	private class_c currentClass = null;
	
	/**
	 * Creates data structures representing basic Cool classes (Object, IO, Int,
	 * Bool, String). Please note: as is this method does not do anything
	 * useful; you will need to edit it to make if do what you want.
	 * */
	private void installBasicClasses() {
		AbstractSymbol filename = AbstractTable.stringtable
				.addString("<basic class>");

		// The following demonstrates how to create dummy parse trees to
		// refer to basic Cool classes. There's no need for method
		// bodies -- these are already built into the runtime system.

		// IMPORTANT: The results of the following expressions are
		// stored in local variables. You will want to do something
		// with those variables at the end of this method to make this
		// code meaningful.

		// The Object class has no parent class. Its methods are
		// cool_abort() : Object aborts the program
		// type_name() : Str returns a string representation
		// of class name
		// copy() : SELF_TYPE returns a copy of the object

		class_c Object_class = new class_c(
				0,
				TreeConstants.Object_,
				TreeConstants.No_class,
				new Features(0)
						.appendElement(
								new method(0, TreeConstants.cool_abort,
										new Formals(0), TreeConstants.Object_,
										new no_expr(0)))
						.appendElement(
								new method(0, TreeConstants.type_name,
										new Formals(0), TreeConstants.Str,
										new no_expr(0)))
						.appendElement(
								new method(0, TreeConstants.copy,
										new Formals(0),
										TreeConstants.SELF_TYPE, new no_expr(0))),
				filename);

		// The IO class inherits from Object. Its methods are
		// out_string(Str) : SELF_TYPE writes a string to the output
		// out_int(Int) : SELF_TYPE "    an int    " "     "
		// in_string() : Str reads a string from the input
		// in_int() : Int "   an int     " "     "

		class_c IO_class = new class_c(
				0,
				TreeConstants.IO,
				TreeConstants.Object_,
				new Features(0)
						.appendElement(
								new method(0, TreeConstants.out_string,
										new Formals(0)
												.appendElement(new formalc(0,
														TreeConstants.arg,
														TreeConstants.Str)),
										TreeConstants.SELF_TYPE, new no_expr(0)))
						.appendElement(
								new method(0, TreeConstants.out_int,
										new Formals(0)
												.appendElement(new formalc(0,
														TreeConstants.arg,
														TreeConstants.Int)),
										TreeConstants.SELF_TYPE, new no_expr(0)))
						.appendElement(
								new method(0, TreeConstants.in_string,
										new Formals(0), TreeConstants.Str,
										new no_expr(0)))
						.appendElement(
								new method(0, TreeConstants.in_int,
										new Formals(0), TreeConstants.Int,
										new no_expr(0))), filename);

		// The Int class has no methods and only a single attribute, the
		// "val" for the integer.

		class_c Int_class = new class_c(0, TreeConstants.Int,
				TreeConstants.Object_, new Features(0).appendElement(new attr(
						0, TreeConstants.val, TreeConstants.prim_slot,
						new no_expr(0))), filename);

		// Bool also has only the "val" slot.
		class_c Bool_class = new class_c(0, TreeConstants.Bool,
				TreeConstants.Object_, new Features(0).appendElement(new attr(
						0, TreeConstants.val, TreeConstants.prim_slot,
						new no_expr(0))), filename);

		// The class Str has a number of slots and operations:
		// val the length of the string
		// str_field the string itself
		// length() : Int returns length of the string
		// concat(arg: Str) : Str performs string concatenation
		// substr(arg: Int, arg2: Int): Str substring selection

		class_c Str_class = new class_c(
				0,
				TreeConstants.Str,
				TreeConstants.Object_,
				new Features(0)
						.appendElement(
								new attr(0, TreeConstants.val,
										TreeConstants.Int, new no_expr(0)))
						.appendElement(
								new attr(0, TreeConstants.str_field,
										TreeConstants.prim_slot, new no_expr(0)))
						.appendElement(
								new method(0, TreeConstants.length,
										new Formals(0), TreeConstants.Int,
										new no_expr(0)))
						.appendElement(
								new method(0, TreeConstants.concat,
										new Formals(0)
												.appendElement(new formalc(0,
														TreeConstants.arg,
														TreeConstants.Str)),
										TreeConstants.Str, new no_expr(0)))
						.appendElement(
								new method(
										0,
										TreeConstants.substr,
										new Formals(0)
												.appendElement(
														new formalc(
																0,
																TreeConstants.arg,
																TreeConstants.Int))
												.appendElement(
														new formalc(
																0,
																TreeConstants.arg2,
																TreeConstants.Int)),
										TreeConstants.Str, new no_expr(0))),
				filename);

		/*
		 * Do something with Object_class, IO_class, Int_class, Bool_class, and
		 * Str_class here
		 */
		
		table.add(Object_class);
		table.add(Str_class);
		table.add(Bool_class);
		table.add(Int_class);
		table.add(IO_class);
		
		obj_const = Object_class;

	}

	public ClassTable(Classes cls) {
		semantErrors = 0;
		errorStream = System.err;
		
		installBasicClasses();
		
		class_c temp;
		
		// Cannot redefine int, string, bool, io
		// Cannot inherit from int, string, bool
		for(int i = 0; i < cls.getLength(); i++) {
			temp = (class_c) cls.getNth(i).copy();
			// Check if inherited from int, string or bool
			for (int j = 0; j < 5; j++) {
				if (j > 0 && j < 4 && 
						temp.getParent() == table.get(j).getName()) {
					semantError(temp);
					System.out.println("Class " + temp.getName().getString()
							+ " cannot inherit class "
							+ table.get(j).getName().getString());
					hierarchyError();
				}
				if(temp.getName() == table.get(j).getName()){
					semantError(temp);
					System.out.println("Redefinition of basic class "
							+ table.get(j).getName().getString());
					hierarchyError();
				}
			}
			for(int k = 5; k < table.size(); k++){
				if(temp.getName() == table.get(k).getName()){
					semantError(temp);
					System.out.println("Class "
							+ table.get(k).getName().getString()
							+ " was previously defined.");
					hierarchyError();
				}
			}
			
			table.add(temp);
		}
		
		
		// Starts at 5, since it doesn't need to check installed classes
		for(int i = 5; i < table.size(); i++){
			if(!isSubtypeOfObject(table.get(i), table.size())){
				semantError(table.get(i));
				System.out.println("Did not inherit Object");
				hierarchyError();
			}
		}
	}
	
	/**
	 * 
	 * @param c
	 * 			the class
	 * @return 	the table index of the parent of the given class
	 * 			-1 if not found
	 */
	public int findParent(class_c c){
		for(int i = 0; i < table.size(); i++){
			if(c.getParent() == table.get(i).getName()){
				return i;
			}
		}
		semantError(c);
		System.out.println("I don't even know");
		return -1;
	}
	
	public class_c retParent(class_c c){
		for(int i = 0; i < table.size(); i++){
			if(c.getParent() == table.get(i).getName()){
				return table.get(i);
			}
		}
		semantError(c);
		System.out.println("Class has no parent");
		hierarchyError();
		return null;
	}
	
	public AbstractSymbol retParent(AbstractSymbol a){
		AbstractSymbol b = findClass(a).getParent();
		if(b != null){
			return b;
		}
		semantError(currentClass);
		System.out.println("No parent?");
		hierarchyError();
		return null;
	}
	
	public class_c findClass(AbstractSymbol a) {
		for(int i = 0; i < table.size(); i++){
			if(table.get(i).getName() == a){
				return table.get(i);
			}
		}
		semantError(currentClass);
		System.out.println("No such class: " + a.getString());
		hierarchyError();
		return null;
	}
	
	public boolean isSubtypeOfObject(class_c c, int depth){
		if(depth < 1){
			return false;
		}
		if(c.getParent() == obj_const.getName()){
			return true;
		}
		int index = findParent(c);
		//System.out.println("What?");

		return isSubtypeOfObject(table.get(index > 0 ? index : 0), depth-1);
	}
	
	public void setCurrClass(class_c c){
		currentClass = c;
	}
	
	public class_c getCurrClass(){
		return currentClass;
	}
	
	public boolean isSubtype(AbstractSymbol a, AbstractSymbol b){
		if (a == TreeConstants.No_type || b == TreeConstants.No_type) {
			return false;
		}
		if(a == b){
			return true;
		} else if (a != obj_const.getName()){
			return isSubtype(retParent(a), b);
		}
		return false;
	}
	
	public void hierarchyError() {
		System.err
				.println("Compilation halted due to static semantic errors.");
		System.exit(1);
	}
	
	public AbstractSymbol getLUB(AbstractSymbol a, AbstractSymbol b){
		return null;
	}
	
	/**
	 * Prints line number and file name of the given class.
	 * 
	 * Also increments semantic error count.
	 * 
	 * @param c
	 *            the class
	 * @return a print stream to which the rest of the error message is to be
	 *         printed.
	 * 
	 * */
	public PrintStream semantError(class_c c) {
		return semantError(c.getFilename(), c);
	}

	/**
	 * Prints the file name and the line number of the given tree node.
	 * 
	 * Also increments semantic error count.
	 * 
	 * @param filename
	 *            the file name
	 * @param t
	 *            the tree node
	 * @return a print stream to which the rest of the error message is to be
	 *         printed.
	 * 
	 * */
	public PrintStream semantError(AbstractSymbol filename, TreeNode t) {
		errorStream.print(filename + ":" + t.getLineNumber() + ": ");
		return semantError();
	}

	/**
	 * Increments semantic error count and returns the print stream for error
	 * messages.
	 * 
	 * @return a print stream to which the error message is to be printed.
	 * 
	 * */
	public PrintStream semantError() {
		semantErrors++;
		return errorStream;
	}

	/** Returns true if there are any static semantic errors. */
	public boolean errors() {
		return semantErrors != 0;
	}
}
