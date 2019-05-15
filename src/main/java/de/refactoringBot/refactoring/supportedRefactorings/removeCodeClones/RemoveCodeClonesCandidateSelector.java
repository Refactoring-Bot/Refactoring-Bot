package de.refactoringBot.refactoring.supportedRefactorings.removeCodeClones;

import de.refactoringBot.refactoring.supportedRefactorings.ExtractMethod.LineMapVariable;
import de.refactoringBot.refactoring.supportedRefactorings.ExtractMethod.RefactorCandidate;
import de.refactoringBot.refactoring.supportedRefactorings.ExtractMethod.StatementGraphNode;

import java.util.*;

public class RemoveCodeClonesCandidateSelector {
    private StatementGraphNode fullGraph;
    private List<RefactorCandidate> candidates;
    private Map<Long, LineMapVariable> variableMap;
    private List<Long> commentLines;
    private List<Long> emptyLines;
    private Long methodEndLine;
    private Long methodStartLine;
    private Long cloneStartLine;
    private Long cloneEndLine;

    private final double lengthScoreWeight = 0.1;
    private final double maxLineLengthScore = 30;
    private final double nestingScoreWeight = 1;
    private final double paramScoreWeight = 1;
    private final double maxParameterScore = 4;
    private final double semanticsScoreWeight = 1;
    private final double semanticsBeginningWeight = 2;

    RemoveCodeClonesCandidateSelector(StatementGraphNode fullGraph, List<RefactorCandidate> candidates, Map<Long, LineMapVariable> variableMap,
                                      List<Long> commentLines, List<Long> emptyLines, Long methodStartLine, Long methodEndLine, Long cloneStartLine, Long cloneEndLine) {
        this.fullGraph = fullGraph;
        this.candidates = candidates;
        this.variableMap = variableMap;
        this.commentLines = commentLines;
        this.emptyLines = emptyLines;
        this.methodEndLine = methodEndLine;
        this.methodStartLine = methodStartLine;
        this.cloneStartLine = cloneStartLine;
        this.cloneEndLine = cloneEndLine;
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
            double cloneExceedScore = this.scoreCloneExceedLength(candidate);
            double statementScore = this.scoreStatements(candidate);
            candidate.score = lengthScore * cloneExceedScore * statementScore;
        }
    }

    private double scoreLength(RefactorCandidate candidate) {
        long lengthCandidate = candidate.endLine - candidate.startLine;
        return lengthCandidate;
    }

    private double scoreCloneExceedLength(RefactorCandidate candidate) {
        if (candidate.startLine < cloneStartLine || candidate.endLine > cloneEndLine) {
            return 0;
        } else {
            return 1;
        }
    }

    private double scoreStatements(RefactorCandidate candidate) {
        for (StatementGraphNode node : candidate.statements) {
            if (node.isExitNode) {
                return 0;
            }
        }
        return 1;
    }
}