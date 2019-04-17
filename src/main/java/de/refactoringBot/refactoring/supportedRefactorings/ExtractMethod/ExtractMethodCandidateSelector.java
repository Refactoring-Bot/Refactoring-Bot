package de.refactoringBot.refactoring.supportedRefactorings.ExtractMethod;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ExtractMethodCandidateSelector {
    private StatementGraphNode fullGraph;
    private List<RefactorCandidate> candidates;
    private Map<Long, LineMapVariable> variableMap;
    private List<Long> commentLines;
    private List<Long> emptyLines;
    private Long methodEndLine;
    private Long methodStartLine;

    private final double lengthScoreWeight = 1;
    private final double maxLineLengthScore = 40;
    private final double nestingScoreWeight = 1;
    private final double paramScoreWeight = 1;
    private final double maxParameterScore = 4;
    private final double semanticsScoreWeight = 1;
    private final double semanticsBeginningWeight = 2;

    ExtractMethodCandidateSelector(StatementGraphNode fullGraph, List<RefactorCandidate> candidates, Map<Long, LineMapVariable> variableMap, List<Long> commentLines, List<Long> emptyLines, Long methodStartLine, Long methodEndLine) {
        this.fullGraph = fullGraph;
        this.candidates = candidates;
        this.variableMap = variableMap;
        this.commentLines = commentLines;
        this.emptyLines = emptyLines;
        this.methodEndLine = methodEndLine;
        this.methodStartLine = methodStartLine;
    }

    public RefactorCandidate selectBestCandidate() {
        this.scoreCandidates();

        candidates.sort(new Comparator<RefactorCandidate>() {
            @Override
            public int compare(RefactorCandidate o1, RefactorCandidate o2) {
                return -Double.compare(o1.score, o2.score);
            }
        });

        return candidates.get(0);
    }

    private void scoreCandidates() {
        for (RefactorCandidate candidate : this.candidates) {
            double lengthScore = this.scoreLength(candidate);
            double nestingScore = this.scoreNesting(candidate);
            double parameterScore = this.scoreParameters(candidate);
            double semanticScore = this.scoreSemantics(candidate);
            candidate.score = lengthScore + nestingScore + parameterScore + semanticScore;
        }
    }

    private double scoreLength(RefactorCandidate candidate) {
        long lengthCandidate = candidate.endLine - candidate.startLine;
        long lengthRemainder = (this.methodEndLine - this.methodStartLine) - lengthCandidate;
        return this.lengthScoreWeight * 0.1 * (Math.min(Math.min(lengthCandidate, lengthRemainder), this.maxLineLengthScore));
    }

    private double scoreNesting(RefactorCandidate candidate) {
        long complexityMethod = this.calculateCognitiveComplexity(fullGraph.children, 1);
        long complexityCandidate = this.calculateCognitiveComplexity(candidate.statements, 1);
        StatementGraphNode remainderGraph = fullGraph;
        for (StatementGraphNode node : candidate.statements) {
            remainderGraph = this.removeNodeFromGraph(remainderGraph, node);
        }
        long complexityRemainder = this.calculateCognitiveComplexity(remainderGraph.children, 1);
        return this.nestingScoreWeight * (complexityMethod - Math.max(complexityCandidate, complexityRemainder));
    }

    private StatementGraphNode removeNodeFromGraph(StatementGraphNode originalGraph, StatementGraphNode node) {
        StatementGraphNode clonedGraph = originalGraph.clone();
        for (int i = 0; i < clonedGraph.children.size(); i++) {
            if (clonedGraph.children.get(i).linenumber.equals(node.linenumber)) {
                clonedGraph.children.remove(i);
                return clonedGraph;
            }
        }
        for (int i = 0; i < clonedGraph.children.size(); i++) {
            StatementGraphNode newNode = this.removeNodeFromGraph(clonedGraph.children.get(i), node);
            originalGraph.children.remove(i);
            originalGraph.children.add(i, newNode);
        }
        return clonedGraph;
    }

    private long calculateCognitiveComplexity(List<StatementGraphNode> graph, int depth) {
        long score = 0;
        for (StatementGraphNode node: graph) {
            if (node.isNestingNode) {
                score += depth;
                if (node.children.size() > 0) {
                    score += this.calculateCognitiveComplexity(node.children, ++depth);
                }
            }
        }
        return score;
    }

    private double scoreParameters(RefactorCandidate candidate) {
        // check input parameters
        Set<LocalVariable> inVariables = new HashSet<>();
        for (Long lineNumber = candidate.startLine; lineNumber <= candidate.endLine; lineNumber++) {
            if (this.variableMap.get(lineNumber) != null) {
                for (Map.Entry<LocalVariable, Set<Long>> variable : this.variableMap.get(lineNumber).in.entrySet()) {
                    for (Long inNumber : variable.getValue()) {
                        if (inNumber != null && inNumber < candidate.startLine) {
                            inVariables.add(variable.getKey());
                        }
                    }
                }
            }
        }
        candidate.inVariables.addAll(inVariables);
        int numberOfInVars = inVariables.size();
        int numberOfOutVars = candidate.outVariables.size();
        return this.paramScoreWeight * (maxParameterScore - numberOfInVars - numberOfOutVars);
    }

    private double scoreSemantics(RefactorCandidate candidate) {
        // lines at beginning
        int numberAtBeginning = 0;
        Long firstLine = candidate.startLine - 1;
        while (this.commentLines.contains(firstLine) || this.emptyLines.contains(firstLine)) {
            firstLine--;
            numberAtBeginning++;
        }
        // lines at end
        int numberAtEnd = 0;
        Long lastLine = candidate.endLine + 1;
        while (this.commentLines.contains(lastLine) || this.emptyLines.contains(lastLine)) {
            lastLine++;
            numberAtEnd++;
        }

        int linesAtBeginning = (numberAtBeginning > 0) ? 1 : 0;
        int linesAtEnd = (numberAtEnd > 0) ? 1 : 0;
        return this.semanticsScoreWeight * 0.25 * (this.semanticsBeginningWeight * (linesAtBeginning + numberAtBeginning) + linesAtEnd + numberAtEnd);
    }
}
