package de.BA.refactoringBot.model.javaparser;

import java.util.ArrayList;
import java.util.List;

public class ParserRefactoringCollection {

	private List<ParserRefactoring> refactorings = new ArrayList<ParserRefactoring>();
	private List<String> doneClasses = new ArrayList<String>();
	private List<String> toDoClasses = new ArrayList<String>();

	public List<ParserRefactoring> getRefactoring() {
		return refactorings;
	}

	public void setRefactoring(List<ParserRefactoring> refactorings) {
		this.refactorings = refactorings;
	}
	
	public void addRefactoring(ParserRefactoring refactoring) {
		this.refactorings.add(refactoring);
	}
	
	public void addRefactorings(List<ParserRefactoring> refactorings) {
		this.refactorings.addAll(refactorings);
	}

	public List<String> getDoneClasses() {
		return doneClasses;
	}

	public void setDoneClasses(List<String> doneClasses) {
		this.doneClasses = doneClasses;
	}
	
	public void addDoneClass(String doneClass) {
		this.doneClasses.add(doneClass);
	}
	
	public void addDoneClasses(List<String> doneClasses) {
		this.doneClasses.addAll(doneClasses);
	}
	
	public void removeDoneClass(String doneClass) {
		this.doneClasses.remove(doneClass);
	}
	
	public void emptyDoneClass() {
		this.doneClasses.clear();
	}

	public List<String> getToDoClasses() {
		return toDoClasses;
	}

	public void setToDoClasses(List<String> toDoClasses) {
		this.toDoClasses = toDoClasses;
	}
	
	public void addToDoClass(String toDoClass) {
		this.toDoClasses.add(toDoClass);
	}
	
	public void removeToDoClass(String toDoClass) {
		this.toDoClasses.remove(toDoClass);
	}
}
