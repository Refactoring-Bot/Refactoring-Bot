package refactoring;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.visitor.ModifierVisitor;
/**
 * 
 * @author Timo Pfaff
 * 
 * Class for removing unused variables. 
 *
 */
public class VariableDeletor extends ModifierVisitor<Void> {

	private String variableName;
	
	@Override
	public Node visit(VariableDeclarator declarator, Void args) {
		if (declarator.getNameAsString().equals(variableName)) {
			return null;
		}
		return declarator;

	}
	
	public void RemoveUnusedVariable(CompilationUnit compilationUnit) {
		this.visit(compilationUnit, null);
	}

	public String getVariableName() {
		return variableName;
	}

	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}

}
